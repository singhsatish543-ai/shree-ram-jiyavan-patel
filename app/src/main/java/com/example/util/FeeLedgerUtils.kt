package com.example.util

import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.PaymentMode
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.i18n.AppStrings
import com.example.i18n.SchoolClasses
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class StudentFeeStanding {
  FULLY_PAID,
  PARTIALLY_PAID,
  UNPAID
}

enum class FeeReportType {
  DAILY,
  MONTHLY,
  ANNUAL,
  CLASS_WISE,
  CASH,
  UPI,
  PENDING_FEES,
  STUDENT_WISE
}

data class StudentMonthFeeSummary(
  val monthIndex: Int,
  val expected: Long,
  val paid: Long,
  val remaining: Long,
  val isFullyPaid: Boolean,
  val isPartiallyPaid: Boolean,
  val payments: List<FeePayment>
)

data class StudentSessionFeeSummary(
  val student: StudentRecord,
  val structure: FeeStructure?,
  val monthSummaries: List<StudentMonthFeeSummary>,
  val totalExpected: Long,
  val totalPaid: Long,
  val totalPending: Long,
  val paidMonths: List<Int>,
  val partialMonths: List<Int>,
  val pendingMonths: List<Int>,
  val standing: StudentFeeStanding
) {
  fun previousBalanceBeforeMonth(monthIndex: Int): Long =
    monthSummaries.filter { it.monthIndex < monthIndex }.sumOf { it.remaining }

  fun monthSummary(monthIndex: Int): StudentMonthFeeSummary? =
    monthSummaries.firstOrNull { it.monthIndex == monthIndex }
}

data class FeesDashboardSummary(
  val totalStudents: Int,
  val totalExpectedFees: Long,
  val totalCollectedFees: Long,
  val totalPendingFees: Long,
  val todaysCollection: Long,
  val thisMonthsCollection: Long,
  val fullyPaidStudents: Int,
  val partiallyPaidStudents: Int,
  val unpaidStudents: Int
)

data class ClassFeePendingSummary(
  val className: String,
  val studentCount: Int,
  val expected: Long,
  val collected: Long,
  val pending: Long,
  val pendingStudentCount: Int
)

data class MonthFeePendingSummary(
  val monthIndex: Int,
  val expected: Long,
  val collected: Long,
  val pending: Long,
  val paidStudentCount: Int,
  val pendingStudentCount: Int
)

data class FeeReportRow(
  val cells: List<String>,
  val amountValue: Long = 0L
)

data class FeeReportData(
  val type: FeeReportType,
  val title: String,
  val subtitle: String,
  val headers: List<String>,
  val rows: List<FeeReportRow>,
  val summaryLabel: String,
  val summaryAmount: Long
)

object FeeLedgerUtils {

  fun currentAcademicMonthIndex(calendar: Calendar = Calendar.getInstance()): Int {
    val calMonth = calendar.get(Calendar.MONTH)
    return if (calMonth >= Calendar.APRIL) (calMonth - Calendar.APRIL + 1) else (calMonth + 10)
  }

  fun todayDisplayDate(date: Date = Date()): String =
    SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(date)

