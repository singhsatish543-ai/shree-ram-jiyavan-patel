package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.UiState
import com.example.i18n.AppLanguage
import com.example.i18n.stringsFor
import com.example.ui.components.ReceiptPreviewDialog
import com.example.ui.screens.FeesCollectionScreen
import com.example.ui.theme.SchoolTheme
import com.example.util.FeeExportUtils
import com.example.util.FeeLedgerUtils
import com.example.util.FeeReportType
import com.example.util.StudentFeeStanding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.FileInputStream
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FeesManagementRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val sampleStudents = listOf(
    StudentRecord(
      id = "stu1",
      admissionNo = "SRJP-2026-0001",
      rollNumber = "1",
      fullName = "Aarav Patel",
      fatherName = "Rajesh Patel",
      className = "Class 6",
      section = "A",
      phone = "9876543210",
      monthlyFeeAmount = 0L,
      status = StudentStatus.ACTIVE.name
    ),
    StudentRecord(
      id = "stu2",
      admissionNo = "SRJP-2026-0002",
      rollNumber = "2",
      fullName = "Ananya Verma",
      fatherName = "Suresh Verma",
      className = "Class 6",
      section = "A",
      phone = "9876500001",
      monthlyFeeAmount = 0L,
      status = StudentStatus.ACTIVE.name
    ),
    StudentRecord(
      id = "stu3",
      admissionNo = "SRJP-2026-0003",
      rollNumber = "1",
      fullName = "Rohan Yadav",
      fatherName = "Mahesh Yadav",
      className = "PG",
      section = "A",
      phone = "9876500002",
      monthlyFeeAmount = 500L,
      status = StudentStatus.ACTIVE.name
    )
  )

  private val sampleStructures = listOf(
    FeeStructure(
      id = "2026_27_class_6",
      academicSession = "2026-27",
      className = "Class 6",
      monthlyTuitionFee = 800L,
      admissionFee = 500L,
      examFee = 400L,
      developmentFee = 300L,
      otherCharges = 100L,
      lateFee = 50L,
      discount = 0L
    ),
    FeeStructure(
      id = "2026_27_pg",
      academicSession = "2026-27",
      className = "PG",
      monthlyTuitionFee = 500L
    )
  )

  @Test
  fun `fee ledger computes partial payments previous balance refunded exclusions and dashboard KPIs`() {
    val todayDisplay = FeeLedgerUtils.todayDisplayDate()
    val todayIso = FeeLedgerUtils.todayIsoDate()
    val currentMonthIdx = FeeLedgerUtils.currentAcademicMonthIndex()

    // stu3 (PG, 500/mo => 6000/yr): pay ALL 12 months cleanly (fully paid)
    val stu3FullYearPayments = (1..12).map { mIdx ->
      FeePayment(
        id = "p_stu3_$mIdx",
        receiptNo = "SRJP-2026-100$mIdx",
        studentId = "stu3",
        studentName = "Rohan Yadav",
        fatherName = "Mahesh Yadav",
        admissionNo = "SRJP-2026-0003",
        className = "PG",
        academicYear = "2026-27",
        monthIndex = mIdx.toLong(),
        monthName = "Month $mIdx",
        tuitionFee = 500L,
        totalAmount = 500L,
        amountPaid = 500L,
        remainingBalance = 0L,
        paymentType = PaymentType.FULL.name,
        paymentStatus = PaymentStatus.PAID.name,
        paymentMode = PaymentMode.CASH.name,
        paidOnDate = if (mIdx == currentMonthIdx) todayDisplay else "01 Apr 2026",
        paidOnIsoDate = if (mIdx == currentMonthIdx) todayIso else "2026-04-01"
      )
    }

    // stu1 (Class 6): April expected = 800+100+500+300 = 1700. Partial pay 1000. Plus 1 refunded receipt that must be excluded!
    val stu1Payments = listOf(
      FeePayment(
        id = "p_stu1_apr",
        receiptNo = "SRJP-2026-00001",
        studentId = "stu1",
        studentName = "Aarav Patel",
        fatherName = "Rajesh Patel",
        admissionNo = "SRJP-2026-0001",
        className = "Class 6",
        academicYear = "2026-27",
        monthIndex = 1L,
        monthName = "April",
        tuitionFee = 800L,
        otherFee = 900L,
        totalAmount = 1700L,
        amountPaid = 1000L,
        remainingBalance = 700L,
        paymentType = PaymentType.PARTIAL.name,
        paymentStatus = PaymentStatus.PARTIAL.name,
        paymentMode = PaymentMode.UPI.name,
        paidOnDate = todayDisplay,
        paidOnIsoDate = todayIso
      ),
      FeePayment(
        id = "p_stu1_refunded",
        receiptNo = "SRJP-2026-00002",
        studentId = "stu1",
        studentName = "Aarav Patel",
        fatherName = "Rajesh Patel",
        admissionNo = "SRJP-2026-0001",
        className = "Class 6",
        academicYear = "2026-27",
        monthIndex = 2L,
        monthName = "May",
        tuitionFee = 800L,
        otherFee = 100L,
        totalAmount = 900L,
        amountPaid = 900L,
        remainingBalance = 0L,
        paymentType = PaymentType.FULL.name,
        paymentStatus = PaymentStatus.REFUNDED.name,
        paymentMode = PaymentMode.CASH.name,
        paidOnDate = todayDisplay,
        paidOnIsoDate = todayIso,
        refundOrCorrectionReason = "Duplicate entry"
      )
    )

    val allPayments = stu3FullYearPayments + stu1Payments

    // Class 6 yearly expected per student: (800+100)*12 + 500(adm) + 300(dev) + 400(exam) = 10800 + 1200 = 12000
    val stu1Summary = FeeLedgerUtils.buildStudentSessionSummary(sampleStudents[0], sampleStructures, allPayments, "2026-27")
    assertEquals(12000L, stu1Summary.totalExpected)
    assertEquals(1000L, stu1Summary.totalPaid)
    assertEquals(11000L, stu1Summary.totalPending)
    assertEquals(StudentFeeStanding.PARTIALLY_PAID, stu1Summary.standing)
    assertEquals(700L, stu1Summary.previousBalanceBeforeMonth(2)) // May previous balance = Apr remaining 700
    assertTrue(stu1Summary.monthSummary(1)?.isPartiallyPaid == true)
    assertFalse(stu1Summary.monthSummary(2)?.isFullyPaid == true) // May was REFUNDED -> unpaid again

    val stu2Summary = FeeLedgerUtils.buildStudentSessionSummary(sampleStudents[1], sampleStructures, allPayments, "2026-27")
    assertEquals(StudentFeeStanding.UNPAID, stu2Summary.standing)

    val stu3Summary = FeeLedgerUtils.buildStudentSessionSummary(sampleStudents[2], sampleStructures, allPayments, "2026-27")
    assertEquals(StudentFeeStanding.FULLY_PAID, stu3Summary.standing)
    assertEquals(0L, stu3Summary.totalPending)

    val dash = FeeLedgerUtils.computeDashboardSummary(
      students = sampleStudents,
      structures = sampleStructures,
      payments = allPayments,
      academicSession = "2026-27",
      currentMonthIndex = currentMonthIdx,
      todayDisplay = todayDisplay,
      todayIso = todayIso
    )
    assertEquals(3, dash.totalStudents)
    assertEquals(12000L + 12000L + 6000L, dash.totalExpectedFees)
    assertEquals(7000L, dash.totalCollectedFees) // 6000 + 1000 (refunded 900 excluded)
    assertEquals(23000L, dash.totalPendingFees)
    assertEquals(1500L, dash.todaysCollection) // 500 (today stu3) + 1000 (today stu1)
    assertEquals(500L, dash.thisMonthsCollection)
    assertEquals(1, dash.fullyPaidStudents)
    assertEquals(1, dash.partiallyPaidStudents)
    assertEquals(1, dash.unpaidStudents)

    // Verify all 8 report types populate accurately
    FeeReportType.entries.forEach { reportType ->
      val report = FeeLedgerUtils.buildFeeReport(
        type = reportType,
        strings = stringsFor(AppLanguage.EN),
        students = sampleStudents,
        structures = sampleStructures,
        payments = allPayments,
        academicSession = "2026-27",
        selectedMonthIndex = 1,
        selectedDateDisplay = todayDisplay,
        selectedDateIso = todayIso
      )
      assertTrue("Report ${reportType.name} should have headers", report.headers.isNotEmpty())
      assertTrue("Report ${reportType.name} should have rows", report.rows.isNotEmpty())
    }
  }

  @Test
  fun `fee export utils generates valid PDF receipt PDF report and Excel XLSX report`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val strings = stringsFor(AppLanguage.EN)
    val payment = FeePayment(
      id = "pay_1",
      receiptNo = "SRJP-2026-00042",
      studentId = "stu1",
      studentName = "Aarav Patel",
      fatherName = "Rajesh Patel",
      admissionNo = "SRJP-2026-0001",
      className = "Class 6",
      academicYear = "2026-27",
      monthIndex = 1L,
      monthName = "April",
      tuitionFee = 800L,
      otherFee = 200L,
      lateFee = 0L,
      discount = 0L,
      previousBalance = 500L,
      totalAmount = 1000L,
      amountPaid = 600L,
      remainingBalance = 900L,
      paymentType = PaymentType.PARTIAL.name,
      paymentStatus = PaymentStatus.PARTIAL.name,
      paymentMode = PaymentMode.UPI.name,
      paidOnDate = "16 Nov 2026",
      collectedBy = "Principal"
    )

    val receiptPdf = FeeExportUtils.exportReceiptToPdf(context, payment, strings)
    assertTrue(receiptPdf.exists() && receiptPdf.length() > 500L)
    val pdfContent = receiptPdf.readText(Charsets.ISO_8859_1)
    assertTrue(pdfContent.startsWith("%PDF-1.4"))
    assertTrue(pdfContent.contains("SHREE RAM JIYAVAN PATEL JUNIOR HIGH SCHOOL"))
    assertTrue(pdfContent.contains("BECHU KA PURWA, AIRAYAN, KHAGA, FATEHPUR"))
    assertTrue(pdfContent.contains("SRJP-2026-00042"))
    assertTrue(pdfContent.contains("Authorized Signature"))

    val reportData = FeeLedgerUtils.buildFeeReport(
      type = FeeReportType.UPI,
      strings = strings,
      students = sampleStudents,
      structures = sampleStructures,
      payments = listOf(payment),
      academicSession = "2026-27",
      selectedMonthIndex = 1,
      selectedDateDisplay = "16 Nov 2026",
      selectedDateIso = "2026-11-16"
    )
    val reportPdf = FeeExportUtils.exportFeeReportToPdf(context, reportData, strings)
    assertTrue(reportPdf.exists() && reportPdf.readText(Charsets.ISO_8859_1).startsWith("%PDF-1.4"))

    val reportExcel = FeeExportUtils.exportFeeReportToExcel(context, reportData, strings)
    assertTrue(reportExcel.exists() && reportExcel.length() > 400L)
    val zipEntries = mutableSetOf<String>()
    ZipInputStream(FileInputStream(reportExcel)).use { zip ->
      var entry = zip.nextEntry
      while (entry != null) {
        zipEntries.add(entry.name)
        entry = zip.nextEntry
      }
    }
    assertTrue(zipEntries.contains("[Content_Types].xml"))
    assertTrue(zipEntries.contains("xl/workbook.xml"))
    assertTrue(zipEntries.contains("xl/worksheets/sheet1.xml"))
  }

  @Test
  fun `fees dashboard structure collect pending history and reports tabs work end to end`() {
    val strings = stringsFor(AppLanguage.EN)
    val payments = listOf(
      FeePayment(
        id = "pay_apr",
        receiptNo = "SRJP-2026-00010",
        studentId = "stu1",
        studentName = "Aarav Patel",
        fatherName = "Rajesh Patel",
        admissionNo = "SRJP-2026-0001",
        className = "Class 6",
        academicYear = "2026-27",
        monthIndex = 1L,
        monthName = "April",
        tuitionFee = 800L,
        otherFee = 900L,
        totalAmount = 1700L,
        amountPaid = 1700L,
        remainingBalance = 0L,
        paymentType = PaymentType.FULL.name,
        paymentStatus = PaymentStatus.PAID.name,
        paymentMode = PaymentMode.CASH.name,
        paidOnDate = "10 Apr 2026",
        collectedBy = "Principal"
      )
    )

    var collectedMode: PaymentMode? = null
    var collectedType: PaymentType? = null
    var collectedPaid: Long = -1L
    var savedStructClass: String? = null
    var savedAdmissionFee: Long = -1L
    var viewedReceipt: FeePayment? = null

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        FeesCollectionScreen(
          strings = strings,
          adminDisplayName = "Principal",
          studentsState = UiState.Success(sampleStudents),
          paymentsState = UiState.Success(payments),
          structuresState = UiState.Success(sampleStructures),
          isSaving = false,
          activeSession = "2026-27",
          onCollectFee = { _, _, _, _, _, _, _, _, _, amtPaid, pType, mode, _, _, onDone ->
            collectedPaid = amtPaid
            collectedType = pType
            collectedMode = mode
            onDone()
          },
          onSaveFeeStructure = { _, cls, _, admission, _, _, _, _, _, onDone ->
            savedStructClass = cls
            savedAdmissionFee = admission
            onDone()
          },
          onViewReceipt = { viewedReceipt = it }
        )
      }
    }

    composeTestRule.waitForIdle()

    // 1. Verify Fees Dashboard KPIs
    composeTestRule.onNodeWithTag("fees_tab_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fee_kpi_total_students").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fees_dashboard_list")
      .performScrollToNode(hasTestTag("fee_kpi_unpaid"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fee_kpi_unpaid").assertIsDisplayed()

    // 2. Open Class-wise Fee Structure tab & configure Admission Fee on Class 6
    composeTestRule.onNodeWithTag("fees_tab_4").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fee_structure_tab_list")
      .performScrollToNode(hasTestTag("fee_structure_card_class_6"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fee_structure_card_class_6").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("struct_admission_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("struct_admission_input").performTextInput("750")
    composeTestRule.onNodeWithTag("save_fee_structure_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("Class 6", savedStructClass)
    assertEquals(750L, savedAdmissionFee)

    // 3. Open Collect Fees tab, search stu1 ("Aarav"), select, verify April is duplicate-blocked
    composeTestRule.onNodeWithTag("fees_tab_1").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("collect_student_search").performTextInput("Aarav")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pick_student_card_stu1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("collect_fees_tab_list")
      .performScrollToNode(hasTestTag("collect_student_summary_card"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("collect_student_summary_card").assertIsDisplayed()

    // April (month 1) is already fully paid -> duplicate warning banner shown
    composeTestRule.onNodeWithTag("collect_fees_tab_list")
      .performScrollToNode(hasTestTag("collect_month_chip_1"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("collect_month_chip_1").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("collect_fees_tab_list")
      .performScrollToNode(hasTestTag("month_already_paid_banner"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("month_already_paid_banner").assertIsDisplayed()

    // Switch to May (month 2): Tuition 800 + Other 100 + Late 50 = 950. Switch to Partial Payment (400) via UPI
    composeTestRule.onNodeWithTag("collect_month_chip_2").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("payment_type_partial").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("collect_amount_paid_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("collect_amount_paid_input").performTextInput("400")
    composeTestRule.waitForIdle()
    // Bring lower card content (remaining balance + payment mode row) into LazyColumn viewport first
    composeTestRule.onNodeWithTag("collect_remaining_balance_text").performScrollTo()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("payment_mode_upi").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Remaining balance for May should be 800 + 100 + 50 (late fee) - 400 = 550
    composeTestRule.onNodeWithTag("collect_remaining_balance_text").performScrollTo()
      .assertTextContains("550", substring = true)

    composeTestRule.onNodeWithTag("confirm_collect_fee_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(400L, collectedPaid)
    assertEquals(PaymentType.PARTIAL, collectedType)
    assertEquals(PaymentMode.UPI, collectedMode)

    // 4. Open Pending Fees tab -> verify student-wise, class-wise, month-wise & reminder button
    composeTestRule.onNodeWithTag("fees_tab_2").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pending_fees_tab_list")
      .performScrollToNode(hasTestTag("pending_student_card_stu1"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pending_student_card_stu1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("send_reminder_btn_stu1").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("pending_fees_tab_list").performScrollToIndex(1)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pending_view_class").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pending_view_month").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // 5. Open Fee History tab -> click receipt row
    composeTestRule.onNodeWithTag("fees_tab_3").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("receipt_row_pay_apr").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("SRJP-2026-00010", viewedReceipt?.receiptNo)

    // 6. Open Reports tab -> verify export buttons
    composeTestRule.onNodeWithTag("fees_tab_5").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("export_fee_report_pdf_btn").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_fee_report_excel_btn").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `receipt preview dialog displays school header fields actions and admin refund correction`() {
    val strings = stringsFor(AppLanguage.EN)
    val receipt = FeePayment(
      id = "pay_r1",
      receiptNo = "SRJP-2026-00099",
      studentId = "stu1",
      studentName = "Aarav Patel",
      fatherName = "Rajesh Patel",
      admissionNo = "SRJP-2026-0001",
      className = "Class 6",
      academicYear = "2026-27",
      monthIndex = 1L,
      monthName = "April",
      tuitionFee = 800L,
      otherFee = 200L,
      previousBalance = 300L,
      totalAmount = 1000L,
      amountPaid = 700L,
      remainingBalance = 600L,
      paymentType = PaymentType.PARTIAL.name,
      paymentStatus = PaymentStatus.PARTIAL.name,
      paymentMode = PaymentMode.BANK_TRANSFER.name,
      paidOnDate = "16 Nov 2026",
      collectedBy = "Principal"
    )

    var updatedStatus: PaymentStatus? = null
    var updatedReason: String? = null

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        ReceiptPreviewDialog(
          payment = receipt,
          strings = strings,
          canRefundOrCorrect = true,
          onRefundOrCorrect = { st, rsn ->
            updatedStatus = st
            updatedReason = rsn
          },
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("receipt_preview_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("SHREE RAM JIYAVAN PATEL JUNIOR HIGH SCHOOL").assertIsDisplayed()
    composeTestRule.onNodeWithText("BECHU KA PURWA, AIRAYAN, KHAGA, FATEHPUR").assertIsDisplayed()
    composeTestRule.onNodeWithText("SRJP-2026-00099").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Rajesh Patel").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Authorized Signature").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("download_receipt_pdf_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("print_receipt_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("share_receipt_button").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("refund_correction_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("refund_reason_input").performTextInput("Wrong amount entered")
    composeTestRule.onNodeWithTag("confirm_mark_refunded_button").assertIsEnabled().performClick()
    composeTestRule.waitForIdle()

    assertEquals(PaymentStatus.REFUNDED, updatedStatus)
    assertEquals("Wrong amount entered", updatedReason)
  }
}
