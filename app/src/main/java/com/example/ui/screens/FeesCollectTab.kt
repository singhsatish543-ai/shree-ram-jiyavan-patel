package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentType
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.i18n.AcademicSessions
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
import com.example.util.FeeLedgerUtils

@Composable
internal fun CollectStudentFeesTab(
  strings: AppStrings,
  adminDisplayName: String,
  students: List<StudentRecord>,
  structures: List<FeeStructure>,
  payments: List<FeePayment>,
  selectedStudent: StudentRecord?,
  onSelectStudent: (StudentRecord?) -> Unit,
  academicYear: String,
  onAcademicYearChange: (String) -> Unit,
  selectedMonthIndex: Int,
  onSelectMonth: (Int) -> Unit,
  isSaving: Boolean,
  onCollectFee: (
    student: StudentRecord,
    academicYear: String,
    monthIndex: Int,
    monthName: String,
    tuitionFee: Long,
    otherFee: Long,
    lateFee: Long,
    discount: Long,
    previousBalance: Long,
    amountPaid: Long,
    paymentType: PaymentType,
    paymentMode: PaymentMode,
    remarks: String,
    collectedBy: String,
    onDone: () -> Unit
  ) -> Unit,
  onViewReceipt: (FeePayment) -> Unit
) {
  var studentQuery by remember { mutableStateOf("") }
  val activeStudents = remember(students) {
    students.filter { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
  }
  val matchingStudents = remember(activeStudents, studentQuery) {
    val q = studentQuery.trim().lowercase()
    if (q.isEmpty()) activeStudents.take(8)
    else activeStudents.filter { s ->
      s.fullName.lowercase().contains(q) ||
        s.admissionNo.lowercase().contains(q) ||
        s.fatherName.lowercase().contains(q)
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("collect_fees_tab_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = strings.collectMonthlyFees,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = RoyalBlue900
            )
            if (selectedStudent != null) {
              TextButton(
                onClick = { onSelectStudent(null) },
                modifier = Modifier.testTag("change_collect_student_btn")
              ) {
                Text(strings.clear, color = RoyalBlue700)
              }
            }
          }

          OutlinedTextField(
            value = studentQuery,
            onValueChange = { studentQuery = it },
            placeholder = { Text(strings.searchStudentsHint) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = strings.searchStudentsHint, tint = RoyalBlue700) },
            trailingIcon = {
              if (studentQuery.isNotEmpty()) {
                IconButton(onClick = { studentQuery = "" }) {
                  Icon(Icons.Filled.Clear, contentDescription = strings.clear)
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("collect_student_search")
          )
        }
      }
    }

    if (selectedStudent == null) {
      if (activeStudents.isEmpty()) {
        item {
          EmptyStateCard(
            icon = Icons.Outlined.School,
            title = strings.noStudentsFound,
            description = strings.noStudentsHint
          )
        }
      } else {
        item {
          Text(
            text = strings.selectStudentForFeeHint,
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )
        }
        items(matchingStudents, key = { "pick_${it.id}" }) { student ->
          val summary = remember(student, structures, payments, academicYear) {
            FeeLedgerUtils.buildStudentSessionSummary(student, structures, payments, academicYear)
          }
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .clickable { onSelectStudent(student) }
              .testTag("pick_student_card_${student.id}")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = SkyBlue50, modifier = Modifier.size(40.dp)) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = student.fullName, tint = RoyalBlue800)
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(student.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
                  Text(
                    text = "${student.admissionNo} • ${student.className} • ${strings.fatherName}: ${student.fatherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                  )
                }
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = formatInr(summary.totalPending),
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = if (summary.totalPending > 0L) RoseDue else EmeraldPaid
                )
                Text(
                  text = strings.pendingFees,
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate500
                )
              }
            }
          }
        }
      }
    } else {
      item {
        SelectedStudentCollectForm(
          student = selectedStudent,
          structures = structures,
          payments = payments,
          academicYear = academicYear,
          onAcademicYearChange = onAcademicYearChange,
          selectedMonthIndex = selectedMonthIndex,
          onSelectMonth = onSelectMonth,
          adminDisplayName = adminDisplayName,
          strings = strings,
          isSaving = isSaving,
          onCollectFee = onCollectFee,
          onViewReceipt = onViewReceipt
        )
      }
    }
  }
}

