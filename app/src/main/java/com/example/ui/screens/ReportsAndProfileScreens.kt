package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ExamResult
import com.example.data.model.FeePayment
import com.example.data.model.Gender
import com.example.data.model.PaymentMode
import com.example.data.model.StudentRecord
import com.example.data.model.TeacherRecord
import com.example.data.model.UiState
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.formatInr
import com.example.ui.components.paymentModeLabel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue100
import com.example.ui.theme.SkyBlue50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun ReportsScreen(
  strings: AppStrings,
  studentsState: UiState<List<StudentRecord>>,
  teachersState: UiState<List<TeacherRecord>>,
  paymentsState: UiState<List<FeePayment>>,
  attendanceState: UiState<List<AttendanceRecord>>,
  resultsState: UiState<List<ExamResult>>,
  modifier: Modifier = Modifier
) {
  if (studentsState is UiState.Loading || paymentsState is UiState.Loading) {
    Box(modifier.fillMaxSize().background(OffWhiteBg), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = RoyalBlue800)
    }
    return
  }

  val students = (studentsState as? UiState.Success)?.data ?: emptyList()
  val teachers = (teachersState as? UiState.Success)?.data ?: emptyList()
  val payments = (paymentsState as? UiState.Success)?.data ?: emptyList()
  val attendance = (attendanceState as? UiState.Success)?.data ?: emptyList()
  val results = (resultsState as? UiState.Success)?.data ?: emptyList()

  val activePayments = remember(payments) { payments.filter { !it.isVoided() } }
  val totalCollected = remember(activePayments) { activePayments.sumOf { it.effectivePaidAmount() } }
  val modeTotals = remember(activePayments) {
    PaymentMode.entries.map { mode ->
      mode to activePayments.filter { PaymentMode.fromRaw(it.paymentMode) == mode }.sumOf { it.effectivePaidAmount() }
    }
  }
  val maleCount = remember(students) { students.count { Gender.fromRaw(it.gender) == Gender.MALE } }
  val femaleCount = remember(students) { students.count { Gender.fromRaw(it.gender) == Gender.FEMALE } }
  val otherCount = remember(students) { students.count { Gender.fromRaw(it.gender) == Gender.OTHER } }

  val presentCount = remember(attendance) { attendance.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.PRESENT } }
  val absentCount = remember(attendance) { attendance.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.ABSENT } }
  val leaveCount = remember(attendance) { attendance.count { AttendanceStatus.fromRaw(it.status) == AttendanceStatus.LEAVE } }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(OffWhiteBg)
      .testTag("reports_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = strings.reportsSummaryTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = RoyalBlue900
      )
    }

    item {
      Surface(shape = RoundedCornerShape(18.dp), color = PureWhite, border = BorderStroke(1.dp, SkyBlue100), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(strings.feeModeBreakdown, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue900)
          Text("${strings.totalFeeCollection}: ${formatInr(totalCollected)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPaid)
          modeTotals.forEach { (mode, amt) ->
            val ratio = if (totalCollected > 0L) amt.toFloat() / totalCollected.toFloat() else 0f
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(paymentModeLabel(mode.name, strings), style = MaterialTheme.typography.bodyMedium, color = Slate700)
                Text(formatInr(amt), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Slate900)
              }
              LinearProgressIndicator(
                progress = { ratio },
                color = RoyalBlue800,
                trackColor = SkyBlue100,
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50))
              )
            }
          }
        }
      }
    }

    item {
      Surface(shape = RoundedCornerShape(18.dp), color = PureWhite, border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(strings.genderBreakdown, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue900)
          ReportMetricRow(strings.totalStudents, students.size.toString())
          ReportMetricRow(strings.male, maleCount.toString())
          ReportMetricRow(strings.female, femaleCount.toString())
          if (otherCount > 0) ReportMetricRow(strings.other, otherCount.toString())
          HorizontalDivider(color = Slate200)
          ReportMetricRow(strings.totalTeachers, teachers.size.toString())
          ReportMetricRow(strings.resultsModule, results.size.toString())
        }
      }
    }

    item {
      Surface(shape = RoundedCornerShape(18.dp), color = PureWhite, border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(strings.attendanceSummaryLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue900)
          if (attendance.isEmpty()) {
            EmptyStateCard(Icons.Filled.Assessment, strings.attendanceSummaryLabel, strings.noRecentAttendance)
          } else {
            ReportMetricRow(strings.presentLabel, presentCount.toString(), EmeraldPaid)
            ReportMetricRow(strings.absentLabel, absentCount.toString(), RoseDue)
            ReportMetricRow(strings.leaveLabel, leaveCount.toString(), AmberWarning)
          }
        }
      }
    }
  }
}

@Composable
private fun ReportMetricRow(
  label: String,
  value: String,
  valueColor: androidx.compose.ui.graphics.Color = Slate900
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = Slate500)
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
  }
}

@Composable
fun AdminProfileScreen(
  strings: AppStrings,
  adminDisplayName: String,
  currentUserId: String,
  academicSession: String,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onOpenSettings: () -> Unit,
  onSwitchRole: () -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OffWhiteBg)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("admin_profile_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Surface(shape = RoundedCornerShape(22.dp), color = PureWhite, border = BorderStroke(1.dp, SkyBlue100), modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier.size(68.dp).clip(CircleShape).background(SkyBlue50),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Filled.AccountCircle, contentDescription = strings.adminProfileTitle, tint = RoyalBlue800, modifier = Modifier.size(44.dp))
        }
        Text(adminDisplayName.ifBlank { strings.adminRole }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalBlue900)
        Text("${strings.schoolName} • $academicSession", style = MaterialTheme.typography.bodySmall, color = Slate500)
        Surface(shape = RoundedCornerShape(50), color = SkyBlue50) {
          Text(
            text = "${strings.accountUidLabel}: ${currentUserId.take(12)}...",
            style = MaterialTheme.typography.labelSmall,
            color = RoyalBlue800,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          )
        }
      }
    }

    Surface(shape = RoundedCornerShape(18.dp), color = PureWhite, border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text(strings.languageSettingLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Slate700)
          LanguageTogglePill(language = language, onLanguageChange = onLanguageChange, onDarkBackground = false)
        }

        OutlinedButton(
          onClick = onOpenSettings,
          modifier = Modifier.fillMaxWidth().testTag("profile_open_settings"),
          border = BorderStroke(1.dp, RoyalBlue800)
        ) {
          Icon(Icons.Filled.Settings, contentDescription = strings.schoolSettingsModule, tint = RoyalBlue800)
          Spacer(Modifier.width(8.dp))
          Text(strings.schoolSettingsModule, color = RoyalBlue800)
        }

        OutlinedButton(
          onClick = onSwitchRole,
          modifier = Modifier.fillMaxWidth().testTag("profile_switch_role"),
          border = BorderStroke(1.dp, RoyalBlue800)
        ) {
          Icon(Icons.Outlined.SwapHoriz, contentDescription = strings.switchPortal, tint = RoyalBlue800)
          Spacer(Modifier.width(8.dp))
          Text(strings.switchPortal, color = RoyalBlue800)
        }

        Button(
          onClick = onSignOut,
          colors = ButtonDefaults.buttonColors(containerColor = RoseDue),
          modifier = Modifier.fillMaxWidth().testTag("profile_sign_out")
        ) {
          Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = strings.signOut)
          Spacer(Modifier.width(8.dp))
          Text(strings.signOut)
        }
      }
    }
  }
}
