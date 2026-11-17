package com.example.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.i18n.AppStrings
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object StudentExportUtils {

  fun generateUniqueAdmissionNo(
    students: List<StudentRecord>,
    academicSession: String = "2026-27"
  ): String {
    val startYear = Regex("""(\d{4})""").find(academicSession)?.groupValues?.getOrNull(1) ?: "2026"
    val trailingDigitsRegex = Regex("""(\d+)$""")
    val maxExisting = students.mapNotNull { student ->
      trailingDigitsRegex.find(student.admissionNo.trim())?.groupValues?.getOrNull(1)?.toIntOrNull()
    }.maxOrNull() ?: 0
    val existingLower = students.map { it.admissionNo.trim().lowercase() }.toSet()

    var seq = (maxExisting + 1).coerceAtLeast(students.size + 1)
    var candidate = "SRJP-$startYear-${seq.toString().padStart(4, '0')}"
    while (candidate.lowercase() in existingLower) {
      seq++
      candidate = "SRJP-$startYear-${seq.toString().padStart(4, '0')}"
    }
    return candidate
  }

  fun isAdmissionNoDuplicate(
    students: List<StudentRecord>,
    admissionNo: String,
    excludeStudentId: String?
  ): Boolean {
    val target = admissionNo.trim().lowercase()
    if (target.isEmpty()) return false
    return students.any { s ->
      s.id != excludeStudentId && s.admissionNo.trim().lowercase() == target
    }
  }

  fun compressImageUriToJpegBytes(
    context: Context,
    uri: Uri,
    maxDimension: Int = 512,
    quality: Int = 82
  ): ByteArray? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxDimension * 2 ||
      bounds.outHeight / sampleSize > maxDimension * 2
    ) {
      sampleSize *= 2
    }

    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val decoded = resolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, decodeOpts)
    } ?: return null

    val scaled = if (decoded.width > maxDimension || decoded.height > maxDimension) {
      val ratio = minOf(
        maxDimension.toFloat() / decoded.width.toFloat(),
        maxDimension.toFloat() / decoded.height.toFloat()
      )
      val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
      val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
      Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
        if (it !== decoded) decoded.recycle()
      }
    } else {
      decoded
    }

    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
    scaled.recycle()
    return out.toByteArray()
  }

  fun exportStudentsToPdf(
    context: Context,
    students: List<StudentRecord>,
    strings: AppStrings,
    classFilter: String?,
    academicSession: String
  ): File {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    val outFile = File(exportDir, "SRJP_Students_$stamp.pdf")

    FileOutputStream(outFile).use { fos ->
      writePdfReport(
        students = students,
        schoolName = "Shree Ram Jiyavan Patel Junior High School",
        schoolAddress = "Bechu Ka Purwa, Airayan, Khaga, Fatehpur, Uttar Pradesh",
        classFilter = classFilter ?: "All Classes",
        academicSession = academicSession,
        outputStream = fos
      )
    }
    return outFile
  }

  internal fun writePdfReport(
    students: List<StudentRecord>,
    schoolName: String,
    schoolAddress: String,
    classFilter: String,
    academicSession: String,
    outputStream: OutputStream
  ) {
    val pageWidth = 595
    val pageHeight = 842
    val margin = 28f
    val rowHeight = 22f
    val dateNow = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
    val activeCount = students.count { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }

    val colXs = floatArrayOf(margin, 98f, 198f, 290f, 354f, 422f, 504f)
    val colChars = intArrayOf(14, 19, 17, 12, 12, 14, 10)
    val headers = listOf("Adm No", "Student Name", "Father Name", "Class", "DOB", "Mobile", "Status")

    // Split students across pages
    val firstPageRows = 28
    val laterPageRows = 32
    val pagesData = mutableListOf<List<StudentRecord>>()
    if (students.isEmpty()) {
      pagesData.add(emptyList())
    } else {
      var index = 0
      val firstChunk = students.subList(0, minOf(firstPageRows, students.size))
      pagesData.add(firstChunk)
      index = firstChunk.size
      while (index < students.size) {
        val nextEnd = minOf(index + laterPageRows, students.size)
        pagesData.add(students.subList(index, nextEnd))
        index = nextEnd
      }
    }

    val pageStreams = pagesData.mapIndexed { pageIdx, rows ->
      buildString {
        var cursorY: Float
        if (pageIdx == 0) {
          // Royal Blue header banner
          append("0.059 0.184 0.447 rg 28 756 539 58 re f\n")
          append("BT 1 1 1 rg /F2 13 Tf 40 794 Td (${escapePdfText(schoolName)}) Tj ET\n")
          append("BT 0.86 0.91 1 rg /F1 9 Tf 40 780 Td (${escapePdfText(schoolAddress)}) Tj ET\n")
          append(
            "BT 0.86 0.91 1 rg /F1 8.5 Tf 40 765 Td (${
              escapePdfText("Session: $academicSession  |  Filter: $classFilter  |  Generated: $dateNow")
            }) Tj ET\n"
          )
          append(
            "BT 0.15 0.22 0.31 rg /F2 9.5 Tf 28 738 Td (${
              escapePdfText("Student Admission Register - Total: ${students.size} (Active: $activeCount, Inactive: ${students.size - activeCount})")
            }) Tj ET\n"
          )
          cursorY = 708f
        } else {
          append(
            "BT 0.15 0.22 0.31 rg /F2 9.5 Tf 28 800 Td (${
              escapePdfText("SRJP Junior High School - Student Register (Page ${pageIdx + 1})")
            }) Tj ET\n"
          )
          cursorY = 770f
        }

        // Table header bar
        append("0.886 0.929 1 rg 28 $cursorY 539 20 re f\n")
        headers.forEachIndexed { cIdx, h ->
          append("BT 0.059 0.184 0.447 rg /F2 8.5 Tf ${colXs[cIdx] + 4f} ${cursorY + 6f} Td (${escapePdfText(h)}) Tj ET\n")
        }
        append("0.796 0.835 0.882 RG 0.6 w 28 $cursorY m 567 $cursorY l S\n")
        cursorY -= rowHeight

        rows.forEachIndexed { rIdx, s ->
          if (rIdx % 2 == 1) {
            append("0.965 0.976 1 rg 28 $cursorY 539 $rowHeight re f\n")
          }
          val vals = listOf(
            s.admissionNo,
            s.fullName,
            s.fatherName,
            "${s.className} (${s.section})",
            s.dateOfBirth.ifBlank { "-" },
            s.phone.ifBlank { "-" },
            s.status
          )
          vals.forEachIndexed { cIdx, v ->
            val clipped = clipPdfField(v, colChars[cIdx])
            append("BT 0.118 0.161 0.231 rg /F1 8 Tf ${colXs[cIdx] + 4f} ${cursorY + 7f} Td (${escapePdfText(clipped)}) Tj ET\n")
          }
          append("0.85 0.88 0.92 RG 0.5 w 28 $cursorY m 567 $cursorY l S\n")
          cursorY -= rowHeight
        }
      }
    }

    // Build PDF object table with exact xref offsets
    // Obj 1: Catalog, Obj 2: Pages, Obj 3: F1, Obj 4: F2, Obj 5..N: Page + Content pairs
    val objects = mutableListOf<ByteArray>()
    val pageObjIds = (0 until pageStreams.size).map { 5 + it * 2 }
    objects.add("<< /Type /Catalog /Pages 2 0 R >>".toByteArray(Charsets.US_ASCII))
    objects.add(
      "<< /Type /Pages /Kids [${pageObjIds.joinToString(" ") { "$it 0 R" }}] /Count ${pageStreams.size} >>"
        .toByteArray(Charsets.US_ASCII)
    )
    objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".toByteArray(Charsets.US_ASCII))
    objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>".toByteArray(Charsets.US_ASCII))

    pageStreams.forEachIndexed { idx, streamStr ->
      val pageObjId = 5 + idx * 2
      val contentObjId = pageObjId + 1
      val streamBytes = streamStr.toByteArray(Charsets.ISO_8859_1)
      objects.add(
        ("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $pageWidth $pageHeight] " +
          "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents $contentObjId 0 R >>")
          .toByteArray(Charsets.US_ASCII)
      )
      val header = "<< /Length ${streamBytes.size} >>\nstream\n".toByteArray(Charsets.US_ASCII)
      val footer = "\nendstream".toByteArray(Charsets.US_ASCII)
      objects.add(header + streamBytes + footer)
    }

    val baos = ByteArrayOutputStream()
    baos.write("%PDF-1.4\n".toByteArray(Charsets.US_ASCII))
    val offsets = mutableListOf<Int>()
    objects.forEachIndexed { idx, body ->
      offsets.add(baos.size())
      baos.write("${idx + 1} 0 obj\n".toByteArray(Charsets.US_ASCII))
      baos.write(body)
      baos.write("\nendobj\n".toByteArray(Charsets.US_ASCII))
    }

    val xrefStart = baos.size()
    baos.write("xref\n0 ${objects.size + 1}\n".toByteArray(Charsets.US_ASCII))
    baos.write("0000000000 65535 f \n".toByteArray(Charsets.US_ASCII))
    offsets.forEach { offset ->
      baos.write(String.format(Locale.ENGLISH, "%010d 00000 n \n", offset).toByteArray(Charsets.US_ASCII))
    }
    baos.write(
      ("trailer\n<< /Size ${objects.size + 1} /Root 1 0 R >>\nstartxref\n$xrefStart\n%%EOF\n")
        .toByteArray(Charsets.US_ASCII)
    )
    outputStream.write(baos.toByteArray())
  }

  private fun clipPdfField(raw: String, maxChars: Int): String {
    val clean = raw.trim()
    return if (clean.length <= maxChars) clean else clean.take((maxChars - 1).coerceAtLeast(1)) + "."
  }

  private fun escapePdfText(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
      when {
        ch == '\\' -> sb.append("\\\\")
        ch == '(' -> sb.append("\\(")
        ch == ')' -> sb.append("\\)")
        ch.code in 32..126 -> sb.append(ch)
        else -> sb.append(' ')
      }
    }
    return sb.toString()
  }

  fun exportStudentsToExcel(
    context: Context,
    students: List<StudentRecord>,
    strings: AppStrings,
    classFilter: String?,
    academicSession: String
  ): File {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    val outFile = File(exportDir, "SRJP_Students_$stamp.xlsx")

    FileOutputStream(outFile).use { fos ->
      writeExcelWorkbook(
        students = students,
        schoolName = strings.schoolName,
        schoolAddress = strings.schoolAddress,
        classFilter = classFilter ?: strings.allClasses,
        academicSession = academicSession,
        outputStream = fos
      )
    }
    return outFile
  }

  internal fun writeExcelWorkbook(
    students: List<StudentRecord>,
    schoolName: String,
    schoolAddress: String,
    classFilter: String,
    academicSession: String,
    outputStream: OutputStream
  ) {
    val sheetXml = buildWorksheetXml(students, schoolName, schoolAddress, classFilter, academicSession)
    ZipOutputStream(outputStream).use { zip ->
      writeZipTextEntry(
        zip,
        "[Content_Types].xml",
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
      )
      writeZipTextEntry(
        zip,
        "_rels/.rels",
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
      )
      writeZipTextEntry(
        zip,
        "xl/workbook.xml",
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Students" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
      )
      writeZipTextEntry(
        zip,
        "xl/_rels/workbook.xml.rels",
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
      )
      writeZipTextEntry(
        zip,
        "xl/styles.xml",
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="2">
    <font><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
  </fonts>
  <fills count="3">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF143C8C"/></patternFill></fill>
  </fills>
  <borders count="1">
    <border><left/><right/><top/><bottom/><diagonal/></border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
  </cellXfs>
</styleSheet>"""
      )
      writeZipTextEntry(zip, "xl/worksheets/sheet1.xml", sheetXml)
    }
  }

  private fun writeZipTextEntry(zip: ZipOutputStream, name: String, content: String) {
    zip.putNextEntry(ZipEntry(name))
    zip.write(content.toByteArray(Charsets.UTF_8))
    zip.closeEntry()
  }

  private fun buildWorksheetXml(
    students: List<StudentRecord>,
    schoolName: String,
    schoolAddress: String,
    classFilter: String,
    academicSession: String
  ): String {
    val sb = StringBuilder()
    sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")

    appendStringRow(sb, 1, listOf(schoolName, schoolAddress, "Session: $academicSession", "Filter: $classFilter"), style = 0)

    val headers = listOf(
      "Admission No", "Roll No", "Student Name", "Father Name", "Mother Name",
      "Date of Birth", "Gender", "Class", "Section", "Mobile Number",
      "Full Address", "Admission Date", "Academic Session", "Previous School",
      "Monthly Fee (INR)", "Status"
    )
    appendStringRow(sb, 2, headers, style = 1)

    students.forEachIndexed { idx, s ->
      val rowNum = idx + 3
      sb.append("<row r=\"").append(rowNum).append("\">")
      val cells = listOf(
        s.admissionNo,
        s.rollNumber,
        s.fullName,
        s.fatherName,
        s.motherName,
        s.dateOfBirth,
        s.gender,
        s.className,
        s.section,
        s.phone,
        s.address,
        s.admissionDate,
        s.academicSession,
        s.previousSchool
      )
      cells.forEachIndexed { colIdx, value ->
        appendInlineCell(sb, colName(colIdx) + rowNum, value, 0)
      }
      // Monthly fee numeric cell (column O = index 14)
      sb.append("<c r=\"").append(colName(14)).append(rowNum).append("\"><v>")
        .append(s.monthlyFeeAmount).append("</v></c>")
      // Status (column P = index 15)
      appendInlineCell(sb, colName(15) + rowNum, s.status, 0)
      sb.append("</row>")
    }

    sb.append("</sheetData></worksheet>")
    return sb.toString()
  }

  private fun appendStringRow(sb: StringBuilder, rowNum: Int, values: List<String>, style: Int) {
    sb.append("<row r=\"").append(rowNum).append("\">")
    values.forEachIndexed { colIdx, value ->
      appendInlineCell(sb, colName(colIdx) + rowNum, value, style)
    }
    sb.append("</row>")
  }

  private fun appendInlineCell(sb: StringBuilder, ref: String, value: String, style: Int) {
    sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"")
    if (style != 0) sb.append(" s=\"").append(style).append("\"")
    sb.append("><is><t>").append(escapeXml(value)).append("</t></is></c>")
  }

  private fun colName(index: Int): String {
    var n = index + 1
    val chars = StringBuilder()
    while (n > 0) {
      val rem = (n - 1) % 26
      chars.append(('A'.code + rem).toChar())
      n = (n - 1) / 26
    }
    return chars.reverse().toString()
  }

  private fun escapeXml(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
      when (ch) {
        '&' -> sb.append("&amp;")
        '<' -> sb.append("&lt;")
        '>' -> sb.append("&gt;")
        '"' -> sb.append("&quot;")
        '\'' -> sb.append("&apos;")
        '\t', '\n', '\r' -> sb.append(' ')
        else -> if (ch.code >= 0x20) sb.append(ch)
      }
    }
    return sb.toString()
  }

  fun shareExportedFile(
    context: Context,
    file: File,
    mimeType: String,
    chooserTitle: String
  ) {
    val authority = "${context.packageName}.fileprovider"
    val contentUri = FileProvider.getUriForFile(context, authority, file)
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, contentUri)
      putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
      clipData = ClipData.newRawUri(file.name, contentUri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
  }
}
