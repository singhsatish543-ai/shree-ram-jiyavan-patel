package com.example.util

import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.i18n.SchoolClasses
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class DailyClassAttendanceSummary(
  val className: String,
  val totalStudents: Int,
  val markedCount: Int,
  val presentCount: Int,
  val absentCount: Int,
  val leaveCount: Int,
  val attendancePercent: Double
)

data class DailyAttendanceReport(
  val dateKey: String,
  val totalStudents: Int,
  val markedCount: Int,
  val unmarkedCount: Int,
  val presentCount: Int,
  val absentCount: Int,
  val leaveCount: Int,
  val attendancePercent: Double,
  val classSummaries: List<DailyClassAttendanceSummary>,
  val records: List<AttendanceRecord>
)

data class MonthlyDaySummary(
  val dateKey: String,
  val totalMarked: Int,
  val presentCount: Int,
  val absentCount: Int,
  val leaveCount: Int,
  val attendancePercent: Double
)

data class StudentAttendanceStats(
  val student: StudentRecord,
  val totalMarked: Int,
  val presentCount: Int,
  val absentCount: Int,
  val leaveCount: Int,
  val attendancePercent: Double,
  val records: List<AttendanceRecord>
)

data class MonthlyAttendanceReport(
  val monthKey: String,
  val totalWorkingDays: Int,
  val totalEntries: Int,
  val presentCount: Int,
  val absentCount: Int,
  val leaveCount: Int,
  val attendancePercent: Double,
  val daySummaries: List<MonthlyDaySummary>,
  val studentStats: List<StudentAttendanceStats>
)

object AttendanceUtils {
  val STANDARD_SECTIONS = listOf("A", "B", "C")

  fun todayDateKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

