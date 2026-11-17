package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminDestination
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.SchoolNotice
import com.example.data.model.SchoolSettings
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.TeacherRecord
import com.example.data.model.UiState
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SummaryKpiCard
import com.example.ui.components.formatInr
import com.example.ui.components.paymentModeLabel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.AzureBlue600
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.OffWhiteBg
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
import com.example.util.FeeLedgerUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class ModuleNavItem(
  val destination: AdminDestination,
  val title: String,
  val icon: ImageVector,
  val accentColor: Color,
  val iconBg: Color,
  val testTag: String
)

@Composable
fun AdminDashboardScreen(
  strings: AppStrings,
  adminDisplayName: String,
  studentsState: UiState<List<StudentRecord>>,
  teachersState: UiState<List<TeacherRecord>>,
  paymentsState: UiState<List<FeePayment>>,
  structuresState: UiState<List<FeeStructure>>,
  attendanceState: UiState<List<AttendanceRecord>>,
  noticesState: UiState<List<SchoolNotice>>,
  schoolSettings: SchoolSettings?,
  onNavigate: (AdminDestination, Boolean) -> Unit,
  onReceiptClick: (FeePayment) -> Unit,
  modifier: Modifier = Modifier
) {
  val currentAcademicMonthIndex = remember {
    val calMonth = Calendar.getInstance().get(Calendar.MONTH)
    if (calMonth >= Calendar.APRIL) (calMonth - Calendar.APRIL + 1) else (calMonth + 10)
  }
  val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()) }
  val todayDisplayDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()) }
  val activeSession = schoolSettings?.academicSession?.ifBlank { "2026-27" } ?: "2026-27"
  val currentMonthName = strings.months.getOrElse(currentAcademicMonthIndex - 1) { strings.months.first() }

  when {
    studentsState is UiState.Loading ||
      teachersState is UiState.Loading ||
      paymentsState is UiState.Loading ||
      attendanceState is UiState.Loading -> {
      Box(
        modifier = modifier
          .fillMaxSize()
          .background(OffWhiteBg)
          .testTag("admin_dashboard_loading"),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = RoyalBlue800)
      }
    }

    studentsState is UiState.Error -> {
      DashboardErrorView(strings.errorLoadingData, studentsState.message, modifier)
    }

    teachersState is UiState.Error -> {
      DashboardErrorView(strings.errorLoadingData, teachersState.message, modifier)
    }

    paymentsState is UiState.Error -> {
      DashboardErrorView(strings.errorLoadingData, paymentsState.message, modifier)
    }

    attendanceState is UiState.Error -> {
      DashboardErrorView(strings.errorLoadingData, attendanceState.message, modifier)
    }

    else -> {
      val students = (studentsState as UiState.Success).data
      val teachers = (teachersState as UiState.Success).data
      val payments = (paymentsState as UiState.Success).data
      val structures = (structuresState as? UiState.Success)?.data ?: emptyList()
      val attendance = (attendanceState as UiState.Success).data
      val notices = (noticesState as? UiState.Success)?.data ?: emptyList()

      val activeStudents = remember(students) {
        students.filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
      }
      val feeDashboardSummary = remember(students, structures, payments, activeSession, currentAcademicMonthIndex, todayDisplayDate) {
        FeeLedgerUtils.computeDashboardSummary(
          students = students,
          structures = structures,
          payments = payments,
          academicSession = activeSession,
          currentMonthIndex = currentAcademicMonthIndex,
          todayDisplay = todayDisplayDate
        )
      }
      val pendingStudentCount = remember(students, structures, payments, activeSession) {
        activeStudents.count { student ->
          FeeLedgerUtils.buildStudentSessionSummary(student, structures, payments, activeSession).totalPending > 0L
        }
      }
      val totalOutstandingFees = feeDashboardSummary.totalPendingFees
      val todaysCollection = feeDashboardSummary.todaysCollection
      val thisMonthsCollection = feeDashboardSummary.thisMonthsCollection
      val totalFeeCollection = feeDashboardSummary.totalCollectedFees
      val distinctClassesCount = remember(students, structures, schoolSettings) {
        val activeDistinct = (students.map { it.className } + structures.map { it.className })
          .filter { it.isNotBlank() }
          .distinct()
          .size
        schoolSettings?.totalConfiguredClasses?.toInt() ?: activeDistinct
      }
      val todayAttendanceRecords = remember(attendance, todayKey) {
        attendance.filter { it.dateKey == todayKey }
      }
      val todayPresentCount = remember(todayAttendanceRecords) {
        todayAttendanceRecords.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.PRESENT }
      }
      val todayAttendanceDisplay = if (todayAttendanceRecords.isEmpty()) {
        "0%"
      } else {
        val pct = (todayPresentCount * 100) / todayAttendanceRecords.size
        "$pct%"
      }
      val todayAttendanceSub = if (todayAttendanceRecords.isEmpty()) {
        strings.notMarkedYet
      } else {
        "$todayPresentCount/${todayAttendanceRecords.size}"
      }

      val moduleCards = remember(strings) {
        listOf(
          ModuleNavItem(AdminDestination.STUDENTS, strings.studentManagement, Icons.Filled.Groups, RoyalBlue800, SkyBlue50, "nav_card_students"),
          ModuleNavItem(AdminDestination.TEACHERS, strings.teacherManagement, Icons.Filled.School, AzureBlue600, SkyBlue50, "nav_card_teachers"),
          ModuleNavItem(AdminDestination.FEES, strings.feesCollectionModule, Icons.AutoMirrored.Filled.ReceiptLong, EmeraldPaid, EmeraldPaidBg, "nav_card_fees"),
          ModuleNavItem(AdminDestination.ATTENDANCE, strings.attendanceModule, Icons.Filled.EventAvailable, RoyalBlue700, SkyBlue50, "nav_card_attendance"),
          ModuleNavItem(AdminDestination.HOMEWORK, strings.homeworkModule, Icons.AutoMirrored.Filled.MenuBook, AmberWarning, AmberWarningBg, "nav_card_homework"),
          ModuleNavItem(AdminDestination.EXAMINATION, strings.examinationModule, Icons.Filled.EditNote, RoyalBlue800, SkyBlue50, "nav_card_examination"),
          ModuleNavItem(AdminDestination.RESULTS, strings.resultsModule, Icons.AutoMirrored.Filled.FactCheck, EmeraldPaid, EmeraldPaidBg, "nav_card_results"),
          ModuleNavItem(AdminDestination.NOTICE_BOARD, strings.noticeBoardModule, Icons.Filled.Campaign, AmberWarning, AmberWarningBg, "nav_card_notices"),
          ModuleNavItem(AdminDestination.TIMETABLE, strings.timetableModule, Icons.Filled.Schedule, AzureBlue600, SkyBlue50, "nav_card_timetable"),
          ModuleNavItem(AdminDestination.REPORTS, strings.reportsModule, Icons.Filled.Assessment, RoyalBlue900, SkyBlue50, "nav_card_reports"),
          ModuleNavItem(AdminDestination.SETTINGS, strings.schoolSettingsModule, Icons.Filled.Settings, Slate700, SkyBlue50, "nav_card_settings")
        )
      }

      val recentStudents = remember(students) {
        students.sortedByDescending { it.createdAt?.seconds ?: 0L }.take(3)
      }

      LazyColumn(
        modifier = modifier
          .fillMaxSize()
          .background(OffWhiteBg)
          .testTag("admin_dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Welcome & Session Overview Card
        item {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, SkyBlue100),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${strings.welcomeAdmin} • ${adminDisplayName.ifBlank { strings.adminRole }}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${schoolSettings?.schoolName ?: strings.schoolName} • $activeSession ($currentMonthName)",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate500,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(50),
                color = SkyBlue50
              ) {
                Text(
                  text = "${notices.size} ${strings.notificationsLabel}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue800,
                  modifier = Modifier
                    .clickable { onNavigate(AdminDestination.NOTICE_BOARD, false) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        // 6 Real-Time Dashboard Statistics
        item {
          BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 700.dp) 3 else 2
            val kpis = listOf(
              StatCardData(strings.totalStudents, students.size.toString(), Icons.Filled.Groups, RoyalBlue800, SkyBlue50, "${activeStudents.size} ${strings.active}", "stat_total_students"),
              StatCardData(strings.totalTeachers, teachers.size.toString(), Icons.Filled.School, AzureBlue600, SkyBlue50, null, "stat_total_teachers"),
              StatCardData(strings.totalClasses, distinctClassesCount.toString(), Icons.Filled.Class, RoyalBlue700, SkyBlue50, null, "stat_total_classes"),
              StatCardData(strings.todaysAttendance, todayAttendanceDisplay, Icons.Filled.EventAvailable, EmeraldPaid, EmeraldPaidBg, todayAttendanceSub, "stat_todays_attendance"),
              StatCardData(strings.totalFeeCollection, formatInr(totalFeeCollection), Icons.Filled.AccountBalanceWallet, EmeraldPaid, EmeraldPaidBg, activeSession, "stat_total_fee_collection"),
              StatCardData(strings.pendingFees, formatInr(totalOutstandingFees), Icons.Filled.PendingActions, if (pendingStudentCount > 0) RoseDue else EmeraldPaid, if (pendingStudentCount > 0) RoseDueBg else EmeraldPaidBg, "$pendingStudentCount ${strings.dueCount}", "stat_pending_fees")
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              kpis.chunked(columns).forEach { rowItems ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  rowItems.forEach { stat ->
                    SummaryKpiCard(
                      label = stat.label,
                      value = stat.value,
                      icon = stat.icon,
                      accentColor = stat.accent,
                      iconBg = stat.iconBg,
                      supportingText = stat.supporting,
                      modifier = Modifier
                        .weight(1f)
                        .testTag(stat.tag)
                    )
                  }
                  if (rowItems.size < columns) {
                    repeat(columns - rowItems.size) {
                      Spacer(modifier = Modifier.weight(1f))
                    }
                  }
                }
              }
            }
          }
        }

        // Fees Collection Quick Summary Card
        item {
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = RoyalBlue900,
            shadowElevation = 4.dp,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("fees_quick_summary_card")
          ) {
            Column(
              modifier = Modifier
                .background(Brush.horizontalGradient(listOf(RoyalBlue900, RoyalBlue700)))
                .padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = strings.feesQuickSummary,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = PureWhite
                )
                Button(
                  onClick = { onNavigate(AdminDestination.FEES, false) },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = RoyalBlue900
                  ),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("summary_collect_fees_btn")
                ) {
                  Text(strings.collectFee, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                FeeSummaryPill(
                  label = strings.todaysCollection,
                  value = formatInr(todaysCollection),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("summary_todays_collection")
                )
                FeeSummaryPill(
                  label = strings.thisMonthsCollection,
                  value = formatInr(thisMonthsCollection),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("summary_month_collection")
                )
                FeeSummaryPill(
                  label = strings.totalOutstandingFees,
                  value = formatInr(totalOutstandingFees),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("summary_outstanding_fees")
                )
              }
            }
          }
        }

        // 11 Functional School Navigation Cards
        item {
          Text(
            text = strings.schoolModulesTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          Spacer(modifier = Modifier.height(10.dp))
          BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 700.dp) 3 else 2
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              moduleCards.chunked(columns).forEach { rowCards ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  rowCards.forEach { card ->
                    ModuleNavigationCard(
                      item = card,
                      onClick = { onNavigate(card.destination, false) },
                      modifier = Modifier.weight(1f)
                    )
                  }
                  if (rowCards.size < columns) {
                    repeat(columns - rowCards.size) {
                      Spacer(modifier = Modifier.weight(1f))
                    }
                  }
                }
              }
            }
          }
        }

        // Recent Activities Header
        item {
          Text(
            text = strings.recentActivitiesTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
        }

        // 1. Recently Added Students
        item {
          ActivitySectionHeader(
            title = strings.recentlyAddedStudents,
            actionLabel = strings.viewAll,
            onAction = { onNavigate(AdminDestination.STUDENTS, false) }
          )
        }
        if (recentStudents.isEmpty()) {
          item {
            EmptyStateCard(
              icon = Icons.Outlined.School,
              title = strings.noStudentsFound,
              description = strings.noRecentStudents,
              actionLabel = strings.addNewStudent,
              onAction = { onNavigate(AdminDestination.STUDENTS, true) }
            )
          }
        } else {
          items(recentStudents, key = { "recent_student_${it.id}" }) { student ->
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = PureWhite,
              border = BorderStroke(1.dp, Slate200),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigate(AdminDestination.STUDENTS, false) }
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SkyBlue50),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = student.fullName,
                    tint = RoyalBlue800
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${student.className} • ${strings.admissionNo}: ${student.admissionNo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Text(
                  text = formatInr(student.monthlyFeeAmount),
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue800
                )
              }
            }
          }
        }

        // 2. Recent Fee Payments
        item {
          ActivitySectionHeader(
            title = strings.recentReceipts,
            actionLabel = strings.viewAll,
            onAction = { onNavigate(AdminDestination.FEES, false) }
          )
        }
        if (payments.isEmpty()) {
          item {
            EmptyStateCard(
              icon = Icons.Outlined.Receipt,
              title = strings.recentReceipts,
              description = strings.noReceiptsYet,
              actionLabel = strings.collectMonthlyFees,
              onAction = { onNavigate(AdminDestination.FEES, false) }
            )
          }
        } else {
          items(payments.take(4), key = { "recent_payment_${it.id}" }) { payment ->
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = PureWhite,
              border = BorderStroke(1.dp, Slate200),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onReceiptClick(payment) }
                .testTag("dashboard_receipt_${payment.id}")
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmeraldPaidBg),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = payment.receiptNo,
                    tint = EmeraldPaid
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = payment.studentName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${payment.className} • ${payment.monthName} • ${paymentModeLabel(payment.paymentMode, strings)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  val paidDisplay = if (payment.amountPaid > 0L) payment.amountPaid else payment.totalAmount
                  Text(
                    text = formatInr(paidDisplay),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (payment.isVoided()) RoseDue else EmeraldPaid
                  )
                  Text(
                    text = payment.paidOnDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                  )
                }
              }
            }
          }
        }

        // 3. Recent Attendance
        item {
          ActivitySectionHeader(
            title = strings.recentAttendanceTitle,
            actionLabel = strings.viewAll,
            onAction = { onNavigate(AdminDestination.ATTENDANCE, false) }
          )
        }
        if (attendance.isEmpty()) {
          item {
            EmptyStateCard(
              icon = Icons.Outlined.EventBusy,
              title = strings.recentAttendanceTitle,
              description = strings.noRecentAttendance,
              actionLabel = strings.attendanceModule,
              onAction = { onNavigate(AdminDestination.ATTENDANCE, false) }
            )
          }
        } else {
          items(attendance.take(4), key = { "recent_att_${it.id}" }) { record ->
            val st = AttendanceStatus.fromRaw(record.status)
            val (badgeColor, badgeBg, badgeLabel) = when (st) {
              AttendanceStatus.PRESENT -> Triple(EmeraldPaid, EmeraldPaidBg, strings.presentLabel)
              AttendanceStatus.ABSENT -> Triple(RoseDue, RoseDueBg, strings.absentLabel)
              AttendanceStatus.LEAVE -> Triple(AmberWarning, AmberWarningBg, strings.leaveLabel)
            }
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = PureWhite,
              border = BorderStroke(1.dp, Slate200),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigate(AdminDestination.ATTENDANCE, false) }
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Outlined.PersonOutline,
                    contentDescription = record.studentName,
                    tint = badgeColor
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = record.studentName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${record.className} • ${record.dateKey}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                  )
                }
                Surface(shape = RoundedCornerShape(50), color = badgeBg) {
                  Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
      }
    }
  }
}

