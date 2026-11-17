package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentType
import com.example.data.model.StudentRecord
import com.example.data.model.UiState
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue800
import com.example.util.FeeLedgerUtils

@Composable
fun FeesCollectionScreen(
  strings: AppStrings,
  adminDisplayName: String,
  studentsState: UiState<List<StudentRecord>>,
  paymentsState: UiState<List<FeePayment>>,
  structuresState: UiState<List<FeeStructure>>,
  isSaving: Boolean,
  activeSession: String = "2026-27",
  initialTabIndex: Int = 0,
  preselectedStudent: StudentRecord? = null,
  onConsumedPreselection: () -> Unit = {},
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
  onSaveFeeStructure: (
    academicSession: String,
    className: String,
    monthlyTuitionFee: Long,
    admissionFee: Long,
    examFee: Long,
    developmentFee: Long,
    otherCharges: Long,
    lateFee: Long,
    discount: Long,
    onDone: () -> Unit
  ) -> Unit,
  onViewReceipt: (FeePayment) -> Unit,
  onShowMessage: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(initialTabIndex.coerceIn(0, 5)) }
  var academicYear by remember(activeSession) { mutableStateOf(activeSession.ifBlank { "2026-27" }) }
  var selectedMonthIndex by remember { mutableIntStateOf(FeeLedgerUtils.currentAcademicMonthIndex()) }
  var collectSelectedStudent by remember { mutableStateOf<StudentRecord?>(null) }
  var editingClassStructure by remember { mutableStateOf<Pair<String, FeeStructure?>?>(null) }

  LaunchedEffect(initialTabIndex) {
    selectedTab = initialTabIndex.coerceIn(0, 5)
  }

  LaunchedEffect(preselectedStudent) {
    if (preselectedStudent != null) {
      collectSelectedStudent = preselectedStudent
      selectedTab = 1
      onConsumedPreselection()
    }
  }

  val tabTitles = listOf(
    strings.tabFeesDashboard,
    strings.tabCollectFees,
    strings.tabPendingFees,
    strings.tabReceipts,
    strings.tabFeeStructure,
    strings.tabFeeReports
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OffWhiteBg)
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = PureWhite,
      contentColor = RoyalBlue800,
      edgePadding = 8.dp,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = RoyalBlue800
        )
      }
    ) {
      tabTitles.forEachIndexed { idx, title ->
        Tab(
          selected = selectedTab == idx,
          onClick = { selectedTab = idx },
          text = {
            Text(
              text = title,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          modifier = Modifier.testTag("fees_tab_$idx")
        )
      }
    }

    when {
      studentsState is UiState.Loading || paymentsState is UiState.Loading || structuresState is UiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = RoyalBlue800)
        }
      }

      studentsState is UiState.Error -> {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(
            icon = Icons.Outlined.School,
            title = strings.errorLoadingData,
            description = studentsState.message
          )
        }
      }

      paymentsState is UiState.Error -> {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(
            icon = Icons.Outlined.Receipt,
            title = strings.errorLoadingData,
            description = paymentsState.message
          )
        }
      }

      else -> {
        val students = (studentsState as UiState.Success).data
        val payments = (paymentsState as UiState.Success).data
        val structures = (structuresState as? UiState.Success)?.data ?: emptyList()

        when (selectedTab) {
          0 -> FeesDashboardTab(
            strings = strings,
            students = students,
            structures = structures,
            payments = payments,
            academicYear = academicYear,
            onAcademicYearChange = { academicYear = it },
            onJumpToTab = { selectedTab = it }
          )

          1 -> CollectStudentFeesTab(
            strings = strings,
            adminDisplayName = adminDisplayName,
            students = students,
            structures = structures,
            payments = payments,
            selectedStudent = collectSelectedStudent,
            onSelectStudent = { collectSelectedStudent = it },
            academicYear = academicYear,
            onAcademicYearChange = { academicYear = it },
            selectedMonthIndex = selectedMonthIndex,
            onSelectMonth = { selectedMonthIndex = it },
            isSaving = isSaving,
            onCollectFee = onCollectFee,
            onViewReceipt = onViewReceipt
          )

          2 -> PendingFeesTab(
            strings = strings,
            students = students,
            structures = structures,
            payments = payments,
            academicYear = academicYear,
            onAcademicYearChange = { academicYear = it },
            onQuickCollect = { student ->
              collectSelectedStudent = student
              selectedTab = 1
            },
            onShowMessage = onShowMessage
          )

          3 -> FeeHistoryTab(
            strings = strings,
            payments = payments,
            onViewReceipt = onViewReceipt
          )

          4 -> ClassFeeStructureTab(
            strings = strings,
            structures = structures,
            academicYear = academicYear,
            onAcademicYearChange = { academicYear = it },
            onEditStructure = { cls, existing ->
              editingClassStructure = cls to existing
            }
          )

          5 -> FeeCollectionReportsTab(
            strings = strings,
            students = students,
            structures = structures,
            payments = payments,
            academicYear = academicYear,
            onAcademicYearChange = { academicYear = it },
            onShowMessage = onShowMessage
          )
        }
      }
    }
  }

  editingClassStructure?.let { (clsName, existing) ->
    FeeStructureDialog(
      academicSession = academicYear,
      className = clsName,
      existing = existing,
      strings = strings,
      isSaving = isSaving,
      onDismiss = { editingClassStructure = null },
      onSave = { tuition, admission, exam, development, otherCharges, lateFee, discount ->
        onSaveFeeStructure(
          academicYear,
          clsName,
          tuition,
          admission,
          exam,
          development,
          otherCharges,
          lateFee,
          discount
        ) {
          editingClassStructure = null
        }
      }
    )
  }
}