@Composable
private fun SelectedStudentCollectForm(
  student: StudentRecord,
  structures: List<FeeStructure>,
  payments: List<FeePayment>,
  academicYear: String,
  onAcademicYearChange: (String) -> Unit,
  selectedMonthIndex: Int,
  onSelectMonth: (Int) -> Unit,
  adminDisplayName: String,
  strings: AppStrings,
  isSaving: Boolean,
  onCollectFee: (
    student: StudentRecord,
    academicYear: String,
    monthIndex: Int,
    monthName: String,
    tuitionFee: Long,
    otherFee: Long,
    lateFee: Long,
    discount: Long,
    previousBalance: Long,
    amountPaid: Long,
    paymentType: PaymentType,
    paymentMode: PaymentMode,
    remarks: String,
    collectedBy: String,
    onDone: () -> Unit
  ) -> Unit,
  onViewReceipt: (FeePayment) -> Unit
) {
  val sessionSummary = remember(student, structures, payments, academicYear) {
    FeeLedgerUtils.buildStudentSessionSummary(student, structures, payments, academicYear)
  }
  val structure = sessionSummary.structure
  val monthSummary = sessionSummary.monthSummary(selectedMonthIndex)
  val previousBalance = remember(sessionSummary, selectedMonthIndex) {
    sessionSummary.previousBalanceBeforeMonth(selectedMonthIndex)
  }
  val baseBreakdown = remember(student, structure, selectedMonthIndex) {
    FeeLedgerUtils.expectedMonthBreakdown(student, structure, selectedMonthIndex)
  }

  var tuitionText by remember(student.id, academicYear, selectedMonthIndex) {
    mutableStateOf(baseBreakdown.first.toString())
  }
  var otherText by remember(student.id, academicYear, selectedMonthIndex) {
    mutableStateOf(baseBreakdown.second.toString())
  }
  var lateText by remember(student.id, academicYear, selectedMonthIndex) {
    mutableStateOf((structure?.lateFee ?: 0L).toString())
  }
  var discountText by remember(student.id, academicYear, selectedMonthIndex) {
    mutableStateOf(baseBreakdown.third.toString())
  }
  var paymentType by remember(student.id, academicYear, selectedMonthIndex) {
    mutableStateOf(PaymentType.FULL)
  }
  var paymentMode by remember { mutableStateOf(PaymentMode.CASH) }
  var remarks by remember { mutableStateOf("") }

  val tuition = tuitionText.toLongOrNull() ?: 0L
  val other = otherText.toLongOrNull() ?: 0L
  val late = lateText.toLongOrNull() ?: 0L
  val discount = discountText.toLongOrNull() ?: 0L
  val netMonthTotal = (tuition + other + late - discount).coerceAtLeast(0L)
  val alreadyPaidThisMonth = monthSummary?.paid ?: 0L
  val remainingDueThisMonth = (netMonthTotal - alreadyPaidThisMonth).coerceAtLeast(0L)

  var amountPaidText by remember(student.id, academicYear, selectedMonthIndex) {
    mutableStateOf(remainingDueThisMonth.toString())
  }

  LaunchedEffect(paymentType, remainingDueThisMonth) {
    if (paymentType == PaymentType.FULL) {
      amountPaidText = remainingDueThisMonth.toString()
    }
  }

  val enteredPaid = if (paymentType == PaymentType.FULL) {
    remainingDueThisMonth
  } else {
    (amountPaidText.toLongOrNull() ?: 0L).coerceAtLeast(0L)
  }
  val monthBalanceAfter = (remainingDueThisMonth - enteredPaid).coerceAtLeast(0L)
  val totalRemainingBalance = (previousBalance + monthBalanceAfter).coerceAtLeast(0L)

  val isMonthAlreadyPaid = monthSummary?.isFullyPaid == true
  val isValidAmount = if (netMonthTotal == 0L) {
    !isMonthAlreadyPaid
  } else {
    enteredPaid in 1L..remainingDueThisMonth
  }

  val paidMonthNames = sessionSummary.paidMonths.map { strings.monthsShort.getOrElse(it - 1) { "M$it" } }
  val pendingMonthNames = sessionSummary.pendingMonths.map { strings.monthsShort.getOrElse(it - 1) { "M$it" } }
  val monthName = strings.months.getOrElse(selectedMonthIndex - 1) { "April" }

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Auto-populated Student Fee Profile Card
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, SkyBlue100),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("collect_student_summary_card")
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = student.fullName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900
        )
        Text(
          text = "${strings.admissionNo}: ${student.admissionNo} • ${strings.classLabel}: ${student.className} (${student.section})",
          style = MaterialTheme.typography.bodySmall,
          color = Slate700
        )
        Text(
          text = "${strings.fatherName}: ${student.fatherName} • ${strings.monthlyFee}: ${formatInr(baseBreakdown.first)}",
          style = MaterialTheme.typography.bodySmall,
          color = Slate500
        )
        HorizontalDivider(color = Slate200)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(strings.previousBalanceLabel, style = MaterialTheme.typography.bodySmall, color = Slate500)
          Text(
            text = formatInr(previousBalance),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (previousBalance > 0L) RoseDue else EmeraldPaid,
            modifier = Modifier.testTag("collect_previous_balance_value")
          )
        }
        Text(
          text = "${strings.paidMonthsLabel}: ${if (paidMonthNames.isEmpty()) "—" else paidMonthNames.joinToString(", ")}",
          style = MaterialTheme.typography.labelSmall,
          color = EmeraldPaid
        )
        Text(
          text = "${strings.pendingMonthsLabel}: ${if (pendingMonthNames.isEmpty()) "—" else pendingMonthNames.joinToString(", ")}",
          style = MaterialTheme.typography.labelSmall,
          color = if (pendingMonthNames.isEmpty()) EmeraldPaid else RoseDue
        )
      }
    }

    // Session & Month Selection
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, Slate200),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(strings.session, style = MaterialTheme.typography.labelMedium, color = Slate700)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

        Text(strings.selectMonth, style = MaterialTheme.typography.labelMedium, color = Slate700)
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          strings.monthsShort.forEachIndexed { idx, shortName ->
            val mIdx = idx + 1
            val mSum = sessionSummary.monthSummary(mIdx)
            val mPaid = mSum?.isFullyPaid == true
            val mPartial = mSum?.isPartiallyPaid == true
            val chipLabel = when {
              mPaid -> "$shortName ✓"
              mPartial -> "$shortName ~"
              else -> shortName
            }
            FilterChip(
              selected = selectedMonthIndex == mIdx,
              onClick = { onSelectMonth(mIdx) },
              label = { Text(chipLabel) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalBlue800,
                selectedLabelColor = PureWhite,
                containerColor = when {
                  mPaid -> EmeraldPaidBg
                  mPartial -> AmberWarningBg
                  else -> PureWhite
                }
              ),
              modifier = Modifier.testTag("collect_month_chip_$mIdx")
            )
          }
        }
      }
    }

    if (isMonthAlreadyPaid) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = EmeraldPaidBg,
        border = BorderStroke(1.dp, EmeraldPaid.copy(alpha = 0.4f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("month_already_paid_banner")
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = strings.paidBadge, tint = EmeraldPaid)
            Spacer(Modifier.width(8.dp))
            Text(
              text = strings.monthAlreadyPaidWarning,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = EmeraldPaid
            )
          }
          monthSummary?.payments?.lastOrNull()?.let { latest ->
            OutlinedButton(
              onClick = { onViewReceipt(latest) },
              border = BorderStroke(1.dp, EmeraldPaid),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = strings.viewReceipt, tint = EmeraldPaid, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("${strings.viewReceipt} • ${latest.receiptNo}", color = EmeraldPaid)
            }
          }
        }
      }
    } else {
      // Fee Amounts & Payment Mode Card
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = tuitionText,
              onValueChange = { tuitionText = it.filter { c -> c.isDigit() }.take(6) },
              label = { Text(strings.tuitionFee) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              colors = schoolTextFieldColors(),
              modifier = Modifier
                .weight(1f)
                .testTag("collect_tuition_input")
            )
            OutlinedTextField(
              value = otherText,
              onValueChange = { otherText = it.filter { c -> c.isDigit() }.take(6) },
              label = { Text(strings.otherFee) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              colors = schoolTextFieldColors(),
              modifier = Modifier.weight(1f)
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = lateText,
              onValueChange = { lateText = it.filter { c -> c.isDigit() }.take(5) },
              label = { Text(strings.lateFee) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              colors = schoolTextFieldColors(),
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = discountText,
              onValueChange = { discountText = it.filter { c -> c.isDigit() }.take(6) },
              label = { Text(strings.discount) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              colors = schoolTextFieldColors(),
              modifier = Modifier.weight(1f)
            )
          }

          // Full vs Partial Toggle
          Text(strings.paymentTypeLabel, style = MaterialTheme.typography.labelMedium, color = Slate700)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = paymentType == PaymentType.FULL,
              onClick = { paymentType = PaymentType.FULL },
              label = { Text(strings.fullPaymentLabel) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPaid,
                selectedLabelColor = PureWhite
              ),
              modifier = Modifier.testTag("payment_type_full")
            )
            FilterChip(
              selected = paymentType == PaymentType.PARTIAL,
              onClick = {
                paymentType = PaymentType.PARTIAL
                if (amountPaidText.toLongOrNull() == null || amountPaidText == "0") {
                  amountPaidText = (remainingDueThisMonth / 2L).coerceAtLeast(1L).toString()
                }
              },
              label = { Text(strings.partialPaymentLabel) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AmberWarning,
                selectedLabelColor = PureWhite
              ),
              modifier = Modifier.testTag("payment_type_partial")
            )
          }

          OutlinedTextField(
            value = amountPaidText,
            onValueChange = {
              amountPaidText = it.filter { c -> c.isDigit() }.take(6)
              val typed = amountPaidText.toLongOrNull() ?: 0L
              if (typed < remainingDueThisMonth) {
                paymentType = PaymentType.PARTIAL
              }
            },
            enabled = paymentType == PaymentType.PARTIAL,
            label = { Text(strings.amountPaidLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("collect_amount_paid_input")
          )

          if (!isValidAmount && remainingDueThisMonth > 0L) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.WarningAmber, contentDescription = strings.invalidAmountError, tint = RoseDue, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(
                text = "${strings.invalidAmountError} (Max: ${formatInr(remainingDueThisMonth)})",
                style = MaterialTheme.typography.labelSmall,
                color = RoseDue
              )
            }
          }

          Text(strings.paymentMode, style = MaterialTheme.typography.labelMedium, color = Slate700)
          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(PaymentMode.CASH, PaymentMode.UPI, PaymentMode.BANK_TRANSFER).forEach { mode ->
              FilterChip(
                selected = paymentMode == mode,
                onClick = { paymentMode = mode },
                label = { Text(paymentModeLabel(mode.name, strings)) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = RoyalBlue800,
                  selectedLabelColor = PureWhite
                ),
                modifier = Modifier.testTag("payment_mode_${mode.name.lowercase()}")
              )
            }
          }

          OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it.take(200) },
            label = { Text(strings.remarksHint) },
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )

          // Live Payable & Remaining Balance Preview
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = SkyBlue50,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.totalAmountLabel, style = MaterialTheme.typography.bodySmall, color = Slate700)
                Text(formatInr(netMonthTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Slate900)
              }
              if (alreadyPaidThisMonth > 0L) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(strings.paidBadge, style = MaterialTheme.typography.bodySmall, color = EmeraldPaid)
                  Text("-${formatInr(alreadyPaidThisMonth)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldPaid)
                }
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.amountPaidLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue900)
                Text(
                  text = formatInr(enteredPaid),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue800,
                  modifier = Modifier.testTag("collect_paying_now_text")
                )
              }
              HorizontalDivider(color = SkyBlue100)
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.remainingBalanceLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate700)
                Text(
                  text = formatInr(totalRemainingBalance),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (totalRemainingBalance > 0L) RoseDue else EmeraldPaid,
                  modifier = Modifier.testTag("collect_remaining_balance_text")
                )
              }
            }
          }

          Button(
            onClick = {
              onCollectFee(
                student,
                academicYear,
                selectedMonthIndex,
                monthName,
                tuition,
                other,
                late,
                discount,
                previousBalance,
                enteredPaid,
                paymentType,
                paymentMode,
                remarks,
                adminDisplayName
              ) {
                remarks = ""
              }
            },
            enabled = !isSaving && isValidAmount,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPaid),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("confirm_collect_fee_button")
          ) {
            if (isSaving) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PureWhite, strokeWidth = 2.dp)
            } else {
              Icon(Icons.Filled.CurrencyRupee, contentDescription = strings.confirmCollectFee, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text(strings.confirmCollectFee, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
