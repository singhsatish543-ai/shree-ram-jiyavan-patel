package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentStatus
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.formatInr
import com.example.ui.components.paymentModeLabel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.RoyalBlue700
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue100
import com.example.ui.theme.SkyBlue50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.FeeExportUtils
import com.example.util.FeeLedgerUtils
import com.example.util.FeeReportType
import com.example.util.StudentSessionFeeSummary

private enum class PendingViewMode {
  STUDENT_WISE,
  CLASS_WISE,
  MONTH_WISE
}

@Composable
internal fun PendingFeesTab(
  strings: AppStrings,
  students: List<StudentRecord>,
  structures: List<FeeStructure>,
  payments: List<FeePayment>,
  academicYear: String,
  onAcademicYearChange: (String) -> Unit,
  onQuickCollect: (StudentRecord) -> Unit,
  onShowMessage: (String) -> Unit
) {
  val context = LocalContext.current
  var viewMode by remember { mutableStateOf(PendingViewMode.STUDENT_WISE) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedClassFilter by remember { mutableStateOf<String?>(null) }

  val activeStudents = remember(students) {
    students.filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
  }
  val allSummaries = remember(activeStudents, structures, payments, academicYear) {
    activeStudents.map { FeeLedgerUtils.buildStudentSessionSummary(it, structures, payments, academicYear) }
  }

  val pendingStudentSummaries = remember(allSummaries, searchQuery, selectedClassFilter) {
    val q = searchQuery.trim().lowercase()
    allSummaries.filter { s ->
      val hasPending = s.totalPending > 0L
      val clsOk = selectedClassFilter == null || s.student.className == selectedClassFilter
      val qOk = q.isEmpty() ||
        s.student.fullName.lowercase().contains(q) ||
        s.student.admissionNo.lowercase().contains(q) ||
        s.student.fatherName.lowercase().contains(q)
      hasPending && clsOk && qOk
    }.sortedByDescending { it.totalPending }
  }

  val classPending = remember(allSummaries, selectedClassFilter) {
    FeeLedgerUtils.computeClassPendingSummaries(allSummaries)
      .filter { selectedClassFilter == null || it.className == selectedClassFilter }
  }

  val filteredForMonth = remember(allSummaries, selectedClassFilter) {
    allSummaries.filter { selectedClassFilter == null || it.student.className == selectedClassFilter }
  }
  val monthPending = remember(filteredForMonth) {
    FeeLedgerUtils.computeMonthPendingSummaries(filteredForMonth)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("pending_fees_tab_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      SessionSelectorBar(
        title = strings.pendingFees,
        academicYear = academicYear,
        onAcademicYearChange = onAcademicYearChange,
        strings = strings
      )
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(
              Triple(PendingViewMode.STUDENT_WISE, strings.studentWiseLabel, "pending_view_student"),
              Triple(PendingViewMode.CLASS_WISE, strings.classWiseLabel, "pending_view_class"),
              Triple(PendingViewMode.MONTH_WISE, strings.monthWiseLabel, "pending_view_month")
            ).forEach { (mode, label, tag) ->
              FilterChip(
                selected = viewMode == mode,
                onClick = { viewMode = mode },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = RoyalBlue800,
                  selectedLabelColor = PureWhite
                ),
                modifier = Modifier.testTag(tag)
              )
            }
          }

          if (viewMode == PendingViewMode.STUDENT_WISE) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text(strings.searchStudentsHint) },
              leadingIcon = { Icon(Icons.Filled.Search, contentDescription = strings.searchStudentsHint, tint = RoyalBlue700) },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = schoolTextFieldColors(),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("pending_search_input")
            )
          }

          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilterChip(
              selected = selectedClassFilter == null,
              onClick = { selectedClassFilter = null },
              label = { Text(strings.allClasses) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalBlue800,
                selectedLabelColor = PureWhite
              )
            )
            strings.classOptions.forEach { cls ->
              FilterChip(
                selected = selectedClassFilter == cls,
                onClick = { selectedClassFilter = if (selectedClassFilter == cls) null else cls },
                label = { Text(cls) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = RoyalBlue800,
                  selectedLabelColor = PureWhite
                )
              )
            }
          }
        }
      }
    }

    when (viewMode) {
      PendingViewMode.STUDENT_WISE -> {
        if (pendingStudentSummaries.isEmpty()) {
          item {
            EmptyStateCard(
              icon = Icons.Filled.CheckCircle,
              title = strings.noPendingFeesTitle,
              description = strings.noPendingFeesDesc
            )
          }
        } else {
          item {
            Text(
              text = "${strings.feeReminderListTitle} (${pendingStudentSummaries.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Slate700
            )
          }
          items(pendingStudentSummaries, key = { "pending_stu_${it.student.id}" }) { summary ->
            PendingStudentCard(
              summary = summary,
              strings = strings,
              academicYear = academicYear,
              onCollect = { onQuickCollect(summary.student) },
              onSendReminder = {
                sendFeeReminder(context, summary, academicYear, strings) { err ->
                  onShowMessage(err)
                }
              }
            )
          }
        }
      }

      PendingViewMode.CLASS_WISE -> {
        items(classPending, key = { "pending_cls_${it.className}" }) { cls ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(cls.className, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue900)
                Text(
                  text = "${cls.studentCount} ${strings.navStudents} • ${cls.pendingStudentCount} ${strings.dueCount}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate500
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(formatInr(cls.pending), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoseDue)
                Text("${strings.collectedTotal}: ${formatInr(cls.collected)}", style = MaterialTheme.typography.labelSmall, color = EmeraldPaid)
              }
            }
          }
        }
      }

      PendingViewMode.MONTH_WISE -> {
        items(monthPending, key = { "pending_mon_${it.monthIndex}" }) { m ->
          val mName = strings.months.getOrElse(m.monthIndex - 1) { "M${m.monthIndex}" }
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(mName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue900)
                Text(
                  text = "${strings.paidCount}: ${m.paidStudentCount} • ${strings.dueCount}: ${m.pendingStudentCount}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate500
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(formatInr(m.pending), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoseDue)
                Text("${strings.collectedTotal}: ${formatInr(m.collected)}", style = MaterialTheme.typography.labelSmall, color = EmeraldPaid)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PendingStudentCard(
  summary: StudentSessionFeeSummary,
  strings: AppStrings,
  academicYear: String,
  onCollect: () -> Unit,
  onSendReminder: () -> Unit
) {
  val student = summary.student
  val pendingNames = summary.pendingMonths.take(4).map { strings.monthsShort.getOrElse(it - 1) { "M$it" } }
  val suffix = if (summary.pendingMonths.size > 4) " +${summary.pendingMonths.size - 4}" else ""

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = PureWhite,
    border = BorderStroke(1.dp, RoseDue.copy(alpha = 0.28f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("pending_student_card_${student.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(student.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
          Text(
            text = "${student.className} • ${student.admissionNo} • ${strings.fatherName}: ${student.fatherName}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )
        }
        Text(
          text = formatInr(summary.totalPending),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = RoseDue
        )
      }

      Text(
        text = "${strings.pendingMonthsLabel} ($academicYear): ${pendingNames.joinToString(", ")}$suffix",
        style = MaterialTheme.typography.labelSmall,
        color = Slate700
      )

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = onSendReminder,
          border = BorderStroke(1.dp, RoyalBlue700),
          modifier = Modifier
            .weight(1f)
            .testTag("send_reminder_btn_${student.id}")
        ) {
          Icon(Icons.Filled.NotificationsActive, contentDescription = strings.sendReminderLabel, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text(strings.sendReminderLabel, color = RoyalBlue800, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
        Button(
          onClick = onCollect,
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
          modifier = Modifier
            .weight(1f)
            .testTag("pending_collect_btn_${student.id}")
        ) {
          Icon(Icons.Filled.CurrencyRupee, contentDescription = strings.collectFee, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text(strings.collectFee, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
      }
    }
  }
}

@Composable
internal fun FeeHistoryTab(
  strings: AppStrings,
  payments: List<FeePayment>,
  onViewReceipt: (FeePayment) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var modeFilter by remember { mutableStateOf<PaymentMode?>(null) }

  val filtered = remember(payments, searchQuery, modeFilter) {
    val q = searchQuery.trim().lowercase()
    payments.filter { p ->
      val mOk = modeFilter == null || PaymentMode.fromRaw(p.paymentMode) == modeFilter
      val qOk = q.isEmpty() ||
        p.studentName.lowercase().contains(q) ||
        p.receiptNo.lowercase().contains(q) ||
        p.admissionNo.lowercase().contains(q) ||
        p.className.lowercase().contains(q)
      mOk && qOk
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Surface(color = PureWhite, shadowElevation = 2.dp) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text(strings.searchStudentsHint) },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = strings.searchStudentsHint, tint = RoyalBlue700) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = schoolTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("fee_history_search_input")
        )
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = modeFilter == null,
            onClick = { modeFilter = null },
            label = { Text(strings.allClasses) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = RoyalBlue800,
              selectedLabelColor = PureWhite
            )
          )
          PaymentMode.entries.forEach { mode ->
            FilterChip(
              selected = modeFilter == mode,
              onClick = { modeFilter = if (modeFilter == mode) null else mode },
              label = { Text(paymentModeLabel(mode.name, strings)) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalBlue800,
                selectedLabelColor = PureWhite
              )
            )
          }
        }
      }
    }

    if (filtered.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        EmptyStateCard(
          icon = Icons.Outlined.Receipt,
          title = strings.recentReceipts,
          description = strings.noReceiptsYet
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("fee_history_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filtered, key = { it.id }) { payment ->
          val status = PaymentStatus.fromRaw(payment.paymentStatus)
          val paidShown = if (payment.amountPaid > 0L) payment.amountPaid else payment.totalAmount
          val (badgeBg, badgeColor, badgeText) = when (status) {
            PaymentStatus.PAID -> Triple(EmeraldPaidBg, EmeraldPaid, strings.paidBadge)
            PaymentStatus.PARTIAL -> Triple(AmberWarningBg, AmberWarning, strings.partialBadge)
            PaymentStatus.REFUNDED -> Triple(RoseDueBg, RoseDue, strings.refundedBadge)
            PaymentStatus.CORRECTED -> Triple(RoseDueBg, RoseDue, strings.correctedBadge)
          }
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .clickable { onViewReceipt(payment) }
              .testTag("receipt_row_${payment.id}")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(badgeBg),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = payment.receiptNo, tint = badgeColor)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                  text = "${payment.studentName} (${payment.className})",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Slate900
                )
                Text(
                  text = "${payment.receiptNo} • ${payment.monthName} (${payment.academicYear})",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate500
                )
                Text(
                  text = "${payment.paidOnDate} • ${paymentModeLabel(payment.paymentMode, strings)} • ${strings.remainingBalanceLabel}: ${formatInr(payment.remainingBalance)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = RoyalBlue700
                )
              }
              Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = formatInr(paidShown),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = badgeColor
                )
                Surface(shape = RoundedCornerShape(50), color = badgeBg) {
                  Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
internal fun FeeCollectionReportsTab(
  strings: AppStrings,
  students: List<StudentRecord>,
  structures: List<FeeStructure>,
  payments: List<FeePayment>,
  academicYear: String,
  onAcademicYearChange: (String) -> Unit,
  onShowMessage: (String) -> Unit
) {
  val context = LocalContext.current
  var selectedReportType by remember { mutableStateOf(FeeReportType.DAILY) }
  var selectedMonthIdx by remember { mutableIntStateOf(FeeLedgerUtils.currentAcademicMonthIndex()) }
  val todayDisplay = remember { FeeLedgerUtils.todayDisplayDate() }
  val todayIso = remember { FeeLedgerUtils.todayIsoDate() }

  val reportData = remember(
    selectedReportType, strings, students, structures, payments, academicYear, selectedMonthIdx, todayDisplay, todayIso
  ) {
    FeeLedgerUtils.buildFeeReport(
      type = selectedReportType,
      strings = strings,
      students = students,
      structures = structures,
      payments = payments,
      academicSession = academicYear,
      selectedMonthIndex = selectedMonthIdx,
      selectedDateDisplay = todayDisplay,
      selectedDateIso = todayIso
    )
  }

  val reportOptions = listOf(
    FeeReportType.DAILY to strings.reportDailyCollection,
    FeeReportType.MONTHLY to strings.reportMonthlyCollection,
    FeeReportType.ANNUAL to strings.reportAnnualCollection,
    FeeReportType.CLASS_WISE to strings.reportClassWiseCollection,
    FeeReportType.CASH to strings.reportCashCollection,
    FeeReportType.UPI to strings.reportUpiCollection,
    FeeReportType.PENDING_FEES to strings.reportPendingFees,
    FeeReportType.STUDENT_WISE to strings.reportStudentWiseFee
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("fee_reports_tab_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      SessionSelectorBar(
        title = strings.collectionReportTitle,
        academicYear = academicYear,
        onAcademicYearChange = onAcademicYearChange,
        strings = strings
      )
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            reportOptions.forEach { (type, label) ->
              FilterChip(
                selected = selectedReportType == type,
                onClick = { selectedReportType = type },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = RoyalBlue800,
                  selectedLabelColor = PureWhite
                ),
                modifier = Modifier.testTag("report_type_${type.name.lowercase()}")
              )
            }
          }

          if (selectedReportType == FeeReportType.MONTHLY) {
            Row(
              modifier = Modifier.horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              strings.monthsShort.forEachIndexed { idx, shortName ->
                val mNum = idx + 1
                FilterChip(
                  selected = selectedMonthIdx == mNum,
                  onClick = { selectedMonthIdx = mNum },
                  label = { Text(shortName) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoyalBlue800,
                    selectedLabelColor = PureWhite
                  )
                )
              }
            }
          }

          Surface(shape = RoundedCornerShape(12.dp), color = SkyBlue50, modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(reportData.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue900)
                Text(reportData.subtitle, style = MaterialTheme.typography.labelSmall, color = Slate500)
              }
              Text(
                text = formatInr(reportData.summaryAmount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue800
              )
            }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = {
                val file = FeeExportUtils.exportFeeReportToPdf(context, reportData, strings)
                runCatching {
                  FeeExportUtils.shareFile(context, file, "application/pdf", reportData.title)
                }.onFailure {
                  onShowMessage("${strings.exportPdf}: ${file.name}")
                }
              },
              border = BorderStroke(1.dp, RoseDue),
              modifier = Modifier
                .weight(1f)
                .testTag("export_fee_report_pdf_btn")
            ) {
              Icon(Icons.Filled.PictureAsPdf, contentDescription = strings.exportPdf, tint = RoseDue, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(strings.exportPdf, color = RoseDue)
            }

            OutlinedButton(
              onClick = {
                val file = FeeExportUtils.exportFeeReportToExcel(context, reportData, strings)
                runCatching {
                  FeeExportUtils.shareFile(
                    context,
                    file,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    reportData.title
                  )
                }.onFailure {
                  onShowMessage("${strings.exportExcel}: ${file.name}")
                }
              },
              border = BorderStroke(1.dp, EmeraldPaid),
              modifier = Modifier
                .weight(1f)
                .testTag("export_fee_report_excel_btn")
            ) {
              Icon(Icons.Filled.TableView, contentDescription = strings.exportExcel, tint = EmeraldPaid, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(strings.exportExcel, color = EmeraldPaid)
            }
          }
        }
      }
    }

    if (reportData.rows.isEmpty()) {
      item {
        EmptyStateCard(
          icon = Icons.Filled.Assessment,
          title = reportData.title,
          description = strings.noReceiptsYet
        )
      }
    } else {
      items(reportData.rows.indices.toList(), key = { "rep_row_$it" }) { idx ->
        val row = reportData.rows[idx]
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PureWhite,
          border = BorderStroke(1.dp, Slate200),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = row.cells.take(2).joinToString(" • "),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )
              Text(
                text = formatInr(row.amountValue),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue800
              )
            }
            HorizontalDivider(color = SkyBlue100)
            Text(
              text = reportData.headers.zip(row.cells).drop(2).joinToString(" • ") { (h, v) -> "$h: $v" },
              style = MaterialTheme.typography.labelSmall,
              color = Slate500
            )
          }
        }
      }
    }
  }
}

private fun sendFeeReminder(
  context: Context,
  summary: StudentSessionFeeSummary,
  academicYear: String,
  strings: AppStrings,
  onError: (String) -> Unit
) {
  val student = summary.student
  val message = buildString {
    appendLine("${strings.schoolName} (${strings.schoolAddress})")
    appendLine("Fee Reminder ($academicYear): ${student.fullName} (Adm: ${student.admissionNo}, ${student.className})")
    appendLine("Pending Fee Balance: INR ${summary.totalPending}")
    append("Please submit pending school fees at the school office at the earliest.")
  }
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_SUBJECT, "Fee Reminder - ${student.fullName} (${student.admissionNo})")
    putExtra(Intent.EXTRA_TEXT, message)
    if (student.phone.isNotBlank()) {
      putExtra("address", student.phone)
    }
  }
  runCatching {
    context.startActivity(Intent.createChooser(intent, strings.sendReminderLabel))
  }.onFailure {
    onError(it.localizedMessage ?: "Unable to open reminder share")
  }
}
