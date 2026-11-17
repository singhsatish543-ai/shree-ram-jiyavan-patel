package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeePayment
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.UiState
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SummaryKpiCard
import com.example.ui.components.formatInr
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
import com.example.ui.theme.Slate900
import java.util.Calendar

@Composable
fun TeacherPortalScreen(
  strings: AppStrings,
  teacherName: String,
  initialAssignedClass: String,
  studentsState: UiState<List<StudentRecord>>,
  paymentsState: UiState<List<FeePayment>>,
  attendanceState: UiState<List<AttendanceRecord>> = UiState.Success(emptyList()),
  isSaving: Boolean = false,
  activeSession: String = "2026-27",
  onMarkAttendance: (StudentRecord, String, AttendanceStatus) -> Unit = { _, _, _ -> },
  onMarkAllPresent: (List<StudentRecord>, String) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  var teacherTabIndex by remember { mutableIntStateOf(0) }
  var selectedClass by remember(initialAssignedClass) {
    mutableStateOf(initialAssignedClass.ifBlank { null })
  }
  val currentAcademicMonthIndex = remember {
    val calMonth = Calendar.getInstance().get(Calendar.MONTH)
    if (calMonth >= Calendar.APRIL) (calMonth - Calendar.APRIL + 1) else (calMonth + 10)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OffWhiteBg)
  ) {
    Surface(color = PureWhite, shadowElevation = 2.dp) {
      TabRow(
        selectedTabIndex = teacherTabIndex,
        containerColor = PureWhite,
        contentColor = RoyalBlue800
      ) {
        Tab(
          selected = teacherTabIndex == 0,
          onClick = { teacherTabIndex = 0 },
          text = {
            Text(
              text = strings.teacherAttendanceTab,
              fontWeight = if (teacherTabIndex == 0) FontWeight.Bold else FontWeight.Medium
            )
          },
          modifier = Modifier.testTag("teacher_tab_attendance")
        )
        Tab(
          selected = teacherTabIndex == 1,
          onClick = { teacherTabIndex = 1 },
          text = {
            Text(
              text = strings.teacherRosterTab,
              fontWeight = if (teacherTabIndex == 1) FontWeight.Bold else FontWeight.Medium
            )
          },
          modifier = Modifier.testTag("teacher_tab_roster")
        )
      }
    }

    if (teacherTabIndex == 0) {
      AttendanceScreen(
        strings = strings,
        studentsState = studentsState,
        attendanceState = attendanceState,
        isSaving = isSaving,
        isAdmin = false,
        initialClass = initialAssignedClass,
        activeSession = activeSession,
        onMarkAttendance = onMarkAttendance,
        onMarkAllPresent = onMarkAllPresent,
        modifier = Modifier.weight(1f).fillMaxWidth()
      )
      return@Column
    }

    when {
      studentsState is UiState.Loading || paymentsState is UiState.Loading -> {
        Box(modifier = Modifier.weight(1f).fillMaxSize().background(OffWhiteBg), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = RoyalBlue800)
        }
      }

      studentsState is UiState.Error -> {
        Box(modifier = Modifier.weight(1f).fillMaxSize().background(OffWhiteBg).padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(
            icon = Icons.Outlined.School,
            title = strings.errorLoadingData,
            description = studentsState.message
          )
        }
      }

      else -> {
        val students = (studentsState as UiState.Success).data
        val payments = (paymentsState as? UiState.Success)?.data ?: emptyList()
        val paidIdsThisMonth = payments
          .filter { !it.isVoided() && it.monthIndex.toInt() == currentAcademicMonthIndex && it.academicYear == "2026-27" }
          .map { it.studentId }
          .toSet()

        val filtered = remember(students, selectedClass) {
          students.filter { selectedClass == null || it.className == selectedClass }
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .background(OffWhiteBg)
        ) {
        Surface(color = PureWhite, shadowElevation = 2.dp) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "${strings.teacherPortalTitle} • $teacherName",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = RoyalBlue900
            )
            Text(
              text = strings.teacherPortalSubtitle,
              style = MaterialTheme.typography.bodySmall,
              color = Slate500
            )
            Row(
              modifier = Modifier.horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              FilterChip(
                selected = selectedClass == null,
                onClick = { selectedClass = null },
                label = { Text(strings.allClasses) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = RoyalBlue800,
                  selectedLabelColor = PureWhite
                )
              )
              strings.classOptions.forEach { cls ->
                FilterChip(
                  selected = selectedClass == cls,
                  onClick = { selectedClass = if (selectedClass == cls) null else cls },
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

        if (filtered.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            EmptyStateCard(
              icon = Icons.Filled.Badge,
              title = strings.noStudentsFound,
              description = strings.noStudentsOverview
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryKpiCard(
                  label = strings.totalStudents,
                  value = filtered.size.toString(),
                  icon = Icons.Filled.Groups,
                  accentColor = RoyalBlue800,
                  iconBg = SkyBlue50,
                  modifier = Modifier.weight(1f)
                )
                val paidCount = filtered.count { it.id in paidIdsThisMonth }
                SummaryKpiCard(
                  label = "${strings.paidCount} (${strings.months[currentAcademicMonthIndex - 1]})",
                  value = "$paidCount / ${filtered.size}",
                  icon = Icons.Filled.CheckCircle,
                  accentColor = EmeraldPaid,
                  iconBg = EmeraldPaidBg,
                  modifier = Modifier.weight(1f)
                )
              }
            }

            items(filtered, key = { it.id }) { student ->
              val isPaid = student.id in paidIdsThisMonth
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = student.fullName,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = Slate900
                    )
                    Text(
                      text = "${student.className} (${student.section}) • ${strings.rollNumber}: ${student.rollNumber} • ${student.admissionNo}",
                      style = MaterialTheme.typography.bodySmall,
                      color = Slate500
                    )
                    Text(
                      text = "${strings.fatherName}: ${student.fatherName}",
                      style = MaterialTheme.typography.bodySmall,
                      color = Slate500
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isPaid) EmeraldPaidBg else RoseDueBg
                  ) {
                    Text(
                      text = if (isPaid) strings.paidBadge else strings.dueBadge,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (isPaid) EmeraldPaid else RoseDue,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
}
}

@Composable
fun StudentPortalScreen(
  strings: AppStrings,
  initialAdmissionNo: String,
  studentsState: UiState<List<StudentRecord>>,
  paymentsState: UiState<List<FeePayment>>,
  onViewReceipt: (FeePayment) -> Unit,
  modifier: Modifier = Modifier
) {
  var lookupQuery by remember(initialAdmissionNo) { mutableStateOf(initialAdmissionNo) }

  when {
    studentsState is UiState.Loading || paymentsState is UiState.Loading -> {
      Box(modifier = modifier.fillMaxSize().background(OffWhiteBg), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = RoyalBlue800)
      }
    }

    studentsState is UiState.Error -> {
      Box(modifier = modifier.fillMaxSize().background(OffWhiteBg).padding(20.dp), contentAlignment = Alignment.Center) {
        EmptyStateCard(
          icon = Icons.Outlined.School,
          title = strings.errorLoadingData,
          description = studentsState.message
        )
      }
    }

    else -> {
      val students = (studentsState as UiState.Success).data
      val payments = (paymentsState as? UiState.Success)?.data ?: emptyList()

      val matchedStudent = remember(students, lookupQuery) {
        val q = lookupQuery.trim().lowercase()
        if (q.isEmpty()) students.firstOrNull()
        else students.firstOrNull {
          it.admissionNo.lowercase() == q ||
            it.admissionNo.lowercase().contains(q) ||
            it.fullName.lowercase().contains(q)
        }
      }

      Column(
        modifier = modifier
          .fillMaxSize()
          .background(OffWhiteBg)
      ) {
        Surface(color = PureWhite, shadowElevation = 2.dp) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = strings.studentPortalTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = RoyalBlue900
            )
            Text(
              text = strings.studentPortalSubtitle,
              style = MaterialTheme.typography.bodySmall,
              color = Slate500
            )
            OutlinedTextField(
              value = lookupQuery,
              onValueChange = { lookupQuery = it },
              placeholder = { Text(strings.enterAdmissionLookup) },
              leadingIcon = { Icon(Icons.Filled.Search, contentDescription = strings.enterAdmissionLookup, tint = RoyalBlue700) },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = schoolTextFieldColors(),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("student_portal_search_input")
            )
          }
        }

        if (matchedStudent == null) {
          Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            EmptyStateCard(
              icon = Icons.Outlined.School,
              title = strings.noStudentsFound,
              description = strings.noMatchingStudent
            )
          }
        } else {
          val studentPaymentsByMonth = remember(payments, matchedStudent) {
            payments.filter { !it.isVoided() && it.studentId == matchedStudent.id && it.academicYear == "2026-27" }
              .associateBy { it.monthIndex.toInt() }
          }

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, SkyBlue100),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = matchedStudent.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue900
                  )
                  Text(
                    text = "${matchedStudent.className} (${matchedStudent.section}) • ${strings.admissionNo}: ${matchedStudent.admissionNo} • ${strings.rollNumber}: ${matchedStudent.rollNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RoyalBlue700
                  )
                  Text(
                    text = "${strings.fatherName}: ${matchedStudent.fatherName} • ${strings.monthlyFee}: ${formatInr(matchedStudent.monthlyFeeAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                  )
                }
              }
            }

            item {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${strings.feeHistoryTitle} (${strings.academicSessionLabel})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
            }

            items(strings.months.indices.toList(), key = { it }) { idx ->
              val monthNum = idx + 1
              val monthName = strings.months[idx]
              val payment = studentPaymentsByMonth[monthNum]
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, if (payment != null) EmeraldPaid.copy(alpha = 0.35f) else Slate200),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = monthName,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = Slate900
                    )
                    if (payment != null) {
                      Text(
                        text = "${payment.receiptNo} • ${payment.paidOnDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                      )
                    } else {
                      Text(
                        text = formatInr(matchedStudent.monthlyFeeAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = RoseDue
                      )
                    }
                  }

                  if (payment != null) {
                    OutlinedButton(
                      onClick = { onViewReceipt(payment) },
                      border = BorderStroke(1.dp, EmeraldPaid)
                    ) {
                      Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = strings.viewReceipt, tint = EmeraldPaid, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("${formatInr(payment.effectivePaidAmount())} • ${strings.viewReceipt}", color = EmeraldPaid)
                    }
                  } else {
                    Surface(shape = RoundedCornerShape(50), color = RoseDueBg) {
                      Text(
                        text = strings.dueBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoseDue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
  }
}
