package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FeeStructure
import com.example.data.model.Gender
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.UiState
import com.example.i18n.AppLanguage
import com.example.i18n.stringsFor
import com.example.ui.screens.StudentManagementScreen
import com.example.ui.theme.SchoolTheme
import com.example.util.StudentExportUtils
import org.junit.Assert.assertEquals
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
class StudentManagementRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `add student auto generates unique admission no validates duplicates and saves 15 fields`() {
    val strings = stringsFor(AppLanguage.EN)
    val existing = listOf(
      StudentRecord(
        id = "s1",
        admissionNo = "SRJP-2026-0001",
        rollNumber = "1",
        fullName = "Aarav Patel",
        fatherName = "Rajesh Patel",
        className = "Class 6",
        section = "A",
        phone = "9876543210",
        monthlyFeeAmount = 600L
      )
    )

    var savedAdmissionNo: String? = null
    var savedFullName: String? = null
    var savedPreviousSchool: String? = null
    var saveCallCount = 0

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        StudentManagementScreen(
          strings = strings,
          studentsState = UiState.Success(existing),
          feeStructures = listOf(FeeStructure(className = "Class 6", monthlyTuitionFee = 600L)),
          activeSession = "2026-27",
          isSaving = false,
          initialOpenAddDialog = false,
          onConsumedInitialAddDialog = {},
          onSaveStudent = { _, adm, _, name, _, _, _, _, _, _, _, _, _, _, _, _, _, prevSchool, _, onDone ->
            saveCallCount++
            savedAdmissionNo = adm
            savedFullName = name
            savedPreviousSchool = prevSchool
            onDone()
          },
          onDeleteStudent = {},
          onCollectFeeForStudent = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_mgmt_total_count").assertIsDisplayed()
    composeTestRule.onNodeWithText("Total Students: 1").assertIsDisplayed()

    // Open Add Student dialog -> Admission No should auto-populate SRJP-2026-0002
    composeTestRule.onNodeWithTag("add_student_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("add_edit_student_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_admission_input").assertTextContains("SRJP-2026-0002")

    // Saving empty required fields shows validation error
    composeTestRule.onNodeWithTag("save_student_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_validation_error").assertTextContains(strings.validationRequiredField)
    assertEquals(0, saveCallCount)

    // Enter valid fields but duplicate admission number (case-insensitive check)
    composeTestRule.onNodeWithTag("student_admission_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("student_admission_input").performTextInput("srjp-2026-0001")
    composeTestRule.onNodeWithTag("student_roll_input").performScrollTo().performTextInput("12")
    composeTestRule.onNodeWithTag("student_name_input").performScrollTo().performTextInput("Ananya Verma")
    composeTestRule.onNodeWithTag("student_father_input").performScrollTo().performTextInput("Suresh Verma")
    composeTestRule.onNodeWithTag("student_mother_input").performScrollTo().performTextInput("Sunita Verma")
    composeTestRule.onNodeWithTag("student_dob_input").performScrollTo().performTextInput("14 Aug 2014")
    composeTestRule.onNodeWithTag("student_phone_input").performScrollTo().performTextInput("9876501234")
    composeTestRule.onNodeWithTag("student_address_input").performScrollTo().performTextInput("Bechu Ka Purwa, Airayan")
    composeTestRule.onNodeWithTag("student_prev_school_input").performScrollTo().performTextInput("Bal Vidya Mandir")

    composeTestRule.onNodeWithTag("save_student_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_validation_error").assertTextContains(strings.duplicateAdmissionError)
    assertEquals(0, saveCallCount)

    // Auto-generate unique Admission No & save
    composeTestRule.onNodeWithTag("auto_admission_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_admission_input").assertTextContains("SRJP-2026-0002")
    composeTestRule.onNodeWithTag("save_student_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    assertEquals(1, saveCallCount)
    assertEquals("SRJP-2026-0002", savedAdmissionNo)
    assertEquals("Ananya Verma", savedFullName)
    assertEquals("Bal Vidya Mandir", savedPreviousSchool)
  }

  @Test
  fun `search class filter view profile edit and toggle status work end to end`() {
    val strings = stringsFor(AppLanguage.EN)
    val students = listOf(
      StudentRecord(
        id = "s1",
        admissionNo = "SRJP-2026-0001",
        rollNumber = "1",
        fullName = "Kavya Sharma",
        fatherName = "Dinesh Sharma",
        motherName = "Meera Sharma",
        dateOfBirth = "10 May 2021",
        className = "PG",
        section = "A",
        gender = Gender.FEMALE.name,
        phone = "9123456789",
        address = "Khaga, Fatehpur",
        admissionDate = "02 Apr 2026",
        academicSession = "2026-27",
        previousSchool = "First Steps Playway",
        monthlyFeeAmount = 500L,
        status = StudentStatus.ACTIVE.name
      ),
      StudentRecord(
        id = "s2",
        admissionNo = "SRJP-2026-0002",
        rollNumber = "7",
        fullName = "Rohan Verma",
        fatherName = "Mahesh Verma",
        className = "Class 8",
        section = "B",
        gender = Gender.MALE.name,
        phone = "9988776655",
        monthlyFeeAmount = 850L,
        status = StudentStatus.ACTIVE.name
      )
    )

    var toggledId: String? = null
    var toggledStatus: StudentStatus? = null
    var editedId: String? = null
    var editedName: String? = null

    composeTestRule.setContent {
      SchoolTheme(language = AppLanguage.EN) {
        StudentManagementScreen(
          strings = strings,
          studentsState = UiState.Success(students),
          feeStructures = emptyList(),
          isSaving = false,
          initialOpenAddDialog = false,
          onConsumedInitialAddDialog = {},
          onSaveStudent = { existingId, _, _, name, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, onDone ->
            editedId = existingId
            editedName = name
            onDone()
          },
          onToggleStudentStatus = { id, status ->
            toggledId = id
            toggledStatus = status
          },
          onDeleteStudent = {},
          onCollectFeeForStudent = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_card_s1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("student_card_s2"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_card_s2").assertIsDisplayed()

    // Search student by name
    composeTestRule.onNodeWithTag("student_search_input").performTextInput("Kavya")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_card_s1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_card_s2").assertDoesNotExist()

    // Clear search & filter by Class 8
    composeTestRule.onNodeWithTag("student_search_input").performTextClearance()
    composeTestRule.onNodeWithTag("class_filter_class_8").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_card_s2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_card_s1").assertDoesNotExist()

    // Switch back to All Classes and View Student Profile (s1)
    composeTestRule.onNodeWithTag("class_filter_all").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("student_card_s1"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_view_s1").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("student_profile_dialog").assertIsDisplayed()
    val inProfile = hasAnyAncestor(hasTestTag("student_profile_dialog"))
    composeTestRule.onNode(hasText("Meera Sharma") and inProfile).performScrollTo().assertIsDisplayed()
    composeTestRule.onNode(hasText("First Steps Playway") and inProfile).performScrollTo().assertIsDisplayed()
    composeTestRule.onNode(hasText("Khaga, Fatehpur") and inProfile).performScrollTo().assertIsDisplayed()

    // Toggle Active/Inactive switch from profile
    composeTestRule.onNodeWithTag("profile_status_switch").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("s1", toggledId)
    assertEquals(StudentStatus.INACTIVE, toggledStatus)

    // Edit student from profile
    composeTestRule.onNodeWithTag("profile_edit_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("add_edit_student_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_name_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("student_name_input").performTextInput("Kavya K. Sharma")
    composeTestRule.onNodeWithTag("save_student_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    assertEquals("s1", editedId)
    assertEquals("Kavya K. Sharma", editedName)
  }

  @Test
  fun `pdf and excel exports generate valid non empty files with student data`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val strings = stringsFor(AppLanguage.EN)
    val sampleStudents = listOf(
      StudentRecord(
        id = "s1",
        admissionNo = "SRJP-2026-0001",
        rollNumber = "1",
        fullName = "Aarav Patel",
        fatherName = "Rajesh Patel",
        motherName = "Seema Patel",
        dateOfBirth = "15 Aug 2015",
        className = "PG",
        section = "A",
        gender = Gender.MALE.name,
        phone = "9876543210",
        address = "Airayan, Khaga",
        admissionDate = "01 Apr 2026",
        academicSession = "2026-27",
        previousSchool = "None",
        monthlyFeeAmount = 500L,
        status = StudentStatus.ACTIVE.name
      )
    )

    val pdfFile = StudentExportUtils.exportStudentsToPdf(
      context = context,
      students = sampleStudents,
      strings = strings,
      classFilter = null,
      academicSession = "2026-27"
    )
    assertTrue("PDF file should exist", pdfFile.exists())
    assertTrue("PDF file should be non-empty", pdfFile.length() > 300)
    val pdfContent = FileInputStream(pdfFile).use { it.readBytes().toString(Charsets.ISO_8859_1) }
    assertTrue("PDF should start with %PDF-", pdfContent.startsWith("%PDF-"))
    assertTrue("PDF should end with %%EOF", pdfContent.trim().endsWith("%%EOF"))
    assertTrue("PDF should contain student name", pdfContent.contains("Aarav Patel"))

    val excelFile = StudentExportUtils.exportStudentsToExcel(
      context = context,
      students = sampleStudents,
      strings = strings,
      classFilter = "PG",
      academicSession = "2026-27"
    )
    assertTrue("Excel file should exist", excelFile.exists())
    assertTrue("Excel file should be non-empty", excelFile.length() > 300)

    val entries = mutableMapOf<String, String>()
    ZipInputStream(FileInputStream(excelFile)).use { zis ->
      var entry = zis.nextEntry
      while (entry != null) {
        entries[entry.name] = zis.readBytes().toString(Charsets.UTF_8)
        zis.closeEntry()
        entry = zis.nextEntry
      }
    }
    assertTrue(entries.containsKey("[Content_Types].xml"))
    assertTrue(entries.containsKey("xl/workbook.xml"))
    val sheetXml = entries["xl/worksheets/sheet1.xml"] ?: ""
    assertTrue(sheetXml.contains("SRJP-2026-0001"))
    assertTrue(sheetXml.contains("Aarav Patel"))
    assertTrue(sheetXml.contains("Seema Patel"))
    assertTrue(sheetXml.contains("15 Aug 2015"))
  }
}