  fun currentMonthKey(): String =
    SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date())

  fun millisToDateKey(utcMillis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
      timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date(utcMillis))
  }

  fun dateKeyToUtcMillis(dateKey: String): Long? = runCatching {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
      timeZone = java.util.TimeZone.getTimeZone("UTC")
      isLenient = false
    }
    sdf.parse(dateKey.trim())?.time
  }.getOrNull()

  fun shiftDateKey(dateKey: String, deltaDays: Int): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val cal = Calendar.getInstance()
    val parsed = runCatching { sdf.parse(dateKey.trim()) }.getOrNull()
    if (parsed != null) cal.time = parsed
    cal.add(Calendar.DAY_OF_YEAR, deltaDays)
    return sdf.format(cal.time)
  }

  fun shiftMonthKey(monthKey: String, deltaMonths: Int): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
    val cal = Calendar.getInstance()
    val parsed = runCatching { sdf.parse(monthKey.trim()) }.getOrNull()
    if (parsed != null) cal.time = parsed
    cal.add(Calendar.MONTH, deltaMonths)
    return sdf.format(cal.time)
  }

  fun formatDisplayDate(dateKey: String): String {
    val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val outFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    return runCatching { inFmt.parse(dateKey.trim())?.let(outFmt::format) }.getOrNull() ?: dateKey
  }

  fun formatDisplayMonth(monthKey: String): String {
    val inFmt = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
    val outFmt = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    return runCatching { inFmt.parse(monthKey.trim())?.let(outFmt::format) }.getOrNull() ?: monthKey
  }

  fun buildRecordDocId(dateKey: String, studentId: String): String {
    val safeDateId = dateKey.trim().replace(Regex("[^a-zA-Z0-9]+"), "_")
    return "${safeDateId}_${studentId.trim()}"
  }

  fun availableSections(students: List<StudentRecord>, allSectionsLabel: String): List<String> {
    val fromStudents = students
      .map { it.section.trim().uppercase(Locale.ENGLISH) }
      .filter { it.isNotBlank() }
    val merged = (STANDARD_SECTIONS + fromStudents).distinct().sorted()
    return listOf(allSectionsLabel) + merged
  }

  fun filterActiveStudents(
    students: List<StudentRecord>,
    selectedClass: String,
    allClassesLabel: String,
    selectedSection: String,
    allSectionsLabel: String,
    searchQuery: String = ""
  ): List<StudentRecord> {
    val q = searchQuery.trim().lowercase(Locale.ENGLISH)
    return students
      .filter { student ->
        StudentStatus.fromRaw(student.status) == StudentStatus.ACTIVE &&
          (selectedClass == allClassesLabel || selectedClass.isBlank() || student.className == selectedClass) &&
          (selectedSection == allSectionsLabel || selectedSection.isBlank() ||
            student.section.equals(selectedSection, ignoreCase = true)) &&
          (q.isEmpty() ||
            student.fullName.lowercase(Locale.ENGLISH).contains(q) ||
            student.admissionNo.lowercase(Locale.ENGLISH).contains(q) ||
            student.rollNumber.lowercase(Locale.ENGLISH).contains(q))
      }
      .sortedWith(
        compareBy<StudentRecord>(
          { SchoolClasses.indexOf(it.className).let { idx -> if (idx >= 0) idx else Int.MAX_VALUE } },
          { it.section.uppercase(Locale.ENGLISH) },
          { it.rollNumber.toIntOrNull() ?: Int.MAX_VALUE },
          { it.fullName.lowercase(Locale.ENGLISH) }
        )
      )
  }

  fun deduplicateRecordsByStudentDate(records: List<AttendanceRecord>): List<AttendanceRecord> {
    return records
      .filter { it.studentId.isNotBlank() && it.dateKey.isNotBlank() }
      .groupBy { "${it.dateKey.trim()}__${it.studentId.trim()}" }
      .values
      .map { group ->
        group.maxByOrNull { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L } ?: group.first()
      }
  }

  fun findRecordForStudentOnDate(
    records: List<AttendanceRecord>,
    studentId: String,
    dateKey: String
  ): AttendanceRecord? {
    val cleanDate = dateKey.trim()
    return records
      .filter { it.studentId == studentId && it.dateKey.trim() == cleanDate }
      .maxByOrNull { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
  }

  fun hasDuplicateAttendance(
    records: List<AttendanceRecord>,
    studentId: String,
    dateKey: String
  ): Boolean = findRecordForStudentOnDate(records, studentId, dateKey) != null

  fun calculateAttendancePercentage(presentCount: Int, totalMarked: Int): Double {
    if (totalMarked <= 0) return 0.0
    return (presentCount * 100.0) / totalMarked
  }

  fun formatPercentage(percent: Double): String {
    val rounded = (percent * 10.0).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) {
      "${rounded.toInt()}%"
    } else {
      String.format(Locale.ENGLISH, "%.1f%%", rounded)
    }
  }

  fun formatPercentage(presentCount: Int, totalMarked: Int): String =
    formatPercentage(calculateAttendancePercentage(presentCount, totalMarked))

  fun buildDailyAttendanceReport(
    students: List<StudentRecord>,
    allRecords: List<AttendanceRecord>,
    dateKey: String,
    selectedClass: String,
    allClassesLabel: String,
    selectedSection: String,
    allSectionsLabel: String
  ): DailyAttendanceReport {
    val filteredStudents = filterActiveStudents(
      students = students,
      selectedClass = selectedClass,
      allClassesLabel = allClassesLabel,
      selectedSection = selectedSection,
      allSectionsLabel = allSectionsLabel
    )
    val studentIdSet = filteredStudents.map { it.id }.toSet()
    val dedupedDayRecords = deduplicateRecordsByStudentDate(allRecords)
      .filter { it.dateKey.trim() == dateKey.trim() && it.studentId in studentIdSet }

    val recordByStudent = dedupedDayRecords.associateBy { it.studentId }
    var present = 0
    var absent = 0
    var leave = 0
    var marked = 0

    filteredStudents.forEach { st ->
      val rec = recordByStudent[st.id]
      if (rec != null) {
        marked++
        when (AttendanceStatus.fromRaw(rec.status)) {
          AttendanceStatus.PRESENT -> present++
          AttendanceStatus.ABSENT -> absent++
          AttendanceStatus.LEAVE -> leave++
        }
      }
    }

    val classSummaries = filteredStudents
      .groupBy { it.className }
      .entries
      .sortedBy { SchoolClasses.indexOf(it.key).let { idx -> if (idx >= 0) idx else Int.MAX_VALUE } }
      .map { (cls, clsStudents) ->
        var clsPresent = 0
        var clsAbsent = 0
        var clsLeave = 0
        var clsMarked = 0
        clsStudents.forEach { st ->
          val rec = recordByStudent[st.id]
          if (rec != null) {
            clsMarked++
            when (AttendanceStatus.fromRaw(rec.status)) {
              AttendanceStatus.PRESENT -> clsPresent++
              AttendanceStatus.ABSENT -> clsAbsent++
              AttendanceStatus.LEAVE -> clsLeave++
            }
          }
        }
        DailyClassAttendanceSummary(
          className = cls,
          totalStudents = clsStudents.size,
          markedCount = clsMarked,
          presentCount = clsPresent,
          absentCount = clsAbsent,
          leaveCount = clsLeave,
          attendancePercent = calculateAttendancePercentage(clsPresent, clsMarked)
        )
      }

    return DailyAttendanceReport(
      dateKey = dateKey,
      totalStudents = filteredStudents.size,
      markedCount = marked,
      unmarkedCount = (filteredStudents.size - marked).coerceAtLeast(0),
      presentCount = present,
      absentCount = absent,
      leaveCount = leave,
      attendancePercent = calculateAttendancePercentage(present, marked),
      classSummaries = classSummaries,
      records = dedupedDayRecords.sortedBy { it.studentName.lowercase(Locale.ENGLISH) }
    )
  }

  fun buildMonthlyAttendanceReport(
    students: List<StudentRecord>,
    allRecords: List<AttendanceRecord>,
    monthKey: String,
    selectedClass: String,
    allClassesLabel: String,
    selectedSection: String,
    allSectionsLabel: String
  ): MonthlyAttendanceReport {
    val filteredStudents = filterActiveStudents(
      students = students,
      selectedClass = selectedClass,
      allClassesLabel = allClassesLabel,
      selectedSection = selectedSection,
      allSectionsLabel = allSectionsLabel
    )
    val studentIds = filteredStudents.map { it.id }.toSet()
    val monthRecords = deduplicateRecordsByStudentDate(allRecords)
      .filter { it.dateKey.trim().startsWith(monthKey.trim()) && it.studentId in studentIds }

    val presentTotal = monthRecords.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.PRESENT }
    val absentTotal = monthRecords.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.ABSENT }
    val leaveTotal = monthRecords.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.LEAVE }
    val totalEntries = monthRecords.size

    val daySummaries = monthRecords
      .groupBy { it.dateKey.trim() }
      .entries
      .sortedByDescending { it.key }
      .map { (day, dayRecords) ->
        val p = dayRecords.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.PRESENT }
        val a = dayRecords.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.ABSENT }
        val l = dayRecords.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.LEAVE }
        MonthlyDaySummary(
          dateKey = day,
          totalMarked = dayRecords.size,
          presentCount = p,
          absentCount = a,
          leaveCount = l,
          attendancePercent = calculateAttendancePercentage(p, dayRecords.size)
        )
      }

    val byStudent = monthRecords.groupBy { it.studentId }
    val studentStats = filteredStudents.map { student ->
      val recs = (byStudent[student.id] ?: emptyList()).sortedByDescending { it.dateKey }
      val p = recs.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.PRESENT }
      val a = recs.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.ABSENT }
      val l = recs.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.LEAVE }
      StudentAttendanceStats(
        student = student,
        totalMarked = recs.size,
        presentCount = p,
        absentCount = a,
        leaveCount = l,
        attendancePercent = calculateAttendancePercentage(p, recs.size),
        records = recs
      )
    }

    return MonthlyAttendanceReport(
      monthKey = monthKey,
      totalWorkingDays = daySummaries.size,
      totalEntries = totalEntries,
      presentCount = presentTotal,
      absentCount = absentTotal,
      leaveCount = leaveTotal,
      attendancePercent = calculateAttendancePercentage(presentTotal, totalEntries),
      daySummaries = daySummaries,
      studentStats = studentStats
    )
  }

  fun buildStudentAttendanceReport(
    students: List<StudentRecord>,
    allRecords: List<AttendanceRecord>,
    selectedClass: String,
    allClassesLabel: String,
    selectedSection: String,
    allSectionsLabel: String,
    searchQuery: String = ""
  ): List<StudentAttendanceStats> {
    val filteredStudents = filterActiveStudents(
      students = students,
      selectedClass = selectedClass,
      allClassesLabel = allClassesLabel,
      selectedSection = selectedSection,
      allSectionsLabel = allSectionsLabel,
      searchQuery = searchQuery
    )
    val byStudent = deduplicateRecordsByStudentDate(allRecords).groupBy { it.studentId }
    return filteredStudents.map { student ->
      val recs = (byStudent[student.id] ?: emptyList()).sortedByDescending { it.dateKey }
      val p = recs.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.PRESENT }
      val a = recs.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.ABSENT }
      val l = recs.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.LEAVE }
      StudentAttendanceStats(
        student = student,
        totalMarked = recs.size,
        presentCount = p,
        absentCount = a,
        leaveCount = l,
        attendancePercent = calculateAttendancePercentage(p, recs.size),
        records = recs
      )
    }
  }
}
