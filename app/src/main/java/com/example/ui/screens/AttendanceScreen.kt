package com.example.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentRecord
import com.example.data.model.UiState
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
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
import com.example.util.AttendanceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
  strings: AppStrings,
  studentsState: UiState<List<StudentRecord>>,
  attendanceState: UiState<List<AttendanceRecord>>,
  isSaving: Boolean,
  isAdmin: Boolean = true,
  initialClass: String = "",
  activeSession: String = "2026-27",
  onMarkAttendance: (StudentRecord, String, AttendanceStatus) -> Unit,
  onMarkAllPresent: (List<StudentRecord>, String) -> Unit,
  onEditAttendance: (AttendanceRecord, AttendanceStatus, String) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier
) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  var selectedClass by remember(initialClass, strings.allClasses) {
    mutableStateOf(
      if (initialClass.isNotBlank() && initialClass in strings.classOptions) initialClass else strings.allClasses
    )
  }
  var selectedSection by remember(strings.allSections) { mutableStateOf(strings.allSections) }
  val todayKey = remember { AttendanceUtils.todayDateKey() }
  var selectedDateKey by remember { mutableStateOf(todayKey) }
  var showDatePicker by remember { mutableStateOf(false) }

  var editTargetRecord by remember { mutableStateOf<AttendanceRecord?>(null) }
  var editInitialStatus by remember { mutableStateOf(AttendanceStatus.PRESENT) }

  val allStudents = (studentsState as? UiState.Success)?.data ?: emptyList()
  val allAttendanceRecords = (attendanceState as? UiState.Success)?.data ?: emptyList()
  val sectionOptions = remember(allStudents, strings.allSections) {
    AttendanceUtils.availableSections(allStudents, strings.allSections)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OffWhiteBg)
      .testTag("attendance_screen")
  ) {
    Surface(color = PureWhite, shadowElevation = 2.dp) {
      Column(
        modifier = Modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Title & Date selector row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.attendanceModule,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = RoyalBlue900
            )
            Text(
              text = "${strings.session}: $activeSession",
              style = MaterialTheme.typography.labelSmall,
              color = Slate500
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
              onClick = { selectedDateKey = AttendanceUtils.shiftDateKey(selectedDateKey, -1) },
              modifier = Modifier.testTag("attendance_prev_day_btn")
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Day", tint = RoyalBlue800)
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = SkyBlue50,
              border = BorderStroke(1.dp, SkyBlue100),
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { showDatePicker = true }
                .minimumInteractiveComponentSize()
                .testTag("attendance_date_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = strings.pickDateLabel, tint = RoyalBlue800, modifier = Modifier.size(14.dp))
                Text(
                  text = AttendanceUtils.formatDisplayDate(selectedDateKey),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue900
                )
              }
            }

            IconButton(
              enabled = selectedDateKey < todayKey,
              onClick = {
                val next = AttendanceUtils.shiftDateKey(selectedDateKey, 1)
                if (next <= todayKey) selectedDateKey = next
              },
              modifier = Modifier.testTag("attendance_next_day_btn")
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day", tint = RoyalBlue800)
            }
          }
        }

        if (selectedDateKey != todayKey) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(
              onClick = { selectedDateKey = todayKey },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              modifier = Modifier.testTag("attendance_today_btn")
            ) {
              Text(strings.todayLabel, color = RoyalBlue800, fontWeight = FontWeight.Bold)
            }
          }
        }

        // 4 Tabs: Mark Daily, Daily Report, Monthly Report, Student-wise
        val tabs = listOf(
          strings.tabMarkAttendance,
          strings.tabDailyReport,
          strings.tabMonthlyReport,
          strings.tabStudentWiseReport
        )
        ScrollableTabRow(
          selectedTabIndex = selectedTabIndex,
          containerColor = PureWhite,
          contentColor = RoyalBlue800,
          edgePadding = 12.dp
        ) {
          tabs.forEachIndexed { idx, label ->
            Tab(
              selected = selectedTabIndex == idx,
              onClick = { selectedTabIndex = idx },
              text = {
                Text(
                  text = label,
                  fontWeight = if (selectedTabIndex == idx) FontWeight.Bold else FontWeight.Medium,
                  maxLines = 1
                )
              },
              modifier = Modifier.testTag("att_tab_$idx")
            )
          }
        }

        // Class Filter Row
        Row(
          modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(strings.allClasses).plus(strings.classOptions).forEachIndexed { idx, cls ->
            val selected = selectedClass == cls
            FilterChip(
              selected = selected,
              onClick = { selectedClass = cls },
              label = { Text(cls) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalBlue800,
                selectedLabelColor = PureWhite
              ),
              modifier = Modifier.testTag("att_class_chip_$idx")
            )
          }
        }

        // Section Filter Row
        Row(
          modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${strings.section}:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Slate700
          )
          sectionOptions.forEach { sec ->
            val selected = selectedSection == sec
            FilterChip(
              selected = selected,
              onClick = { selectedSection = sec },
              label = { Text(sec) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalBlue700,
                selectedLabelColor = PureWhite
              ),
              modifier = Modifier.testTag("att_section_chip_$sec")
            )
          }
        }
      }
    }

    when {
      studentsState is UiState.Loading || attendanceState is UiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = RoyalBlue800)
        }
      }

      studentsState is UiState.Error -> {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(
            icon = Icons.Outlined.EventAvailable,
            title = strings.errorLoadingData,
            description = studentsState.message
          )
        }
      }

      attendanceState is UiState.Error -> {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(
            icon = Icons.Outlined.EventAvailable,
            title = strings.errorLoadingData,
            description = attendanceState.message
          )
        }
      }

      else -> {
        when (selectedTabIndex) {
          0 -> MarkDailyAttendanceTab(
            strings = strings,
            students = allStudents,
            allRecords = allAttendanceRecords,
            selectedDateKey = selectedDateKey,
            selectedClass = selectedClass,
            selectedSection = selectedSection,
            isSaving = isSaving,
            isAdmin = isAdmin,
            onMarkAttendance = onMarkAttendance,
            onMarkAllPresent = onMarkAllPresent,
            onOpenEditDialog = { rec, preferredStatus ->
              editTargetRecord = rec
              editInitialStatus = preferredStatus ?: AttendanceStatus.fromRaw(rec.status)
            }
          )

          1 -> DailyAttendanceReportTab(
            strings = strings,
            students = allStudents,
            allRecords = allAttendanceRecords,
            selectedDateKey = selectedDateKey,
            selectedClass = selectedClass,
            selectedSection = selectedSection,
            isAdmin = isAdmin,
            onOpenEditDialog = { rec, preferredStatus ->
              editTargetRecord = rec
              editInitialStatus = preferredStatus ?: AttendanceStatus.fromRaw(rec.status)
            }
          )

          2 -> MonthlyAttendanceReportTab(
            strings = strings,
            students = allStudents,
            allRecords = allAttendanceRecords,
            selectedClass = selectedClass,
            selectedSection = selectedSection
          )

          else -> StudentWiseAttendanceReportTab(
            strings = strings,
            students = allStudents,
            allRecords = allAttendanceRecords,
            selectedClass = selectedClass,
            selectedSection = selectedSection,
            isAdmin = isAdmin,
            onOpenEditDialog = { rec, preferredStatus ->
              editTargetRecord = rec
              editInitialStatus = preferredStatus ?: AttendanceStatus.fromRaw(rec.status)
            }
          )
        }
      }
    }
  }

  if (showDatePicker) {
    val todayUtcMillis = remember(todayKey) {
      AttendanceUtils.dateKeyToUtcMillis(todayKey) ?: System.currentTimeMillis()
    }
    val initialMillis = remember(selectedDateKey) {
      AttendanceUtils.dateKeyToUtcMillis(selectedDateKey) ?: todayUtcMillis
    }
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = initialMillis,
      selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayUtcMillis
      }
    )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { millis ->
              val picked = AttendanceUtils.millisToDateKey(millis)
              if (picked <= todayKey) selectedDateKey = picked
            }
            showDatePicker = false
          }
        ) {
          Text(strings.save, color = RoyalBlue800, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text(strings.cancel)
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  val targetEdit = editTargetRecord
  if (targetEdit != null && isAdmin) {
    AdminEditAttendanceDialog(
      record = targetEdit,
      initialStatus = editInitialStatus,
      strings = strings,
      isSaving = isSaving,
      onDismiss = { editTargetRecord = null },
      onSave = { newStatus, reason ->
        onEditAttendance(targetEdit, newStatus, reason)
        editTargetRecord = null
      }
    )
  }
}

@Composable
private fun MarkDailyAttendanceTab(
  strings: AppStrings,
  students: List<StudentRecord>,
  allRecords: List<AttendanceRecord>,
  selectedDateKey: String,
  selectedClass: String,
  selectedSection: String,
  isSaving: Boolean,
  isAdmin: Boolean,
  onMarkAttendance: (StudentRecord, String, AttendanceStatus) -> Unit,
  onMarkAllPresent: (List<StudentRecord>, String) -> Unit,
  onOpenEditDialog: (AttendanceRecord, AttendanceStatus?) -> Unit
) {
  val filteredStudents = remember(students, selectedClass, selectedSection, strings) {
    AttendanceUtils.filterActiveStudents(
      students = students,
      selectedClass = selectedClass,
      allClassesLabel = strings.allClasses,
      selectedSection = selectedSection,
      allSectionsLabel = strings.allSections
    )
  }

  val dayRecordsByStudent = remember(allRecords, selectedDateKey) {
    AttendanceUtils.deduplicateRecordsByStudentDate(allRecords)
      .filter { it.dateKey.trim() == selectedDateKey.trim() }
      .associateBy { it.studentId }
  }

  if (filteredStudents.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
      EmptyStateCard(
        icon = Icons.Outlined.EventAvailable,
        title = strings.noStudentsFound,
        description = strings.noStudentsHint
      )
    }
    return
  }

  val unmarkedStudents = remember(filteredStudents, dayRecordsByStudent) {
    filteredStudents.filter { dayRecordsByStudent[it.id] == null }
  }
  val presentCount = filteredStudents.count {
    dayRecordsByStudent[it.id]?.let { rec -> AttendanceStatus.fromRaw(rec.status) == AttendanceStatus.PRESENT } == true
  }
  val absentCount = filteredStudents.count {
    dayRecordsByStudent[it.id]?.let { rec -> AttendanceStatus.fromRaw(rec.status) == AttendanceStatus.ABSENT } == true
  }
  val leaveCount = filteredStudents.count {
    dayRecordsByStudent[it.id]?.let { rec -> AttendanceStatus.fromRaw(rec.status) == AttendanceStatus.LEAVE } == true
  }
  val markedCount = filteredStudents.size - unmarkedStudents.size
  val dailyPct = AttendanceUtils.calculateAttendancePercentage(presentCount, markedCount)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("mark_daily_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SkyBlue100),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("mark_daily_summary_card")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "${strings.presentLabel}: $presentCount • ${strings.absentLabel}: $absentCount • ${strings.leaveLabel}: $leaveCount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "${strings.markedCountLabel}: $markedCount/${filteredStudents.size} • ${strings.unmarkedCountLabel}: ${unmarkedStudents.size}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
            AttendancePercentBadge(
              percent = dailyPct,
              totalMarked = markedCount,
              modifier = Modifier.testTag("mark_daily_percent_badge")
            )
          }

          Button(
            enabled = !isSaving && unmarkedStudents.isNotEmpty(),
            onClick = { onMarkAllPresent(unmarkedStudents, selectedDateKey) },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPaid),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mark_all_present_button")
          ) {
            Icon(Icons.Filled.DoneAll, contentDescription = strings.markAllPresent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("${strings.markAllPresent} (${unmarkedStudents.size})")
          }
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SkyBlue50,
        border = BorderStroke(1.dp, SkyBlue100),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("duplicate_guard_banner")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.VerifiedUser,
            contentDescription = null,
            tint = RoyalBlue800,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = if (isAdmin) strings.duplicateAttendanceBanner
            else "${strings.duplicateAttendanceBanner} ${strings.adminEditOnlyHint}",
            style = MaterialTheme.typography.labelSmall,
            color = RoyalBlue900
          )
        }
      }
    }

    items(filteredStudents, key = { it.id }) { student ->
      val existingRecord = dayRecordsByStudent[student.id]
      val currentStatus = existingRecord?.status?.let { AttendanceStatus.fromRaw(it) }
      val isAlreadyMarked = existingRecord != null

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, if (isAlreadyMarked) SkyBlue100 else Slate200),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("att_student_card_${student.id}")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = student.fullName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${student.className} (${student.section}) • ${strings.rollNumber}: ${student.rollNumber} • ${student.admissionNo}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
              if (existingRecord != null) {
                Text(
                  text = buildString {
                    append("${strings.markedByLabel}: ${existingRecord.markedBy}")
                    if (existingRecord.editedBy.isNotBlank()) {
                      append(" • ${strings.editedByLabel}: ${existingRecord.editedBy}")
                    }
                  },
                  style = MaterialTheme.typography.labelSmall,
                  color = RoyalBlue800
                )
              }
            }

            if (existingRecord == null) {
              Surface(
                shape = RoundedCornerShape(50),
                color = OffWhiteBg,
                modifier = Modifier.testTag("att_unmarked_badge_${student.id}")
              ) {
                Text(
                  text = strings.notMarkedYet,
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate500,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(50),
                  color = SkyBlue50,
                  border = BorderStroke(1.dp, SkyBlue100),
                  modifier = Modifier.testTag("att_marked_badge_${student.id}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(12.dp))
                    Text(
                      text = strings.markedLockedLabel,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = RoyalBlue800
                    )
                  }
                }
                if (isAdmin) {
                  OutlinedButton(
                    enabled = !isSaving,
                    onClick = { onOpenEditDialog(existingRecord, currentStatus) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, RoyalBlue800),
                    modifier = Modifier.testTag("att_edit_btn_${student.id}")
                  ) {
                    Icon(Icons.Filled.Edit, contentDescription = strings.editAttendanceBtn, tint = RoyalBlue800, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.editAttendanceBtn, style = MaterialTheme.typography.labelSmall, color = RoyalBlue800)
                  }
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val handleStatusTap: (AttendanceStatus) -> Unit = { tappedStatus ->
              if (existingRecord == null) {
                onMarkAttendance(student, selectedDateKey, tappedStatus)
              } else if (isAdmin && currentStatus != tappedStatus) {
                onOpenEditDialog(existingRecord, tappedStatus)
              }
            }

            AttendanceStatusButton(
              label = strings.presentLabel,
              selected = currentStatus == AttendanceStatus.PRESENT,
              enabled = !isSaving && (!isAlreadyMarked || (isAdmin && currentStatus != AttendanceStatus.PRESENT)),
              selectedColor = EmeraldPaid,
              selectedBg = EmeraldPaidBg,
              onClick = { handleStatusTap(AttendanceStatus.PRESENT) },
              modifier = Modifier
                .weight(1f)
                .testTag("att_present_${student.id}")
            )
            AttendanceStatusButton(
              label = strings.absentLabel,
              selected = currentStatus == AttendanceStatus.ABSENT,
              enabled = !isSaving && (!isAlreadyMarked || (isAdmin && currentStatus != AttendanceStatus.ABSENT)),
              selectedColor = RoseDue,
              selectedBg = RoseDueBg,
              onClick = { handleStatusTap(AttendanceStatus.ABSENT) },
              modifier = Modifier
                .weight(1f)
                .testTag("att_absent_${student.id}")
            )
            AttendanceStatusButton(
              label = strings.leaveLabel,
              selected = currentStatus == AttendanceStatus.LEAVE,
              enabled = !isSaving && (!isAlreadyMarked || (isAdmin && currentStatus != AttendanceStatus.LEAVE)),
              selectedColor = AmberWarning,
              selectedBg = AmberWarningBg,
              onClick = { handleStatusTap(AttendanceStatus.LEAVE) },
              modifier = Modifier
                .weight(1f)
                .testTag("att_leave_${student.id}")
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AttendanceStatusButton(
  label: String,
  selected: Boolean,
  enabled: Boolean,
  selectedColor: Color,
  selectedBg: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (selected) selectedBg else OffWhiteBg,
    border = BorderStroke(
      width = if (selected) 1.5.dp else 1.dp,
      color = if (selected) selectedColor else Slate200
    ),
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(enabled = enabled, onClick = onClick)
      .minimumInteractiveComponentSize()
  ) {
    Box(
      modifier = Modifier.padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = when {
          selected -> selectedColor
          enabled -> Slate700
          else -> Slate500
        }
      )
    }
  }
}
