package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentRecord
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.AzureBlue600
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue100
import com.example.ui.theme.SkyBlue50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.AttendanceUtils
import com.example.util.StudentAttendanceStats

@Composable
fun AttendancePercentBadge(
  percent: Double,
  totalMarked: Int,
  modifier: Modifier = Modifier
) {
  val (fg, bg) = when {
    totalMarked <= 0 -> Slate500 to OffWhiteBg
    percent >= 75.0 -> EmeraldPaid to EmeraldPaidBg
    percent >= 50.0 -> AmberWarning to AmberWarningBg
    else -> RoseDue to RoseDueBg
  }
  val text = if (totalMarked <= 0) "--" else AttendanceUtils.formatPercentage(percent)
  Surface(
    shape = RoundedCornerShape(50),
    color = bg,
    border = BorderStroke(1.dp, fg.copy(alpha = 0.25f)),
    modifier = modifier.semantics(mergeDescendants = true) {}
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = fg,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun AttendanceStatusPill(
  status: AttendanceStatus,
  strings: AppStrings,
  modifier: Modifier = Modifier
) {
  val (label, fg, bg) = when (status) {
    AttendanceStatus.PRESENT -> Triple(strings.presentLabel, EmeraldPaid, EmeraldPaidBg)
    AttendanceStatus.ABSENT -> Triple(strings.absentLabel, RoseDue, RoseDueBg)
    AttendanceStatus.LEAVE -> Triple(strings.leaveLabel, AmberWarning, AmberWarningBg)
  }
  Surface(
    shape = RoundedCornerShape(50),
    color = bg,
    modifier = modifier
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = fg,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun AttendanceMiniKpiTile(
  label: String,
  value: String,
  accentColor: Color,
  bgColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = bgColor,
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        maxLines = 1
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Slate700,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun DailyAttendanceReportTab(
  strings: AppStrings,
  students: List<StudentRecord>,
  allRecords: List<AttendanceRecord>,
  selectedDateKey: String,
  selectedClass: String,
  selectedSection: String,
  isAdmin: Boolean,
  onOpenEditDialog: (AttendanceRecord, AttendanceStatus?) -> Unit,
  modifier: Modifier = Modifier
) {
  val report = remember(students, allRecords, selectedDateKey, selectedClass, selectedSection, strings) {
    AttendanceUtils.buildDailyAttendanceReport(
      students = students,
      allRecords = allRecords,
      dateKey = selectedDateKey,
      selectedClass = selectedClass,
      allClassesLabel = strings.allClasses,
      selectedSection = selectedSection,
      allSectionsLabel = strings.allSections
    )
  }

  if (report.totalStudents == 0) {
    Box(modifier = modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
      EmptyStateCard(
        icon = Icons.Outlined.EventAvailable,
        title = strings.noStudentsFound,
        description = strings.noStudentsHint
      )
    }
    return
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("daily_report_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SkyBlue100),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("daily_report_summary_card")
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.tabDailyReport,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue900
              )
              Text(
                text = "${AttendanceUtils.formatDisplayDate(selectedDateKey)} ($selectedDateKey)",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = strings.attendancePercentageLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
              AttendancePercentBadge(
                percent = report.attendancePercent,
                totalMarked = report.markedCount,
                modifier = Modifier.testTag("daily_report_percent_badge")
              )
            }
          }

          LinearProgressIndicator(
            progress = {
              if (report.markedCount > 0) (report.attendancePercent / 100.0).toFloat().coerceIn(0f, 1f) else 0f
            },
            color = RoyalBlue800,
            trackColor = SkyBlue50,
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(50))
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AttendanceMiniKpiTile(
              label = strings.presentLabel,
              value = report.presentCount.toString(),
              accentColor = EmeraldPaid,
              bgColor = EmeraldPaidBg,
              modifier = Modifier.weight(1f)
            )
            AttendanceMiniKpiTile(
              label = strings.absentLabel,
              value = report.absentCount.toString(),
              accentColor = RoseDue,
              bgColor = RoseDueBg,
              modifier = Modifier.weight(1f)
            )
            AttendanceMiniKpiTile(
              label = strings.leaveLabel,
              value = report.leaveCount.toString(),
              accentColor = AmberWarning,
              bgColor = AmberWarningBg,
              modifier = Modifier.weight(1f)
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AttendanceMiniKpiTile(
              label = strings.totalStudents,
              value = report.totalStudents.toString(),
              accentColor = RoyalBlue900,
              bgColor = SkyBlue50,
              modifier = Modifier.weight(1f)
            )
            AttendanceMiniKpiTile(
              label = strings.markedCountLabel,
              value = report.markedCount.toString(),
              accentColor = AzureBlue600,
              bgColor = SkyBlue50,
              modifier = Modifier.weight(1f)
            )
            AttendanceMiniKpiTile(
              label = strings.unmarkedCountLabel,
              value = report.unmarkedCount.toString(),
              accentColor = Slate700,
              bgColor = OffWhiteBg,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    if (report.classSummaries.isNotEmpty()) {
      item {
        Text(
          text = strings.classWiseLabel,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900
        )
      }
      items(report.classSummaries, key = { it.className }) { clsSummary ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PureWhite,
          border = BorderStroke(1.dp, Slate200),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_class_summary_${clsSummary.className}")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
              Text(
                text = clsSummary.className,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "${strings.markedCountLabel}: ${clsSummary.markedCount}/${clsSummary.totalStudents} • ${strings.presentLabel}: ${clsSummary.presentCount} • ${strings.absentLabel}: ${clsSummary.absentCount} • ${strings.leaveLabel}: ${clsSummary.leaveCount}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
            AttendancePercentBadge(
              percent = clsSummary.attendancePercent,
              totalMarked = clsSummary.markedCount
            )
          }
        }
      }
    }

    if (report.records.isEmpty()) {
      item {
        EmptyStateCard(
          icon = Icons.Outlined.Assessment,
          title = strings.noAttendanceRecordsTitle,
          description = strings.noAttendanceRecordsDesc
        )
      }
    } else {
      item {
        Text(
          text = "${strings.studentWiseLabel} (${report.records.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900
        )
      }
      items(report.records, key = { "${it.dateKey}_${it.studentId}" }) { rec ->
        val status = AttendanceStatus.fromRaw(rec.status)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PureWhite,
          border = BorderStroke(1.dp, Slate200),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_record_card_${rec.studentId}")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = rec.studentName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "${rec.className} (${rec.section}) • ${rec.admissionNo}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
              Text(
                text = buildString {
                  append("${strings.markedByLabel}: ${rec.markedBy}")
                  if (rec.editedBy.isNotBlank()) {
                    append(" • ${strings.editedByLabel}: ${rec.editedBy}")
                  }
                },
                style = MaterialTheme.typography.labelSmall,
                color = RoyalBlue800
              )
              if (rec.editReason.isNotBlank()) {
                Text(
                  text = rec.editReason,
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate500
                )
              }
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              AttendanceStatusPill(status = status, strings = strings)
              if (isAdmin) {
                IconButton(
                  onClick = { onOpenEditDialog(rec, status) },
                  modifier = Modifier.testTag("daily_edit_btn_${rec.studentId}")
                ) {
                  Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = strings.editAttendanceBtn,
                    tint = RoyalBlue800
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
fun MonthlyAttendanceReportTab(
  strings: AppStrings,
  students: List<StudentRecord>,
  allRecords: List<AttendanceRecord>,
  selectedClass: String,
  selectedSection: String,
  modifier: Modifier = Modifier
) {
  val currentMonth = remember { AttendanceUtils.currentMonthKey() }
  var selectedMonthKey by remember { mutableStateOf(currentMonth) }

  val report = remember(students, allRecords, selectedMonthKey, selectedClass, selectedSection, strings) {
    AttendanceUtils.buildMonthlyAttendanceReport(
      students = students,
      allRecords = allRecords,
      monthKey = selectedMonthKey,
      selectedClass = selectedClass,
      allClassesLabel = strings.allClasses,
      selectedSection = selectedSection,
      allSectionsLabel = strings.allSections
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("monthly_report_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SkyBlue100),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("monthly_report_summary_card")
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { selectedMonthKey = AttendanceUtils.shiftMonthKey(selectedMonthKey, -1) },
              modifier = Modifier.testTag("monthly_prev_btn")
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", tint = RoyalBlue800)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(18.dp))
                Text(
                  text = AttendanceUtils.formatDisplayMonth(selectedMonthKey),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue900,
                  modifier = Modifier.testTag("monthly_month_label")
                )
              }
              Text(
                text = strings.monthlySummaryTitle,
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
            }
            IconButton(
              enabled = selectedMonthKey < currentMonth,
              onClick = { selectedMonthKey = AttendanceUtils.shiftMonthKey(selectedMonthKey, 1) },
              modifier = Modifier.testTag("monthly_next_btn")
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", tint = RoyalBlue800)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${strings.workingDaysLabel}: ${report.totalWorkingDays}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.testTag("monthly_working_days_text")
              )
              Text(
                text = "${strings.markedCountLabel}: ${report.totalEntries}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
            AttendancePercentBadge(
              percent = report.attendancePercent,
              totalMarked = report.totalEntries,
              modifier = Modifier.testTag("monthly_report_percent_badge")
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AttendanceMiniKpiTile(
              label = strings.presentLabel,
              value = report.presentCount.toString(),
              accentColor = EmeraldPaid,
              bgColor = EmeraldPaidBg,
              modifier = Modifier.weight(1f)
            )
            AttendanceMiniKpiTile(
              label = strings.absentLabel,
              value = report.absentCount.toString(),
              accentColor = RoseDue,
              bgColor = RoseDueBg,
              modifier = Modifier.weight(1f)
            )
            AttendanceMiniKpiTile(
              label = strings.leaveLabel,
              value = report.leaveCount.toString(),
              accentColor = AmberWarning,
              bgColor = AmberWarningBg,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    if (report.totalEntries == 0) {
      item {
        EmptyStateCard(
          icon = Icons.Outlined.CalendarMonth,
          title = strings.noAttendanceRecordsTitle,
          description = strings.noAttendanceRecordsDesc
        )
      }
    } else {
      item {
        Text(
          text = strings.dailyBreakdownTitle,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900
        )
      }
      items(report.daySummaries, key = { it.dateKey }) { day ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PureWhite,
          border = BorderStroke(1.dp, Slate200),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_day_card_${day.dateKey}")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = AttendanceUtils.formatDisplayDate(day.dateKey),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "${strings.presentLabel}: ${day.presentCount} • ${strings.absentLabel}: ${day.absentCount} • ${strings.leaveLabel}: ${day.leaveCount}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
            AttendancePercentBadge(
              percent = day.attendancePercent,
              totalMarked = day.totalMarked
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = strings.studentWiseLabel,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900
        )
      }
      items(report.studentStats, key = { "monthly_${it.student.id}" }) { stat ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PureWhite,
          border = BorderStroke(1.dp, Slate200),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_student_card_${stat.student.id}")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = stat.student.fullName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "${stat.student.className} (${stat.student.section}) • ${strings.presentLabel}: ${stat.presentCount}/${stat.totalMarked} • ${strings.absentLabel}: ${stat.absentCount} • ${strings.leaveLabel}: ${stat.leaveCount}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
            AttendancePercentBadge(
              percent = stat.attendancePercent,
              totalMarked = stat.totalMarked
            )
          }
        }
      }
    }
  }
}

@Composable
fun StudentWiseAttendanceReportTab(
  strings: AppStrings,
  students: List<StudentRecord>,
  allRecords: List<AttendanceRecord>,
  selectedClass: String,
  selectedSection: String,
  isAdmin: Boolean,
  onOpenEditDialog: (AttendanceRecord, AttendanceStatus?) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedStudentForHistory by remember { mutableStateOf<StudentAttendanceStats?>(null) }

  val statsList = remember(students, allRecords, selectedClass, selectedSection, searchQuery, strings) {
    AttendanceUtils.buildStudentAttendanceReport(
      students = students,
      allRecords = allRecords,
      selectedClass = selectedClass,
      allClassesLabel = strings.allClasses,
      selectedSection = selectedSection,
      allSectionsLabel = strings.allSections,
      searchQuery = searchQuery
    )
  }

  Column(modifier = modifier.fillMaxSize()) {
    Surface(color = PureWhite, border = BorderStroke(1.dp, Slate200)) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text(strings.searchStudentsHint) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = strings.searchStudentsHint, tint = RoyalBlue800) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = schoolTextFieldColors(),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .testTag("student_att_search_input")
      )
    }

    if (statsList.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        EmptyStateCard(
          icon = Icons.Outlined.PersonSearch,
          title = strings.noStudentsFound,
          description = strings.noStudentsHint
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("student_wise_report_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(statsList, key = { it.student.id }) { stat ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .clickable { selectedStudentForHistory = stat }
              .testTag("student_wise_card_${stat.student.id}")
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
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = stat.student.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                  )
                  Text(
                    text = "${stat.student.className} (${stat.student.section}) • ${strings.rollNumber}: ${stat.student.rollNumber} • ${stat.student.admissionNo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                  )
                }
                AttendancePercentBadge(
                  percent = stat.attendancePercent,
                  totalMarked = stat.totalMarked,
                  modifier = Modifier.testTag("student_wise_percent_${stat.student.id}")
                )
              }

              LinearProgressIndicator(
                progress = {
                  if (stat.totalMarked > 0) (stat.attendancePercent / 100.0).toFloat().coerceIn(0f, 1f) else 0f
                },
                color = when {
                  stat.totalMarked == 0 -> Slate200
                  stat.attendancePercent >= 75.0 -> EmeraldPaid
                  stat.attendancePercent >= 50.0 -> AmberWarning
                  else -> RoseDue
                },
                trackColor = OffWhiteBg,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(50))
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "${strings.workingDaysLabel}: ${stat.totalMarked}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue900
                )
                Text(
                  text = "${strings.presentLabel}: ${stat.presentCount}  •  ${strings.absentLabel}: ${stat.absentCount}  •  ${strings.leaveLabel}: ${stat.leaveCount}",
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate700
                )
              }
            }
          }
        }
      }
    }
  }

  val historyTarget = selectedStudentForHistory
  if (historyTarget != null) {
    // Recompute live stats if records updated after edit
    val liveStats = statsList.firstOrNull { it.student.id == historyTarget.student.id } ?: historyTarget
    StudentAttendanceHistoryDialog(
      stats = liveStats,
      isAdmin = isAdmin,
      strings = strings,
      onDismiss = { selectedStudentForHistory = null },
      onRequestEdit = { rec -> onOpenEditDialog(rec, AttendanceStatus.fromRaw(rec.status)) }
    )
  }
}

@Composable
fun StudentAttendanceHistoryDialog(
  stats: StudentAttendanceStats,
  isAdmin: Boolean,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onRequestEdit: (AttendanceRecord) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = PureWhite,
    title = {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = strings.studentHistoryTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900
        )
        Text(
          text = "${stats.student.fullName} • ${stats.student.className} (${stats.student.section})",
          style = MaterialTheme.typography.bodySmall,
          color = Slate500
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SkyBlue50,
          border = BorderStroke(1.dp, SkyBlue100),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${strings.presentLabel}: ${stats.presentCount}/${stats.totalMarked}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue900
              )
              Text(
                text = "${strings.absentLabel}: ${stats.absentCount} • ${strings.leaveLabel}: ${stats.leaveCount}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate700
              )
            }
            AttendancePercentBadge(percent = stats.attendancePercent, totalMarked = stats.totalMarked)
          }
        }

        if (stats.records.isEmpty()) {
          Text(
            text = strings.attendanceHistoryEmpty,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500
          )
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(stats.records, key = { "${it.dateKey}_${it.studentId}" }) { rec ->
              val st = AttendanceStatus.fromRaw(rec.status)
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = OffWhiteBg,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = AttendanceUtils.formatDisplayDate(rec.dateKey),
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = Slate900
                    )
                    Text(
                      text = "${strings.markedByLabel}: ${rec.markedBy}",
                      style = MaterialTheme.typography.labelSmall,
                      color = Slate500
                    )
                    if (rec.editedBy.isNotBlank()) {
                      Text(
                        text = "${strings.editedByLabel}: ${rec.editedBy}" +
                          if (rec.editReason.isNotBlank()) " (${rec.editReason})" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalBlue800
                      )
                    }
                  }
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AttendanceStatusPill(status = st, strings = strings)
                    if (isAdmin) {
                      IconButton(onClick = { onRequestEdit(rec) }) {
                        Icon(Icons.Filled.Edit, contentDescription = strings.editAttendanceBtn, tint = RoyalBlue800, modifier = Modifier.size(18.dp))
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.close, color = RoyalBlue800, fontWeight = FontWeight.Bold)
      }
    }
  )
}

