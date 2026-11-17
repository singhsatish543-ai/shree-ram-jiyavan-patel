package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ExamResult
import com.example.data.model.ExamSchedule
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.Gender
import com.example.data.model.HomeworkItem
import com.example.data.model.NoticeAudience
import com.example.data.model.NoticePriority
import com.example.data.model.PaymentMode
import com.example.data.model.SchoolNotice
import com.example.data.model.SchoolSettings
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.TeacherRecord
import com.example.data.model.TimetableEntry
import com.example.data.model.UiState
import com.example.i18n.AppLanguage
import com.example.i18n.stringsFor
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.theme.SchoolTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SRJP School", appName)
  }

  @Test
  fun `admin dashboard shows loading empty and error states accurately`() {
    val strings = stringsFor(AppLanguage.EN)

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        AdminDashboardScreen(
          strings = strings,
          adminDisplayName = "Principal",
          studentsState = UiState.Loading,
          teachersState = UiState.Loading,
          paymentsState = UiState.Loading,
          structuresState = UiState.Loading,
          attendanceState = UiState.Loading,
          noticesState = UiState.Loading,
          schoolSettings = null,
          onNavigate = { _, _ -> },
          onReceiptClick = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admin_dashboard_loading").assertIsDisplayed()
  }

  @Test
  fun `admin dashboard displays error state when firestore fails`() {
    val strings = stringsFor(AppLanguage.EN)

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        AdminDashboardScreen(
          strings = strings,
          adminDisplayName = "Principal",
          studentsState = UiState.Error("Firestore unavailable"),
          teachersState = UiState.Success(emptyList()),
          paymentsState = UiState.Success(emptyList()),
          structuresState = UiState.Success(emptyList()),
          attendanceState = UiState.Success(emptyList()),
          noticesState = UiState.Success(emptyList()),
          schoolSettings = null,
          onNavigate = { _, _ -> },
          onReceiptClick = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admin_dashboard_error").assertIsDisplayed()
    composeTestRule.onNodeWithText("Firestore unavailable").assertIsDisplayed()
  }

  @Test
  fun `admin dashboard displays live computed statistics and quick summary`() {
    val strings = stringsFor(AppLanguage.EN)
    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    val todayDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())

    val students = listOf(
      StudentRecord(
        id = "s1",
        admissionNo = "ADM01",
        rollNumber = "1",
        fullName = "Aarav Patel",
        className = "Class 6",
        gender = Gender.MALE.name,
        monthlyFeeAmount = 800L,
        status = StudentStatus.ACTIVE.name
      ),
      StudentRecord(
        id = "s2",
        admissionNo = "ADM02",
        rollNumber = "2",
        fullName = "Ananya Verma",
        className = "Class 7",
        gender = Gender.FEMALE.name,
        monthlyFeeAmount = 900L,
        status = StudentStatus.ACTIVE.name
      )
    )
    val teachers = listOf(
      TeacherRecord(id = "t1", staffCode = "T01", fullName = "Ramesh Sir", subject = "Maths")
    )
    val payments = listOf(
      FeePayment(
        id = "p1",
        receiptNo = "SRJP-001",
        studentId = "s1",
        studentName = "Aarav Patel",
        admissionNo = "ADM01",
        className = "Class 6",
        academicYear = "2026-27",
        monthIndex = 8L,
        monthName = "November",
        tuitionFee = 800L,
        totalAmount = 800L,
        paymentMode = PaymentMode.UPI.name,
        paidOnDate = todayDate
      )
    )
    val attendance = listOf(
      AttendanceRecord(
        id = "a1",
        studentId = "s1",
        studentName = "Aarav Patel",
        className = "Class 6",
        dateKey = todayKey,
        status = AttendanceStatus.PRESENT.name
      )
    )

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        AdminPortalScaffold(
          strings = strings,
          language = AppLanguage.EN,
          onLanguageChange = {},
          adminDisplayName = "Principal",
          currentUserId = "admin-uid",
          schoolSettings = SchoolSettings(
            academicSession = "2026-27",
            totalConfiguredClasses = 11L
          ),
          studentsState = UiState.Success(students),
          teachersState = UiState.Success(teachers),
          feePaymentsState = UiState.Success(payments),
          feeStructuresState = UiState.Success(listOf(FeeStructure(className = "Class 6", monthlyTuitionFee = 800L))),
          attendanceState = UiState.Success(attendance),
          homeworkState = UiState.Success(emptyList()),
          examsState = UiState.Success(emptyList()),
          resultsState = UiState.Success(emptyList()),
          noticesState = UiState.Success(listOf(SchoolNotice(id = "n1", title = "Winter Timing", body = "8:30 AM"))),
          timetableState = UiState.Success(emptyList()),
          isSaving = false
        )
      }
    }

    composeTestRule.waitForIdle()
    listOf(
      "stat_total_students",
      "stat_total_teachers",
      "stat_total_classes",
      "stat_todays_attendance",
      "stat_total_fee_collection",
      "stat_pending_fees"
    ).forEach { tag ->
      composeTestRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
    }
    composeTestRule.onNodeWithTag("admin_dashboard_list")
      .performScrollToNode(hasTestTag("fees_quick_summary_card"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fees_quick_summary_card").assertIsDisplayed()
  }

  @Test
  fun `admin dashboard all 11 navigation cards and 5 bottom nav tabs open respective screens`() {
    val strings = stringsFor(AppLanguage.EN)

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        AdminPortalScaffold(
          strings = strings,
          language = AppLanguage.EN,
          onLanguageChange = {},
          adminDisplayName = "Principal",
          currentUserId = "admin-uid",
          schoolSettings = SchoolSettings(academicSession = "2026-27", totalConfiguredClasses = 11L),
          studentsState = UiState.Success(listOf(StudentRecord(id = "s1", admissionNo = "A1", fullName = "Aarav", className = "Class 6"))),
          teachersState = UiState.Success(listOf(TeacherRecord(id = "t1", staffCode = "T1", fullName = "Ramesh", subject = "Hindi"))),
          feePaymentsState = UiState.Success(emptyList()),
          feeStructuresState = UiState.Success(emptyList()),
          attendanceState = UiState.Success(emptyList()),
          homeworkState = UiState.Success(listOf(HomeworkItem(id = "h1", className = "Class 6", subject = "Maths", title = "Ex 1", description = "Solve Q1-5", dueDate = "Tomorrow"))),
          examsState = UiState.Success(listOf(ExamSchedule(id = "e1", examName = "Half Yearly", className = "Class 6", subject = "Maths", examDate = "10 Dec", maxMarks = 100L))),
          resultsState = UiState.Success(listOf(ExamResult(id = "r1", examName = "Half Yearly", studentName = "Aarav", className = "Class 6", subject = "Maths", marksObtained = 88L, maxMarks = 100L, grade = "A"))),
          noticesState = UiState.Success(listOf(SchoolNotice(id = "n1", title = "PTM", body = "Saturday 10 AM", audience = NoticeAudience.ALL.name, priority = NoticePriority.IMPORTANT.name))),
          timetableState = UiState.Success(listOf(TimetableEntry(id = "tt1", className = "Class 6", dayOfWeek = "Monday", periodLabel = "1st", subject = "Maths", teacherName = "Ramesh"))),
          isSaving = false
        )
      }
    }
    composeTestRule.waitForIdle()

    // Test all 11 Module Navigation Cards + Return Back to Dashboard
    val moduleCardToScreenTag = listOf(
      "nav_card_students" to "student_search_input",
      "nav_card_teachers" to "teacher_management_screen",
      "nav_card_fees" to "fees_tab_0",
      "nav_card_attendance" to "attendance_screen",
      "nav_card_homework" to "homework_screen",
      "nav_card_examination" to "examination_screen",
      "nav_card_results" to "results_screen",
      "nav_card_notices" to "notice_board_screen",
      "nav_card_timetable" to "timetable_screen",
      "nav_card_reports" to "reports_screen",
      "nav_card_settings" to "school_settings_screen"
    )

    moduleCardToScreenTag.forEach { (cardTag, expectedScreenTag) ->
      composeTestRule.onNodeWithTag("admin_dashboard_list")
        .performScrollToNode(hasTestTag(cardTag))
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(cardTag).performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(expectedScreenTag).assertIsDisplayed()

      // Return to Home via TopBar Back button
      composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("admin_dashboard_list").assertIsDisplayed()
    }

    // Test Header Notification Bell -> Notice Board
    composeTestRule.onNodeWithTag("header_notifications_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("notice_board_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
    composeTestRule.waitForIdle()

    // Test Header Admin Profile Pill -> Admin Profile
    composeTestRule.onNodeWithTag("header_admin_profile").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admin_profile_screen").assertIsDisplayed()

    // Test all 5 Bottom Navigation Tabs (Home, Students, Fees, Reports, Profile)
    composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admin_dashboard_list").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bottom_nav_students").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_search_input").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bottom_nav_fees").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fees_tab_0").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bottom_nav_reports").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("reports_screen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admin_profile_screen").assertIsDisplayed()
  }
}
