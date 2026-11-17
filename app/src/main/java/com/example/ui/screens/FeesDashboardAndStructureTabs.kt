package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.i18n.AcademicSessions
import com.example.i18n.AppStrings
import com.example.ui.components.SummaryKpiCard
import com.example.ui.components.formatInr
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.AzureBlue600
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
import com.example.util.FeeLedgerUtils

@Composable
internal fun FeesDashboardTab(
  strings: AppStrings,
  students: List<StudentRecord>,
  structures: List<FeeStructure>,
  payments: List<FeePayment>,
  academicYear: String,
  onAcademicYearChange: (String) -> Unit,
  onJumpToTab: (Int) -> Unit
) {
  val currentMonthIdx = remember { FeeLedgerUtils.currentAcademicMonthIndex() }
  val summary = remember(students, structures, payments, academicYear, currentMonthIdx) {
    FeeLedgerUtils.computeDashboardSummary(
      students = students,
      structures = structures,
      payments = payments,
      academicSession = academicYear,
      currentMonthIndex = currentMonthIdx
    )
  }
  val activeStudents = remember(students) {
    students.filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
  }
  val studentSummaries = remember(activeStudents, structures, payments, academicYear) {
    activeStudents.map { FeeLedgerUtils.buildStudentSessionSummary(it, structures, payments, academicYear) }
  }
  val classSummaries = remember(studentSummaries) {
    FeeLedgerUtils.computeClassPendingSummaries(studentSummaries)
  }

  val kpiItems = listOf(
    FeeKpiSpec(strings.totalStudents, summary.totalStudents.toString(), Icons.Filled.Groups, RoyalBlue800, SkyBlue50, "fee_kpi_total_students"),
    FeeKpiSpec(strings.totalExpectedFeesLabel, formatInr(summary.totalExpectedFees), Icons.Filled.AccountBalanceWallet, RoyalBlue700, SkyBlue50, "fee_kpi_expected"),
    FeeKpiSpec(strings.totalFeesCollectedLabel, formatInr(summary.totalCollectedFees), Icons.Filled.CurrencyRupee, EmeraldPaid, EmeraldPaidBg, "fee_kpi_collected"),
    FeeKpiSpec(strings.totalPendingFeesLabel, formatInr(summary.totalPendingFees), Icons.Filled.PendingActions, RoseDue, RoseDueBg, "fee_kpi_pending"),
    FeeKpiSpec(strings.todaysCollection, formatInr(summary.todaysCollection), Icons.Filled.Today, AzureBlue600, SkyBlue50, "fee_kpi_today"),
    FeeKpiSpec(strings.thisMonthsCollection, formatInr(summary.thisMonthsCollection), Icons.AutoMirrored.Filled.ReceiptLong, EmeraldPaid, EmeraldPaidBg, "fee_kpi_month"),
    FeeKpiSpec(strings.fullyPaidStudentsLabel, summary.fullyPaidStudents.toString(), Icons.Filled.CheckCircle, EmeraldPaid, EmeraldPaidBg, "fee_kpi_fully_paid"),
    FeeKpiSpec(strings.partiallyPaidStudentsLabel, summary.partiallyPaidStudents.toString(), Icons.Filled.HourglassTop, AmberWarning, AmberWarningBg, "fee_kpi_partial_paid"),
    FeeKpiSpec(strings.unpaidStudentsLabel, summary.unpaidStudents.toString(), Icons.Filled.WarningAmber, RoseDue, RoseDueBg, "fee_kpi_unpaid")
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("fees_dashboard_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      SessionSelectorBar(
        title = strings.feesModuleTitle,
        academicYear = academicYear,
        onAcademicYearChange = onAcademicYearChange,
        strings = strings
      )
    }

    item {
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 680.dp) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          kpiItems.chunked(columns).forEach { rowItems ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              rowItems.forEach { item ->
                SummaryKpiCard(
                  label = item.label,
                  value = item.value,
                  icon = item.icon,
                  accentColor = item.accent,
                  iconBg = item.bg,
                  modifier = Modifier
                    .weight(1f)
                    .testTag(item.tag)
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

    // Quick Action Buttons
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SkyBlue100),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = strings.quickActions,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RoyalBlue900
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { onJumpToTab(1) },
              colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
              modifier = Modifier
                .weight(1f)
                .testTag("quick_open_collect_btn")
            ) {
              Icon(Icons.Filled.CurrencyRupee, contentDescription = strings.tabCollectFees, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(strings.tabCollectFees, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
              onClick = { onJumpToTab(2) },
              border = BorderStroke(1.dp, RoseDue),
              modifier = Modifier
                .weight(1f)
                .testTag("quick_open_pending_btn")
            ) {
              Icon(Icons.Filled.PendingActions, contentDescription = strings.tabPendingFees, tint = RoseDue, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(strings.tabPendingFees, color = RoseDue, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { onJumpToTab(4) },
              border = BorderStroke(1.dp, RoyalBlue700),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Filled.Tune, contentDescription = strings.tabFeeStructure, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(strings.tabFeeStructure, color = RoyalBlue800, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
              onClick = { onJumpToTab(5) },
              border = BorderStroke(1.dp, RoyalBlue700),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Filled.Assessment, contentDescription = strings.tabFeeReports, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(strings.tabFeeReports, color = RoyalBlue800, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
          }
        }
      }
    }

    if (classSummaries.isNotEmpty()) {
      item {
        Text(
          text = strings.classWiseOverview,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Slate700
        )
      }
      items(classSummaries, key = { "dash_cls_${it.className}" }) { cls ->
        val progress = if (cls.expected > 0L) (cls.collected.toFloat() / cls.expected.toFloat()).coerceIn(0f, 1f) else 0f
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = PureWhite,
          border = BorderStroke(1.dp, Slate200),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${cls.className} (${cls.studentCount} ${strings.navStudents})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue900
              )
              Text(
                text = "${formatInr(cls.collected)} / ${formatInr(cls.expected)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = EmeraldPaid
              )
            }
            LinearProgressIndicator(
              progress = { progress },
              color = RoyalBlue800,
              trackColor = SkyBlue100,
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
            )
            Text(
              text = "${strings.totalPendingFeesLabel}: ${formatInr(cls.pending)} (${cls.pendingStudentCount} ${strings.dueCount})",
              style = MaterialTheme.typography.labelSmall,
              color = if (cls.pending > 0L) RoseDue else EmeraldPaid
            )
          }
        }
      }
    }
  }
}

