package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.UiState
import com.example.i18n.AppLanguage
import com.example.i18n.stringsFor
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.TeacherPortalScreen
import com.example.ui.theme.SchoolTheme
import com.example.util.AttendanceUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AttendanceManagementRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val todayKey = AttendanceUtils.todayDateKey()
  private val yesterdayKey = AttendanceUtils.shiftDateKey(todayKey, -1)
  private val currentMonth = AttendanceUtils.currentMonthKey()

  private val sampleStudents = listOf(
    StudentRecord(
      id = "s1",
      admissionNo = "SRJP-01",
      rollNumber = "1",
      fullName = "Aarav Patel",
      fatherName = "Rajesh Patel",
      className = "Class 6",
      section = "A",
      status = StudentStatus.ACTIVE.name
    ),
    StudentRecord(
      id = "s2",
      admissionNo = "SRJP-02",
      rollNumber = "2",
      fullName = "Diya Verma",
      fatherName = "Suresh Verma",
      className = "Class 6",
      section = "B",
      status = StudentStatus.ACTIVE.name
    ),
    StudentRecord(
      id = "s3",
      admissionNo = "SRJP-03",
      rollNumber = "1",
      fullName = "Kabir Yadav",
      fatherName = "Mahesh Yadav",
      className = "Class 7",
      section = "A",
      status = StudentStatus.ACTIVE.name
    ),
    StudentRecord(
      id = "s4_inactive",
      admissionNo = "SRJP-04",
      rollNumber = "9",
      fullName = "Inactive Student",
      fatherName = "Parent",
      className = "Class 6",
      section = "A",
      status = StudentStatus.INACTIVE.name
    )
  )

  private val sampleAttendance = listOf(
    AttendanceRecord(
      id = "${todayKey.replace('-', '_')}_s1",
      studentId = "s1",
      studentName = "Aarav Patel",
      admissionNo = "SRJP-01",
      className = "Class 6",
      section = "A",
      dateKey = todayKey,
      status = AttendanceStatus.PRESENT.name,
      markedBy = "Principal"
    ),
    AttendanceRecord(
      id = "${yesterdayKey.replace('-', '_')}_s1",
      studentId = "s1",
      studentName = "Aarav Patel",
      admissionNo = "SRJP-01",
      className = "Class 6",
      section = "A",
      dateKey = yesterdayKey,
      status = AttendanceStatus.ABSENT.name,
      markedBy = "Ramesh Sir"
    ),
    AttendanceRecord(
      id = "${yesterdayKey.replace('-', '_')}_s2",
      studentId = "s2",
      studentName = "Diya Verma",
      admissionNo = "SRJP-02",
      className = "Class 6",
      section = "B",
      dateKey = yesterdayKey,
      status = AttendanceStatus.PRESENT.name,
      markedBy = "Ramesh Sir"
    )
  )

  @Test
  fun `attendance percentage and daily monthly student-wise report calculations are accurate`() {
    assertEquals(0.0, AttendanceUtils.calculateAttendancePercentage(0, 0), 0.001)
    assertEquals(75.0, AttendanceUtils.calculateAttendancePercentage(3, 4), 0.001)
    assertEquals("83.3%", AttendanceUtils.formatPercentage(5, 6))
    assertEquals("100%", AttendanceUtils.formatPercentage(4, 4))

    // Duplicate prevention checks
    assertTrue(AttendanceUtils.hasDuplicateAttendance(sampleAttendance, "s1", todayKey))
    assertFalse(AttendanceUtils.hasDuplicateAttendance(sampleAttendance, "s2", todayKey))

    // Deduplication removes duplicate student+date records from calculations
    val withDuplicate = sampleAttendance + AttendanceRecord(
      id = "legacy_dup_s1",
      studentId = "s1",
      studentName = "Aarav Patel",
      admissionNo = "SRJP-01",
      className = "Class 6",
      section = "A",
      dateKey = todayKey,
      status = AttendanceStatus.PRESENT.name,
      markedBy = "Duplicate"
    )
    val deduped = AttendanceUtils.deduplicateRecordsByStudentDate(withDuplicate)
    assertEquals(3, deduped.size)

    // Daily report for today (All classes, All sections) -> 3 active, 1 marked (s1 Present), 2 unmarked, 100% of marked
    val daily = AttendanceUtils.buildDailyAttendanceReport(
      students = sampleStudents,
      allRecords = withDuplicate,
      dateKey = todayKey,
      selectedClass = "All Classes",
      allClassesLabel = "All Classes",
      selectedSection = "All Sections",
      allSectionsLabel = "All Sections"
    )
    assertEquals(3, daily.totalStudents)
    assertEquals(1, daily.markedCount)
    assertEquals(2, daily.unmarkedCount)
    assertEquals(1, daily.presentCount)
    assertEquals(100.0, daily.attendancePercent, 0.001)

    // Section filtering (Section B -> only Diya)
    val sectionBStudents = AttendanceUtils.filterActiveStudents(
      students = sampleStudents,
      selectedClass = "Class 6",
      allClassesLabel = "All Classes",
      selectedSection = "B",
      allSectionsLabel = "All Sections"
    )
    assertEquals(listOf("s2"), sectionBStudents.map { it.id })

    // Monthly report for current month
    val monthly = AttendanceUtils.buildMonthlyAttendanceReport(
      students = sampleStudents,
      allRecords = sampleAttendance,
      monthKey = currentMonth,
      selectedClass = "All Classes",
      allClassesLabel = "All Classes",
      selectedSection = "All Sections",
      allSectionsLabel = "All Sections"
    )
    assertTrue(monthly.totalEntries >= 1)

    // Student-wise report
    val studentStats = AttendanceUtils.buildStudentAttendanceReport(
      students = sampleStudents,
      allRecords = sampleAttendance,
      selectedClass = "All Classes",
      allClassesLabel = "All Classes",
      selectedSection = "All Sections",
      allSectionsLabel = "All Sections",
      searchQuery = "Aarav"
    )
    assertEquals(1, studentStats.size)
    assertEquals(2, studentStats.first().totalMarked)
    assertEquals(1, studentStats.first().presentCount)
    assertEquals(1, studentStats.first().absentCount)
    assertEquals(50.0, studentStats.first().attendancePercent, 0.001)
  }

  @Test
  fun `admin marks attendance filters section prevents duplicates and edits saved record with reason`() {
    val strings = stringsFor(AppLanguage.EN)
    var markedStudentId: String? = null
    var markedStatus: AttendanceStatus? = null
    var editedRecordId: String? = null
    var editedStatus: AttendanceStatus? = null
    var editedReason: String? = null

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        AttendanceScreen(
          strings = strings,
          studentsState = UiState.Success(sampleStudents),
          attendanceState = UiState.Success(sampleAttendance),
          isSaving = false,
          isAdmin = true,
          onMarkAttendance = { student, _, status ->
            markedStudentId = student.id
            markedStatus = status
          },
          onMarkAllPresent = { _, _ -> },
          onEditAttendance = { record, newStatus, reason ->
            editedRecordId = record.id
            editedStatus = newStatus
            editedReason = reason
          }
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("attendance_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("duplicate_guard_banner").assertIsDisplayed()

    // Filter Section B -> only s2 is shown, s1 is hidden
    composeTestRule.onNodeWithTag("att_section_chip_B").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mark_daily_list").performScrollToNode(hasTestTag("att_student_card_s2"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("att_student_card_s2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("att_student_card_s1").assertDoesNotExist()

    // Mark unmarked s2 as Absent
    composeTestRule.onNodeWithTag("att_absent_s2").performClick()
    composeTestRule.waitForIdle()
    assertEquals("s2", markedStudentId)
    assertEquals(AttendanceStatus.ABSENT, markedStatus)

    // Switch back to All Sections and verify already-marked s1 shows locked badge & Admin Edit button
    composeTestRule.onNodeWithTag("att_section_chip_All Sections").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mark_daily_list").performScrollToNode(hasTestTag("att_student_card_s1"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("att_marked_badge_s1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("att_edit_btn_s1").performClick()
    composeTestRule.waitForIdle()

    // Admin Edit dialog opens -> change s1 to LEAVE with reason
    composeTestRule.onNodeWithTag("admin_edit_attendance_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("edit_status_leave").performClick()
    composeTestRule.onNodeWithTag("edit_attendance_reason_input").performTextInput("Medical leave note")
    composeTestRule.onNodeWithTag("save_edit_attendance_button").performClick()
    composeTestRule.waitForIdle()

    assertEquals("${todayKey.replace('-', '_')}_s1", editedRecordId)
    assertEquals(AttendanceStatus.LEAVE, editedStatus)
    assertEquals("Medical leave note", editedReason)

    // Verify Daily Report tab
    composeTestRule.onNodeWithTag("att_tab_1").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("daily_report_summary_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("daily_report_percent_badge").assertTextContains("100%", substring = true)

    // Verify Monthly Report tab
    composeTestRule.onNodeWithTag("att_tab_2").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("monthly_report_summary_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("monthly_working_days_text").assertIsDisplayed()

    // Verify Student-wise Report tab & search
    composeTestRule.onNodeWithTag("att_tab_3").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_att_search_input").performTextInput("Aarav")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_wise_report_list").performScrollToNode(hasTestTag("student_wise_card_s1"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_wise_card_s1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_wise_percent_s1").assertTextContains("50%", substring = true)
  }

  @Test
  fun `teacher portal provides daily attendance marking while preventing edits to already marked records`() {
    val strings = stringsFor(AppLanguage.EN)
    var markedId: String? = null
    var markedStatus: AttendanceStatus? = null
    var batchCount = 0

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        TeacherPortalScreen(
          strings = strings,
          teacherName = "Ramesh Sir",
          initialAssignedClass = "Class 6",
          studentsState = UiState.Success(sampleStudents),
          paymentsState = UiState.Success(emptyList()),
          attendanceState = UiState.Success(sampleAttendance),
          isSaving = false,
          onMarkAttendance = { student, _, status ->
            markedId = student.id
            markedStatus = status
          },
          onMarkAllPresent = { unmarkedList, _ ->
            batchCount = unmarkedList.size
          }
        )
      }
    }

    composeTestRule.waitForIdle()
    // Teacher portal lands on Attendance tab, prefiltered to Class 6 (s1 marked, s2 unmarked; s3 is Class 7)
    composeTestRule.onNodeWithTag("teacher_tab_attendance").assertIsDisplayed()
    composeTestRule.onNodeWithTag("teacher_tab_roster").assertIsDisplayed()

    composeTestRule.onNodeWithTag("mark_daily_list").performScrollToNode(hasTestTag("att_student_card_s1"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("att_marked_badge_s1").assertIsDisplayed()
    // Teacher does NOT have Edit button on marked s1
    composeTestRule.onNodeWithTag("att_edit_btn_s1").assertDoesNotExist()
    // Clicking disabled status on already-marked s1 must NOT fire duplicate callback
    composeTestRule.onNodeWithTag("att_absent_s1").performClick()
    composeTestRule.waitForIdle()
    assertNull(markedId)

    // Teacher CAN mark unmarked s2 in Class 6
    composeTestRule.onNodeWithTag("mark_daily_list").performScrollToNode(hasTestTag("att_student_card_s2"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("att_present_s2").performClick()
    composeTestRule.waitForIdle()
    assertEquals("s2", markedId)
    assertEquals(AttendanceStatus.PRESENT, markedStatus)

    // Mark All Unmarked Present only sends unmarked Class 6 student (s2 -> count 1)
    composeTestRule.onNodeWithTag("mark_daily_list").performScrollToIndex(0)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mark_all_present_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(1, batchCount)

    // Switching to Class Roster & Fees tab still works
    composeTestRule.onNodeWithTag("teacher_tab_roster").performClick()
    composeTestRule.waitForIdle()
  }
}