private data class StatCardData(
  val label: String,
  val value: String,
  val icon: ImageVector,
  val accent: Color,
  val iconBg: Color,
  val supporting: String?,
  val tag: String
)

@Composable
private fun DashboardErrorView(title: String, message: String, modifier: Modifier) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(OffWhiteBg)
      .padding(20.dp)
      .testTag("admin_dashboard_error"),
    contentAlignment = Alignment.Center
  ) {
    EmptyStateCard(
      icon = Icons.Outlined.School,
      title = title,
      description = message
    )
  }
}

@Composable
private fun FeeSummaryPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = PureWhite.copy(alpha = 0.14f),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = PureWhite,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = PureWhite.copy(alpha = 0.85f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun ModuleNavigationCard(
  item: ModuleNavItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = PureWhite,
    border = BorderStroke(1.dp, SkyBlue100),
    shadowElevation = 2.dp,
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .minimumInteractiveComponentSize()
      .testTag(item.testTag)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(item.iconBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = item.icon,
          contentDescription = item.title,
          tint = item.accentColor,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = item.title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Slate900,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun ActivitySectionHeader(
  title: String,
  actionLabel: String,
  onAction: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = Slate700
    )
    TextButton(onClick = onAction) {
      Text(actionLabel, color = RoyalBlue700)
    }
  }
}