@Composable
internal fun ClassFeeStructureTab(
  strings: AppStrings,
  structures: List<FeeStructure>,
  academicYear: String,
  onAcademicYearChange: (String) -> Unit,
  onEditStructure: (className: String, existing: FeeStructure?) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("fee_structure_tab_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      SessionSelectorBar(
        title = strings.feeStructureTitle,
        academicYear = academicYear,
        onAcademicYearChange = onAcademicYearChange,
        strings = strings
      )
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = SkyBlue50,
        border = BorderStroke(1.dp, SkyBlue100),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = strings.applyToClassStudents,
          style = MaterialTheme.typography.bodySmall,
          color = Slate700,
          modifier = Modifier.padding(14.dp)
        )
      }
    }

    items(strings.classOptions, key = { it }) { cls ->
      val struct = FeeLedgerUtils.findFeeStructure(structures, cls, academicYear)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { onEditStructure(cls, struct) }
          .testTag("fee_structure_card_${cls.lowercase().replace(" ", "_")}")
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = cls,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue900
              )
              if (struct != null) {
                Surface(shape = RoundedCornerShape(50), color = SkyBlue50) {
                  Text(
                    text = struct.academicSession.ifBlank { academicYear },
                    style = MaterialTheme.typography.labelSmall,
                    color = RoyalBlue800,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
              }
            }
            if (struct != null) {
              Text(
                text = "${strings.tuitionFee}: ${formatInr(struct.monthlyTuitionFee)} • ${strings.admissionFeeLabel}: ${formatInr(struct.admissionFee)} • ${strings.examFee}: ${formatInr(struct.examFee)}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate700
              )
              Text(
                text = "${strings.developmentFeeLabel}: ${formatInr(struct.developmentFee)} • ${strings.otherChargesLabel}: ${formatInr(struct.effectiveOtherCharges())} • ${strings.lateFee}: ${formatInr(struct.lateFee)} • ${strings.discount}: ${formatInr(struct.discount)}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
            } else {
              Text(
                text = "${strings.tuitionFee}: —",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
          }
          IconButton(onClick = { onEditStructure(cls, struct) }) {
            Icon(Icons.Filled.Edit, contentDescription = strings.edit, tint = RoyalBlue800)
          }
        }
      }
    }
  }
}