  fun todayIsoDate(date: Date = Date()): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date)

  fun findFeeStructure(
    structures: List<FeeStructure>,
    className: String,
    academicSession: String
  ): FeeStructure? {
    val exact = structures.firstOrNull {
      it.className.equals(className, ignoreCase = true) &&
        it.academicSession.equals(academicSession, ignoreCase = true)
    }
    if (exact != null) return exact
    return structures.firstOrNull {
      it.className.equals(className, ignoreCase = true) &&
        (it.academicSession.isBlank() || it.academicSession == "2026-27")
    } ?: structures.firstOrNull { it.className.equals(className, ignoreCase = true) }
  }

  fun expectedMonthBreakdown(
    student: StudentRecord,
    structure: FeeStructure?,
    monthIndex: Int
  ): Triple<Long, Long, Long> {
    // Returns Triple(tuition, otherChargesIncludingOneTime, discount)
    val tuition = if (student.monthlyFeeAmount > 0L) {
      student.monthlyFeeAmount
    } else {
      structure?.monthlyTuitionFee ?: 0L
    }
    val monthlyOther = structure?.effectiveOtherCharges() ?: 0L
    val aprilOneTime = if (monthIndex == 1 && structure != null) {
      structure.admissionFee + structure.developmentFee
    } else {
      0L
    }
    val examSlice = if (structure != null && structure.examFee > 0L) {
      when (monthIndex) {
        6 -> structure.examFee / 2L
        12 -> structure.examFee - (structure.examFee / 2L)
        else -> 0L
      }
    } else {
      0L
    }
    val other = (monthlyOther + aprilOneTime + examSlice).coerceAtLeast(0L)
    val discount = (structure?.discount ?: 0L).coerceAtLeast(0L)
    return Triple(tuition, other, discount)
  }

  fun expectedMonthFee(
    student: StudentRecord,
    structure: FeeStructure?,
    monthIndex: Int
  ): Long {
    val (tuition, other, discount) = expectedMonthBreakdown(student, structure, monthIndex)
    return (tuition + other - discount).coerceAtLeast(0L)
  }

  fun buildStudentSessionSummary(
    student: StudentRecord,
    structures: List<FeeStructure>,
    payments: List<FeePayment>,
    academicSession: String
  ): StudentSessionFeeSummary {
    val structure = findFeeStructure(structures, student.className, academicSession)
    val validPayments = payments.filter {
      it.studentId == student.id &&
        it.academicYear == academicSession &&
        !it.isVoided()
    }
    val byMonth = validPayments.groupBy { it.monthIndex.toInt() }

    val monthSummaries = (1..12).map { monthIdx ->
      val monthPayments = byMonth[monthIdx].orEmpty()
      val baseExpected = expectedMonthFee(student, structure, monthIdx)
      val lateChargesApplied = monthPayments.sumOf { it.lateFee }
      val extraDiscountApplied = monthPayments.sumOf { p ->
        val structDisc = structure?.discount ?: 0L
        (p.discount - structDisc).coerceAtLeast(0L)
      }
      val expected = (baseExpected + lateChargesApplied - extraDiscountApplied).coerceAtLeast(0L)
      val paid = monthPayments.sumOf { it.effectivePaidAmount() }
      val remaining = (expected - paid).coerceAtLeast(0L)
      val isFullyPaid = (expected > 0L && remaining == 0L) ||
        (expected == 0L && monthPayments.any { it.remainingBalance == 0L && it.effectivePaidAmount() >= 0L })
      val isPartiallyPaid = !isFullyPaid && paid > 0L
      StudentMonthFeeSummary(
        monthIndex = monthIdx,
        expected = expected,
        paid = paid,
        remaining = remaining,
        isFullyPaid = isFullyPaid,
        isPartiallyPaid = isPartiallyPaid,
        payments = monthPayments
      )
    }

    val totalExpected = monthSummaries.sumOf { it.expected }
    val totalPaid = monthSummaries.sumOf { it.paid }
    val totalPending = monthSummaries.sumOf { it.remaining }
    val paidMonths = monthSummaries.filter { it.isFullyPaid }.map { it.monthIndex }
    val partialMonths = monthSummaries.filter { it.isPartiallyPaid }.map { it.monthIndex }
    val pendingMonths = monthSummaries.filter { !it.isFullyPaid && it.expected > 0L }.map { it.monthIndex }

    val standing = when {
      totalExpected == 0L && totalPaid == 0L -> StudentFeeStanding.UNPAID
      totalPending == 0L && totalPaid > 0L -> StudentFeeStanding.FULLY_PAID
      totalPaid > 0L -> StudentFeeStanding.PARTIALLY_PAID
      else -> StudentFeeStanding.UNPAID
    }

    return StudentSessionFeeSummary(
      student = student,
      structure = structure,
      monthSummaries = monthSummaries,
      totalExpected = totalExpected,
      totalPaid = totalPaid,
      totalPending = totalPending,
      paidMonths = paidMonths,
      partialMonths = partialMonths,
      pendingMonths = pendingMonths,
      standing = standing
    )
  }

  fun computeDashboardSummary(
    students: List<StudentRecord>,
    structures: List<FeeStructure>,
    payments: List<FeePayment>,
    academicSession: String,
    currentMonthIndex: Int = currentAcademicMonthIndex(),
    todayDisplay: String = todayDisplayDate(),
    todayIso: String = todayIsoDate()
  ): FeesDashboardSummary {
    val activeStudents = students.filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
    val summaries = activeStudents.map { student ->
      buildStudentSessionSummary(student, structures, payments, academicSession)
    }

    val activePayments = payments.filter { !it.isVoided() }
    val sessionPayments = activePayments.filter { it.academicYear == academicSession }

    val todaysCollection = activePayments.filter { p ->
      p.paidOnDate == todayDisplay || p.paidOnIsoDate == todayIso
    }.sumOf { it.effectivePaidAmount() }

    val thisMonthsCollection = sessionPayments.filter { p ->
      p.monthIndex.toInt() == currentMonthIndex
    }.sumOf { it.effectivePaidAmount() }

    val totalCollectedFees = sessionPayments.sumOf { it.effectivePaidAmount() }
    val totalExpectedFees = summaries.sumOf { it.totalExpected }
    val totalPendingFees = summaries.sumOf { it.totalPending }

    return FeesDashboardSummary(
      totalStudents = activeStudents.size,
      totalExpectedFees = totalExpectedFees,
      totalCollectedFees = totalCollectedFees,
      totalPendingFees = totalPendingFees,
      todaysCollection = todaysCollection,
      thisMonthsCollection = thisMonthsCollection,
      fullyPaidStudents = summaries.count { it.standing == StudentFeeStanding.FULLY_PAID },
      partiallyPaidStudents = summaries.count { it.standing == StudentFeeStanding.PARTIALLY_PAID },
      unpaidStudents = summaries.count { it.standing == StudentFeeStanding.UNPAID }
    )
  }

  fun computeClassPendingSummaries(
    studentSummaries: List<StudentSessionFeeSummary>
  ): List<ClassFeePendingSummary> {
    val orderMap = SchoolClasses.withIndex().associate { it.value to it.index }
    return studentSummaries
      .groupBy { it.student.className.ifBlank { "Unassigned" } }
      .map { (cls, list) ->
        ClassFeePendingSummary(
          className = cls,
          studentCount = list.size,
          expected = list.sumOf { it.totalExpected },
          collected = list.sumOf { it.totalPaid },
          pending = list.sumOf { it.totalPending },
          pendingStudentCount = list.count { it.totalPending > 0L }
        )
      }
      .sortedBy { orderMap[it.className] ?: Int.MAX_VALUE }
  }

  fun computeMonthPendingSummaries(
    studentSummaries: List<StudentSessionFeeSummary>
  ): List<MonthFeePendingSummary> {
    return (1..12).map { monthIdx ->
      val entries = studentSummaries.mapNotNull { it.monthSummary(monthIdx) }
      MonthFeePendingSummary(
        monthIndex = monthIdx,
        expected = entries.sumOf { it.expected },
        collected = entries.sumOf { it.paid },
        pending = entries.sumOf { it.remaining },
        paidStudentCount = entries.count { it.isFullyPaid },
        pendingStudentCount = entries.count { it.remaining > 0L }
      )
    }
  }

  fun buildFeeReport(
    type: FeeReportType,
    strings: AppStrings,
    students: List<StudentRecord>,
    structures: List<FeeStructure>,
    payments: List<FeePayment>,
    academicSession: String,
    selectedMonthIndex: Int,
    selectedDateDisplay: String,
    selectedDateIso: String
  ): FeeReportData {
    val activeStudents = students.filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
    val summaries = activeStudents.map { buildStudentSessionSummary(it, structures, payments, academicSession) }
    val validPayments = payments.filter { !it.isVoided() }
    val sessionPayments = validPayments.filter { it.academicYear == academicSession }

    return when (type) {
      FeeReportType.DAILY -> {
        val daily = validPayments.filter {
          it.paidOnDate == selectedDateDisplay || it.paidOnIsoDate == selectedDateIso
        }
        FeeReportData(
          type = type,
          title = strings.reportDailyCollection,
          subtitle = "${strings.dateLabel}: $selectedDateDisplay • ${strings.session}: $academicSession",
          headers = listOf("Receipt No", "Student", "Class", "Month", "Mode", "Amount (INR)"),
          rows = daily.map { p ->
            FeeReportRow(
              listOf(p.receiptNo, p.studentName, p.className, p.monthName, p.paymentMode, p.effectivePaidAmount().toString()),
              p.effectivePaidAmount()
            )
          },
          summaryLabel = strings.todaysCollection,
          summaryAmount = daily.sumOf { it.effectivePaidAmount() }
        )
      }

      FeeReportType.MONTHLY -> {
        val monthName = strings.months.getOrElse(selectedMonthIndex - 1) { "April" }
        val monthly = sessionPayments.filter { it.monthIndex.toInt() == selectedMonthIndex }
        FeeReportData(
          type = type,
          title = strings.reportMonthlyCollection,
          subtitle = "${strings.monthLabel}: $monthName • ${strings.session}: $academicSession",
          headers = listOf("Receipt No", "Student", "Class", "Date", "Mode", "Amount (INR)"),
          rows = monthly.map { p ->
            FeeReportRow(
              listOf(p.receiptNo, p.studentName, p.className, p.paidOnDate, p.paymentMode, p.effectivePaidAmount().toString()),
              p.effectivePaidAmount()
            )
          },
          summaryLabel = "$monthName ${strings.collectedTotal}",
          summaryAmount = monthly.sumOf { it.effectivePaidAmount() }
        )
      }

      FeeReportType.ANNUAL -> {
        val byMonth = computeMonthPendingSummaries(summaries)
        FeeReportData(
          type = type,
          title = strings.reportAnnualCollection,
          subtitle = "${strings.session}: $academicSession",
          headers = listOf("Month", "Expected (INR)", "Collected (INR)", "Pending (INR)", "Paid", "Due"),
          rows = byMonth.map { m ->
            val name = strings.months.getOrElse(m.monthIndex - 1) { "M${m.monthIndex}" }
            FeeReportRow(
              listOf(name, m.expected.toString(), m.collected.toString(), m.pending.toString(), m.paidStudentCount.toString(), m.pendingStudentCount.toString()),
              m.collected
            )
          },
          summaryLabel = strings.totalFeesCollectedLabel,
          summaryAmount = byMonth.sumOf { it.collected }
        )
      }

      FeeReportType.CLASS_WISE -> {
        val byClass = computeClassPendingSummaries(summaries)
        FeeReportData(
          type = type,
          title = strings.reportClassWiseCollection,
          subtitle = "${strings.session}: $academicSession",
          headers = listOf("Class", "Students", "Expected (INR)", "Collected (INR)", "Pending (INR)", "Due Count"),
          rows = byClass.map { c ->
            FeeReportRow(
              listOf(c.className, c.studentCount.toString(), c.expected.toString(), c.collected.toString(), c.pending.toString(), c.pendingStudentCount.toString()),
              c.collected
            )
          },
          summaryLabel = strings.totalFeesCollectedLabel,
          summaryAmount = byClass.sumOf { it.collected }
        )
      }

      FeeReportType.CASH -> {
        val cashPayments = sessionPayments.filter { PaymentMode.fromRaw(it.paymentMode) == PaymentMode.CASH }
        FeeReportData(
          type = type,
          title = strings.reportCashCollection,
          subtitle = "${strings.modeCash} • ${strings.session}: $academicSession",
          headers = listOf("Receipt No", "Student", "Class", "Month", "Date", "Amount (INR)"),
          rows = cashPayments.map { p ->
            FeeReportRow(
              listOf(p.receiptNo, p.studentName, p.className, p.monthName, p.paidOnDate, p.effectivePaidAmount().toString()),
              p.effectivePaidAmount()
            )
          },
          summaryLabel = strings.reportCashCollection,
          summaryAmount = cashPayments.sumOf { it.effectivePaidAmount() }
        )
      }

      FeeReportType.UPI -> {
        val upiPayments = sessionPayments.filter { PaymentMode.fromRaw(it.paymentMode) == PaymentMode.UPI }
        FeeReportData(
          type = type,
          title = strings.reportUpiCollection,
          subtitle = "${strings.modeUpi} • ${strings.session}: $academicSession",
          headers = listOf("Receipt No", "Student", "Class", "Month", "Date", "Amount (INR)"),
          rows = upiPayments.map { p ->
            FeeReportRow(
              listOf(p.receiptNo, p.studentName, p.className, p.monthName, p.paidOnDate, p.effectivePaidAmount().toString()),
              p.effectivePaidAmount()
            )
          },
          summaryLabel = strings.reportUpiCollection,
          summaryAmount = upiPayments.sumOf { it.effectivePaidAmount() }
        )
      }

      FeeReportType.PENDING_FEES -> {
        val dueStudents = summaries.filter { it.totalPending > 0L }
          .sortedByDescending { it.totalPending }
        FeeReportData(
          type = type,
          title = strings.reportPendingFees,
          subtitle = "${strings.session}: $academicSession • ${dueStudents.size} students",
          headers = listOf("Adm No", "Student", "Class", "Father", "Mobile", "Pending (INR)"),
          rows = dueStudents.map { s ->
            FeeReportRow(
              listOf(
                s.student.admissionNo,
                s.student.fullName,
                s.student.className,
                s.student.fatherName,
                s.student.phone.ifBlank { "-" },
                s.totalPending.toString()
              ),
              s.totalPending
            )
          },
          summaryLabel = strings.totalPendingFeesLabel,
          summaryAmount = dueStudents.sumOf { it.totalPending }
        )
      }

      FeeReportType.STUDENT_WISE -> {
        FeeReportData(
          type = type,
          title = strings.reportStudentWiseFee,
          subtitle = "${strings.session}: $academicSession • ${summaries.size} students",
          headers = listOf("Adm No", "Student", "Class", "Expected", "Paid", "Pending"),
          rows = summaries.map { s ->
            FeeReportRow(
              listOf(
                s.student.admissionNo,
                s.student.fullName,
                s.student.className,
                s.totalExpected.toString(),
                s.totalPaid.toString(),
                s.totalPending.toString()
              ),
              s.totalPaid
            )
          },
          summaryLabel = strings.totalFeesCollectedLabel,
          summaryAmount = summaries.sumOf { it.totalPaid }
        )
      }
    }
  }
}
