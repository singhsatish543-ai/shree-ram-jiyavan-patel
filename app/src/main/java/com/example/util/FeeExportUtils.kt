package com.example.util

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.FeePayment
import com.example.data.model.PaymentStatus
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

object FeeExportUtils {

  private const val RECEIPT_SCHOOL_TITLE = "SHREE RAM JIYAVAN PATEL JUNIOR HIGH SCHOOL"
  private const val RECEIPT_SCHOOL_ADDRESS = "BECHU KA PURWA, AIRAYAN, KHAGA, FATEHPUR"

  fun exportReceiptToPdf(
    context: Context,
    payment: FeePayment,
    strings: AppStrings
  ): File {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val safeReceipt = payment.receiptNo.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Receipt" }
    val outFile = File(exportDir, "SRJP_Receipt_$safeReceipt.pdf")
    FileOutputStream(outFile).use { fos ->
      writeReceiptPdf(payment = payment, strings = strings, outputStream = fos)
    }
    return outFile
  }

  fun saveReceiptPdfToDownloads(
    context: Context,
    payment: FeePayment,
    strings: AppStrings
  ): File {
    val cacheFile = exportReceiptToPdf(context, payment, strings)
    val bytes = cacheFile.readBytes()
    val safeReceipt = payment.receiptNo.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Receipt" }
    val fileName = "SRJP_Receipt_$safeReceipt.pdf"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      runCatching {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
          put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
          put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/SRJP_School")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
          resolver.openOutputStream(uri)?.use { out ->
            out.write(bytes)
          }
        }
      }
    }
    return cacheFile
  }

  fun shareReceiptPdf(
    context: Context,
    payment: FeePayment,
    strings: AppStrings
  ) {
    val file = exportReceiptToPdf(context, payment, strings)
    shareFile(context, file, "application/pdf", strings.shareReceipt)
  }

  fun printFeeReceipt(
    context: Context,
    payment: FeePayment,
    strings: AppStrings
  ) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    val pdfFile = exportReceiptToPdf(context, payment, strings)
    val pdfBytes = pdfFile.readBytes()
    val jobName = "SRJP_Fee_Receipt_${payment.receiptNo}"

    val adapter = object : PrintDocumentAdapter() {
      override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
      ) {
        if (cancellationSignal?.isCanceled == true) {
          callback.onLayoutCancelled()
          return
        }
        val info = PrintDocumentInfo.Builder("${jobName}.pdf")
          .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
          .setPageCount(1)
          .build()
        callback.onLayoutFinished(info, true)
      }

      override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
      ) {
        if (cancellationSignal?.isCanceled == true) {
          callback.onWriteCancelled()
          return
        }
        try {
          FileOutputStream(destination?.fileDescriptor).use { out ->
            out.write(pdfBytes)
          }
          callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
          callback.onWriteFailed(e.localizedMessage ?: "Print write error")
        }
      }
    }
    printManager.print(jobName, adapter, PrintAttributes.Builder().build())
  }

  internal fun writeReceiptPdf(
    payment: FeePayment,
    strings: AppStrings,
    outputStream: OutputStream
  ) {
    val paidNow = if (payment.amountPaid > 0L) payment.amountPaid else payment.totalAmount
    val status = PaymentStatus.fromRaw(payment.paymentStatus)
    val streamContent = buildString {
      // Bordered A4 receipt box
      append("0.059 0.184 0.447 RG 1.5 w 36 330 523 476 re S\n")
      // Header royal blue bar
      append("0.059 0.184 0.447 rg 36 742 523 64 re f\n")
      append("BT 1 1 1 rg /F2 13 Tf 52 782 Td (${escapePdfText(RECEIPT_SCHOOL_TITLE)}) Tj ET\n")
      append("BT 0.86 0.91 1 rg /F1 9.5 Tf 52 764 Td (${escapePdfText(RECEIPT_SCHOOL_ADDRESS)}) Tj ET\n")
      append("BT 0.86 0.91 1 rg /F2 8.5 Tf 52 749 Td (${escapePdfText("OFFICIAL FEE RECEIPT (${payment.academicYear})")}) Tj ET\n")

      // Status pill in header right
      val statusText = status.name
      append("1 1 1 rg 462 752 84 18 re f\n")
      append("BT 0.059 0.184 0.447 rg /F2 8.5 Tf 474 758 Td (${escapePdfText(statusText)}) Tj ET\n")

      // Metadata rows
      var y = 716f
      val leftX = 52f
      val rightValX = 330f

      val detailRows = listOf(
        "Receipt Number" to payment.receiptNo,
        "Payment Date" to payment.paidOnDate,
        "Student Name" to payment.studentName,
        "Admission Number" to payment.admissionNo,
        "Father's Name" to payment.fatherName.ifBlank { "-" },
        "Class" to payment.className,
        "Fee Month" to "${payment.monthName} (${payment.academicYear})",
        "Payment Mode" to payment.paymentMode
      )

      detailRows.forEachIndexed { idx, (label, value) ->
        if (idx % 2 == 0) {
          append("0.965 0.976 1 rg 46 ${y - 5f} 503 20 re f\n")
        }
        append("BT 0.35 0.42 0.51 rg /F1 9.5 Tf $leftX $y Td (${escapePdfText(label)}) Tj ET\n")
        append("BT 0.09 0.14 0.22 rg /F2 9.5 Tf $rightValX $y Td (${escapePdfText(value)}) Tj ET\n")
        y -= 22f
      }

      append("0.796 0.835 0.882 RG 0.8 w 46 ${y + 8f} m 549 ${y + 8f} l S\n")
      y -= 10f

      val amountRows = mutableListOf(
        "Monthly Tuition Fee" to "INR ${payment.tuitionFee}"
      )
      if (payment.otherFee > 0L) amountRows.add("Other / Exam / Dev Fee" to "INR ${payment.otherFee}")
      if (payment.lateFee > 0L) amountRows.add("Late Fee" to "INR ${payment.lateFee}")
      if (payment.discount > 0L) amountRows.add("Discount" to "-INR ${payment.discount}")
      amountRows.add("Total Amount" to "INR ${payment.totalAmount}")
      amountRows.add("Previous Balance" to "INR ${payment.previousBalance}")
      amountRows.add("Amount Paid" to "INR $paidNow")
      amountRows.add("Remaining Balance" to "INR ${payment.remainingBalance}")

      amountRows.forEachIndexed { idx, (label, value) ->
        val isBoldHighlight = label == "Amount Paid" || label == "Remaining Balance"
        if (label == "Amount Paid") {
          append("0.886 0.929 1 rg 46 ${y - 5f} 503 22 re f\n")
        } else if (idx % 2 == 1) {
          append("0.98 0.985 0.995 rg 46 ${y - 5f} 503 20 re f\n")
        }
        val font = if (isBoldHighlight) "/F2" else "/F1"
        val color = if (label == "Amount Paid") "0.059 0.184 0.447" else "0.09 0.14 0.22"
        append("BT $color rg $font 10 Tf $leftX $y Td (${escapePdfText(label)}) Tj ET\n")
        append("BT $color rg /F2 10 Tf $rightValX $y Td (${escapePdfText(value)}) Tj ET\n")
        y -= 21f
      }

      if (payment.remarks.isNotBlank()) {
        append("BT 0.35 0.42 0.51 rg /F1 8.5 Tf $leftX $y Td (${escapePdfText("Remarks: ${payment.remarks}")}) Tj ET\n")
        y -= 16f
      }
      if (payment.refundOrCorrectionReason.isNotBlank()) {
        append("BT 0.72 0.14 0.18 rg /F2 8.5 Tf $leftX $y Td (${escapePdfText("Audit Note (${payment.paymentStatus}): ${payment.refundOrCorrectionReason}")}) Tj ET\n")
      }

      // Signature footer
      append("0.796 0.835 0.882 RG 0.8 w 46 392 m 549 392 l S\n")
      append("BT 0.35 0.42 0.51 rg /F1 8.5 Tf 52 368 Td (${escapePdfText("Collected By: ${payment.collectedBy.ifBlank { "School Admin" }}")}) Tj ET\n")
      append("0.35 0.42 0.51 RG 0.8 w 385 370 m 535 370 l S\n")
      append("BT 0.059 0.184 0.447 rg /F2 9 Tf 402 354 Td (${escapePdfText("Authorized Signature")}) Tj ET\n")
      append("BT 0.45 0.52 0.61 rg /F1 7.5 Tf 52 342 Td (${escapePdfText("Computer-generated official receipt - Shree Ram Jiyavan Patel Junior High School")}) Tj ET\n")
    }

    writeSingleOrMultiPagePdf(listOf(streamContent), outputStream)
  }

  fun exportFeeReportToPdf(
    context: Context,
    report: FeeReportData,
    strings: AppStrings
  ): File {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    val outFile = File(exportDir, "SRJP_FeeReport_${report.type.name}_$stamp.pdf")
    FileOutputStream(outFile).use { fos ->
      writeFeeReportPdf(report = report, outputStream = fos)
    }
    return outFile
  }

  internal fun writeFeeReportPdf(
    report: FeeReportData,
    outputStream: OutputStream
  ) {
    val margin = 28f
    val rowHeight = 20f
    val colCount = report.headers.size.coerceAtLeast(1)
    val usableWidth = 539f
    val colStep = usableWidth / colCount.toFloat()
    val colXs = FloatArray(colCount) { idx -> margin + idx * colStep }
    val dateNow = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())

    val firstPageRows = 26
    val laterPageRows = 32
    val pagesData = mutableListOf<List<FeeReportRow>>()
    if (report.rows.isEmpty()) {
      pagesData.add(emptyList())
    } else {
      val firstEnd = minOf(firstPageRows, report.rows.size)
      pagesData.add(report.rows.subList(0, firstEnd))
      var idx = firstEnd
      while (idx < report.rows.size) {
        val nextEnd = minOf(idx + laterPageRows, report.rows.size)
        pagesData.add(report.rows.subList(idx, nextEnd))
        idx = nextEnd
      }
    }

    val pageStreams = pagesData.mapIndexed { pageIdx, rows ->
      buildString {
        var cursorY: Float
        if (pageIdx == 0) {
          append("0.059 0.184 0.447 rg 28 756 539 58 re f\n")
          append("BT 1 1 1 rg /F2 12.5 Tf 40 794 Td (${escapePdfText(RECEIPT_SCHOOL_TITLE)}) Tj ET\n")
          append("BT 0.86 0.91 1 rg /F1 8.5 Tf 40 780 Td (${escapePdfText(RECEIPT_SCHOOL_ADDRESS)}) Tj ET\n")
          append("BT 0.86 0.91 1 rg /F1 8.5 Tf 40 765 Td (${escapePdfText("${report.title}  |  ${report.subtitle}  |  Generated: $dateNow")}) Tj ET\n")
          append("BT 0.15 0.22 0.31 rg /F2 9.5 Tf 28 738 Td (${escapePdfText("${report.summaryLabel}: INR ${report.summaryAmount}  |  Records: ${report.rows.size}")}) Tj ET\n")
          cursorY = 708f
        } else {
          append("BT 0.15 0.22 0.31 rg /F2 9.5 Tf 28 800 Td (${escapePdfText("${report.title} (Page ${pageIdx + 1})")}) Tj ET\n")
          cursorY = 770f
        }

        append("0.886 0.929 1 rg 28 $cursorY 539 20 re f\n")
        report.headers.forEachIndexed { cIdx, h ->
          append("BT 0.059 0.184 0.447 rg /F2 8 Tf ${colXs[cIdx] + 4f} ${cursorY + 6f} Td (${escapePdfText(clipPdfField(h, 14))}) Tj ET\n")
        }
        append("0.796 0.835 0.882 RG 0.6 w 28 $cursorY m 567 $cursorY l S\n")
        cursorY -= rowHeight

        rows.forEachIndexed { rIdx, row ->
          if (rIdx % 2 == 1) {
            append("0.965 0.976 1 rg 28 $cursorY 539 $rowHeight re f\n")
          }
          row.cells.take(colCount).forEachIndexed { cIdx, cellVal ->
            append("BT 0.118 0.161 0.231 rg /F1 8 Tf ${colXs[cIdx] + 4f} ${cursorY + 6f} Td (${escapePdfText(clipPdfField(cellVal, 15))}) Tj ET\n")
          }
          append("0.85 0.88 0.92 RG 0.5 w 28 $cursorY m 567 $cursorY l S\n")
          cursorY -= rowHeight
        }
      }
    }

    writeSingleOrMultiPagePdf(pageStreams, outputStream)
  }

  fun exportFeeReportToExcel(
    context: Context,
    report: FeeReportData,
    strings: AppStrings
  ): File {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    val outFile = File(exportDir, "SRJP_FeeReport_${report.type.name}_$stamp.xlsx")
    FileOutputStream(outFile).use { fos ->
      writeFeeReportExcel(report = report, outputStream = fos)
    }
    return outFile
  }

  internal fun writeFeeReportExcel(
    report: FeeReportData,
    outputStream: OutputStream
  ) {
    val sb = StringBuilder()
    sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
    appendStringRow(sb, 1, listOf(RECEIPT_SCHOOL_TITLE, RECEIPT_SCHOOL_ADDRESS, report.title, report.subtitle), 0)
    appendStringRow(sb, 2, listOf(report.summaryLabel, "INR ${report.summaryAmount}", "Total Rows", report.rows.size.toString()), 0)
    appendStringRow(sb, 3, report.headers, 1)

    report.rows.forEachIndexed { idx, row ->
      val rowNum = idx + 4
      sb.append("<row r=\"").append(rowNum).append("\">")
      row.cells.forEachIndexed { colIdx, value ->
        val numeric = value.toLongOrNull()
        if (numeric != null && colIdx >= 2) {
          sb.append("<c r=\"").append(colName(colIdx)).append(rowNum).append("\"><v>")
            .append(numeric).append("</v></c>")
        } else {
          appendInlineCell(sb, colName(colIdx) + rowNum, value, 0)
        }
      }
      sb.append("</row>")
    }
    sb.append("</sheetData></worksheet>")
    val sheetXml = sb.toString()

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
    <sheet name="FeeReport" sheetId="1" r:id="rId1"/>
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

  fun shareFile(
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

  private fun writeSingleOrMultiPagePdf(
    pageStreams: List<String>,
    outputStream: OutputStream
  ) {
    val pageWidth = 595
    val pageHeight = 842
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

  private fun writeZipTextEntry(zip: ZipOutputStream, name: String, content: String) {
    zip.putNextEntry(ZipEntry(name))
    zip.write(content.toByteArray(Charsets.UTF_8))
    zip.closeEntry()
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
}