@Composable
fun AdminEditAttendanceDialog(
  record: AttendanceRecord,
  initialStatus: AttendanceStatus,
  strings: AppStrings,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (AttendanceStatus, String) -> Unit
) {
  var selectedStatus by remember(record, initialStatus) { mutableStateOf(initialStatus) }
  var editReason by remember(record) { mutableStateOf(record.editReason) }

  AlertDialog(
    onDismissRequest = { if (!isSaving) onDismiss() },
    containerColor = PureWhite,
    title = {
      Text(
        text = strings.editAttendanceTitle,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = RoyalBlue900,
        modifier = Modifier.testTag("admin_edit_attendance_dialog")
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SkyBlue50,
          border = BorderStroke(1.dp, SkyBlue100),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = record.studentName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = RoyalBlue900
            )
            Text(
              text = "${record.className} (${record.section}) • ${record.admissionNo}",
              style = MaterialTheme.typography.bodySmall,
              color = Slate700
            )
            Text(
              text = "${strings.attendanceDateLabel}: ${AttendanceUtils.formatDisplayDate(record.dateKey)}",
              style = MaterialTheme.typography.labelSmall,
              color = RoyalBlue800
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          EditStatusChoiceChip(
            label = strings.presentLabel,
            selected = selectedStatus == AttendanceStatus.PRESENT,
            selectedColor = EmeraldPaid,
            selectedBg = EmeraldPaidBg,
            onClick = { selectedStatus = AttendanceStatus.PRESENT },
            modifier = Modifier
              .weight(1f)
              .testTag("edit_status_present")
          )
          EditStatusChoiceChip(
            label = strings.absentLabel,
            selected = selectedStatus == AttendanceStatus.ABSENT,
            selectedColor = RoseDue,
            selectedBg = RoseDueBg,
            onClick = { selectedStatus = AttendanceStatus.ABSENT },
            modifier = Modifier
              .weight(1f)
              .testTag("edit_status_absent")
          )
          EditStatusChoiceChip(
            label = strings.leaveLabel,
            selected = selectedStatus == AttendanceStatus.LEAVE,
            selectedColor = AmberWarning,
            selectedBg = AmberWarningBg,
            onClick = { selectedStatus = AttendanceStatus.LEAVE },
            modifier = Modifier
              .weight(1f)
              .testTag("edit_status_leave")
          )
        }

        OutlinedTextField(
          value = editReason,
          onValueChange = { editReason = it },
          label = { Text(strings.editReasonHint) },
          maxLines = 2,
          shape = RoundedCornerShape(12.dp),
          colors = schoolTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_attendance_reason_input")
        )
      }
    },
    confirmButton = {
      Button(
        enabled = !isSaving,
        onClick = { onSave(selectedStatus, editReason.trim()) },
        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
        modifier = Modifier.testTag("save_edit_attendance_button")
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      OutlinedButton(
        enabled = !isSaving,
        onClick = onDismiss
      ) {
        Text(strings.cancel)
      }
    }
  )
}

@Composable
private fun EditStatusChoiceChip(
  label: String,
  selected: Boolean,
  selectedColor: Color,
  selectedBg: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (selected) selectedBg else OffWhiteBg,
    border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) selectedColor else Slate200),
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
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
        color = if (selected) selectedColor else Slate700
      )
    }
  }
}