@Composable
internal fun FeeStructureDialog(
  academicSession: String,
  className: String,
  existing: FeeStructure?,
  strings: AppStrings,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (
    tuition: Long,
    admission: Long,
    exam: Long,
    development: Long,
    otherCharges: Long,
    lateFee: Long,
    discount: Long
  ) -> Unit
) {
  var tuitionText by remember { mutableStateOf((existing?.monthlyTuitionFee ?: 500L).toString()) }
  var admissionText by remember { mutableStateOf((existing?.admissionFee ?: 0L).toString()) }
  var examText by remember { mutableStateOf((existing?.examFee ?: 0L).toString()) }
  var developmentText by remember { mutableStateOf((existing?.developmentFee ?: 0L).toString()) }
  var otherChargesText by remember {
    mutableStateOf((existing?.effectiveOtherCharges() ?: 0L).toString())
  }
  var lateFeeText by remember { mutableStateOf((existing?.lateFee ?: 0L).toString()) }
  var discountText by remember { mutableStateOf((existing?.discount ?: 0L).toString()) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = PureWhite,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "$className • ${strings.feeStructureTitle}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = RoyalBlue900
            )
            Text(
              text = "${strings.session}: $academicSession",
              style = MaterialTheme.typography.bodySmall,
              color = Slate500
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = strings.cancel)
          }
        }
        HorizontalDivider(color = Slate200)

        FeeNumberField(tuitionText, { tuitionText = it }, strings.tuitionFee, Modifier.testTag("struct_tuition_input"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FeeNumberField(admissionText, { admissionText = it }, strings.admissionFeeLabel, Modifier.weight(1f).testTag("struct_admission_input"))
          FeeNumberField(examText, { examText = it }, strings.examFee, Modifier.weight(1f).testTag("struct_exam_input"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FeeNumberField(developmentText, { developmentText = it }, strings.developmentFeeLabel, Modifier.weight(1f).testTag("struct_development_input"))
          FeeNumberField(otherChargesText, { otherChargesText = it }, strings.otherChargesLabel, Modifier.weight(1f).testTag("struct_other_input"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FeeNumberField(lateFeeText, { lateFeeText = it }, strings.lateFee, Modifier.weight(1f).testTag("struct_late_input"))
          FeeNumberField(discountText, { discountText = it }, strings.discount, Modifier.weight(1f).testTag("struct_discount_input"))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text(strings.cancel, color = Slate500) }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onSave(
                tuitionText.toLongOrNull() ?: 0L,
                admissionText.toLongOrNull() ?: 0L,
                examText.toLongOrNull() ?: 0L,
                developmentText.toLongOrNull() ?: 0L,
                otherChargesText.toLongOrNull() ?: 0L,
                lateFeeText.toLongOrNull() ?: 0L,
                discountText.toLongOrNull() ?: 0L
              )
            },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
            modifier = Modifier.testTag("save_fee_structure_button")
          ) {
            if (isSaving) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PureWhite, strokeWidth = 2.dp)
            } else {
              Text(strings.saveStructure, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
internal fun SessionSelectorBar(
  title: String,
  academicYear: String,
  onAcademicYearChange: (String) -> Unit,
  strings: AppStrings
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = PureWhite,
    border = BorderStroke(1.dp, Slate200),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = RoyalBlue900,
        modifier = Modifier.weight(1f)
      )
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        AcademicSessions.forEach { yr ->
          FilterChip(
            selected = academicYear == yr,
            onClick = { onAcademicYearChange(yr) },
            label = { Text(yr, style = MaterialTheme.typography.labelSmall) },
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

@Composable
private fun FeeNumberField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier
) {
  OutlinedTextField(
    value = value,
    onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() }.take(6)) },
    label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    singleLine = true,
    colors = schoolTextFieldColors(),
    modifier = modifier.fillMaxWidth()
  )
}

private data class FeeKpiSpec(
  val label: String,
  val value: String,
  val icon: ImageVector,
  val accent: androidx.compose.ui.graphics.Color,
  val bg: androidx.compose.ui.graphics.Color,
  val tag: String
)
