package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.auth.signOutSchoolPortal
import com.example.data.model.AdminDestination
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
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.PortalRole
import com.example.data.model.SchoolNotice
import com.example.data.model.SchoolSettings
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.TeacherRecord
import com.example.data.model.TimetableEntry
import com.example.data.model.UiState
import com.example.data.repository.AcademicRepository
import com.example.data.repository.SchoolRepository
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.LanguagePreferences
import com.example.i18n.stringsFor
import com.example.ui.components.ReceiptPreviewDialog
import com.example.ui.components.SchoolTopBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdminProfileScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.ExaminationScreen
import com.example.ui.screens.FeesCollectionScreen
import com.example.ui.screens.HomeworkScreen
import com.example.ui.screens.NoticeBoardScreen
import com.example.ui.screens.PendingLoginMetadata
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.SchoolSettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentLoginScreen
import com.example.ui.screens.StudentManagementScreen
import com.example.ui.screens.StudentPortalScreen
import com.example.ui.screens.TeacherLoginScreen
import com.example.ui.screens.TeacherManagementScreen
import com.example.ui.screens.TeacherPortalScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SchoolTheme
import com.example.ui.theme.SkyBlue100
import com.example.ui.theme.Slate500
import com.example.ui.viewmodel.SchoolViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

object Routes {
  const val SPLASH = "splash"
  const val ADMIN_LOGIN = "admin_login"
  const val TEACHER_LOGIN = "teacher_login"
  const val STUDENT_LOGIN = "student_login"
  const val ADMIN_DASHBOARD = "admin_dashboard"
  const val STUDENT_MANAGEMENT = "student_management"
  const val FEES_COLLECTION = "fees_collection"
  const val TEACHER_PORTAL = "teacher_portal"
  const val STUDENT_PORTAL = "student_portal"
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth ->
    trySend(auth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val languagePrefs = LanguagePreferences(applicationContext)

    setContent {
      val scope = rememberCoroutineScope()
      val language by languagePrefs.languageFlow.collectAsStateWithLifecycle(initialValue = AppLanguage.EN)
      val strings = remember(language) { stringsFor(language) }

      SchoolTheme(language = language) {
        SchoolAppRoot(
          strings = strings,
          language = language,
          onLanguageChange = { newLang ->
            scope.launch { languagePrefs.setLanguage(newLang) }
          }
        )
      }
    }
  }
}

@Composable
private fun SchoolAppRoot(
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit
) {
  val auth = Firebase.auth
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  var showSplash by remember { mutableStateOf(true) }
  var pendingLoginMeta by remember { mutableStateOf<PendingLoginMetadata?>(null) }

  if (showSplash) {
    SplashScreen(
      strings = strings,
      language = language,
      onLanguageChange = onLanguageChange,
      onContinue = { showSplash = false }
    )
    return
  }

  val user = currentUser
  if (user == null) {
    UnauthenticatedLoginNav(
      strings = strings,
      language = language,
      onLanguageChange = onLanguageChange,
      onLoginSuccess = { meta ->
        pendingLoginMeta = meta
      }
    )
  } else {
    key(user.uid) {
      AuthenticatedSchoolApp(
        currentUserId = user.uid,
        authDisplayName = user.displayName ?: "",
        pendingLoginMeta = pendingLoginMeta,
        onConsumedPendingMeta = { pendingLoginMeta = null },
        strings = strings,
        language = language,
        onLanguageChange = onLanguageChange
      )
    }
  }
}

@Composable
private fun UnauthenticatedLoginNav(
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onLoginSuccess: (PendingLoginMetadata) -> Unit
) {
  val navController = rememberNavController()

  val switchToRole: (PortalRole) -> Unit = { role ->
    val targetRoute = when (role) {
      PortalRole.ADMIN -> Routes.ADMIN_LOGIN
      PortalRole.TEACHER -> Routes.TEACHER_LOGIN
      PortalRole.STUDENT -> Routes.STUDENT_LOGIN
    }
    navController.navigate(targetRoute) {
      popUpTo(Routes.ADMIN_LOGIN) { inclusive = false }
      launchSingleTop = true
    }
  }

  NavHost(
    navController = navController,
    startDestination = Routes.ADMIN_LOGIN
  ) {
    composable(Routes.ADMIN_LOGIN) {
      AdminLoginScreen(
        strings = strings,
        language = language,
        onLanguageChange = onLanguageChange,
        onSelectRoleScreen = switchToRole,
        onLoginSuccess = onLoginSuccess
      )
    }
    composable(Routes.TEACHER_LOGIN) {
      TeacherLoginScreen(
        strings = strings,
        language = language,
        onLanguageChange = onLanguageChange,
        onSelectRoleScreen = switchToRole,
        onLoginSuccess = onLoginSuccess
      )
    }
    composable(Routes.STUDENT_LOGIN) {
      StudentLoginScreen(
        strings = strings,
        language = language,
        onLanguageChange = onLanguageChange,
        onSelectRoleScreen = switchToRole,
        onLoginSuccess = onLoginSuccess
      )
    }
  }
}

@Composable
private fun AuthenticatedSchoolApp(
  currentUserId: String,
  authDisplayName: String,
  pendingLoginMeta: PendingLoginMetadata?,
  onConsumedPendingMeta: () -> Unit,
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val viewModel: SchoolViewModel = viewModel(
    key = currentUserId,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        SchoolViewModel(
          repository = SchoolRepository(app.applicationContext),
          academicRepository = AcademicRepository(app.applicationContext),
          currentUserId = currentUserId,
          authDisplayName = authDisplayName
        )
      }
    }
  )

  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val schoolSettings by viewModel.schoolSettings.collectAsStateWithLifecycle()
  val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
  val feePaymentsState by viewModel.feePaymentsState.collectAsStateWithLifecycle()
  val feeStructuresState by viewModel.feeStructuresState.collectAsStateWithLifecycle()
  val teachersState by viewModel.teachersState.collectAsStateWithLifecycle()
  val attendanceState by viewModel.attendanceState.collectAsStateWithLifecycle()
  val homeworkState by viewModel.homeworkState.collectAsStateWithLifecycle()
  val examsState by viewModel.examsState.collectAsStateWithLifecycle()
  val resultsState by viewModel.resultsState.collectAsStateWithLifecycle()
  val noticesState by viewModel.noticesState.collectAsStateWithLifecycle()
  val timetableState by viewModel.timetableState.collectAsStateWithLifecycle()
  val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
  val latestReceipt by viewModel.latestReceipt.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) {
    viewModel.events.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  var activeRole by remember { mutableStateOf(pendingLoginMeta?.role ?: PortalRole.ADMIN) }
  var roleSyncedFromProfile by remember { mutableStateOf(pendingLoginMeta != null) }

  LaunchedEffect(pendingLoginMeta) {
    if (pendingLoginMeta != null) {
      activeRole = pendingLoginMeta.role
      roleSyncedFromProfile = true
      viewModel.syncUserPortalProfile(
        displayName = pendingLoginMeta.displayName.ifBlank { authDisplayName },
        portalRole = pendingLoginMeta.role,
        language = language,
        staffCode = pendingLoginMeta.staffCode,
        admissionNo = pendingLoginMeta.admissionNo,
        assignedClass = pendingLoginMeta.assignedClass
      )
      onConsumedPendingMeta()
    }
  }

  LaunchedEffect(userProfile) {
    val profile = userProfile
    if (profile != null && !roleSyncedFromProfile && pendingLoginMeta == null) {
      activeRole = PortalRole.fromRaw(profile.portalMode)
      roleSyncedFromProfile = true
    }
  }

  var showRoleSwitcherLogin by remember { mutableStateOf<PortalRole?>(null) }

  val switcherRole = showRoleSwitcherLogin
  if (switcherRole != null) {
    BackHandler { showRoleSwitcherLogin = null }
    when (switcherRole) {
      PortalRole.ADMIN -> AdminLoginScreen(
        strings = strings,
        language = language,
        onLanguageChange = onLanguageChange,
        onSelectRoleScreen = { showRoleSwitcherLogin = it },
        onLoginSuccess = { meta ->
          activeRole = meta.role
          viewModel.syncUserPortalProfile(meta.displayName, meta.role, language)
          showRoleSwitcherLogin = null
        }
      )

      PortalRole.TEACHER -> TeacherLoginScreen(
        strings = strings,
        language = language,
        onLanguageChange = onLanguageChange,
        onSelectRoleScreen = { showRoleSwitcherLogin = it },
        onLoginSuccess = { meta ->
          activeRole = meta.role
          viewModel.syncUserPortalProfile(
            meta.displayName, meta.role, language,
            staffCode = meta.staffCode, assignedClass = meta.assignedClass
          )
          showRoleSwitcherLogin = null
        }
      )

      PortalRole.STUDENT -> StudentLoginScreen(
        strings = strings,
        language = language,
        onLanguageChange = onLanguageChange,
        onSelectRoleScreen = { showRoleSwitcherLogin = it },
        onLoginSuccess = { meta ->
          activeRole = meta.role
          viewModel.syncUserPortalProfile(
            meta.displayName, meta.role, language,
            admissionNo = meta.admissionNo
          )
          showRoleSwitcherLogin = null
        }
      )
    }
    return
  }

  val resolvedUserName = userProfile?.displayName?.ifBlank { authDisplayName }?.ifBlank { "Principal" }
    ?: authDisplayName.ifBlank { "Principal" }

  val handleSignOut: () -> Unit = {
    signOutSchoolPortal(credentialManager, scope)
  }

  val handleSwitchRole: () -> Unit = {
    showRoleSwitcherLogin = activeRole
  }

  if (activeRole == PortalRole.ADMIN) {
    AdminPortalScaffold(
      strings = strings,
      language = language,
      onLanguageChange = onLanguageChange,
      adminDisplayName = resolvedUserName,
      currentUserId = currentUserId,
      schoolSettings = schoolSettings,
      studentsState = studentsState,
      teachersState = teachersState,
      feePaymentsState = feePaymentsState,
      feeStructuresState = feeStructuresState,
      attendanceState = attendanceState,
      homeworkState = homeworkState,
      examsState = examsState,
      resultsState = resultsState,
      noticesState = noticesState,
      timetableState = timetableState,
      isSaving = isSaving,
      snackbarHostState = snackbarHostState,
      onSwitchRole = handleSwitchRole,
      onSignOut = handleSignOut,
      onReceiptClick = { viewModel.showReceipt(it) },
      onSaveStudent = { id, adm, roll, name, father, mother, cls, sec, gender, phone, addr, fee, status, photoUrl, dob, admDate, session, prevSchool, photoBytes, onDone ->
        viewModel.saveStudent(
          id, adm, roll, name, father, mother, cls, sec, gender, phone, addr, fee, status,
          photoUrl, dob, admDate, session, prevSchool, photoBytes,
          strings.studentSavedMsg, onDone
        )
      },
      onToggleStudentStatus = { studentId, newStatus ->
        viewModel.toggleStudentStatus(studentId, newStatus, strings.studentSavedMsg)
      },
      onDeleteStudent = { studentId ->
        viewModel.deleteStudent(studentId, strings.studentDeletedMsg)
      },
      onShowStudentMessage = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
      },
      onCollectFee = { student, year, mIdx, mName, tuition, other, late, discount, prevBal, amtPaid, payType, mode, remarks, collector, onDone ->
        viewModel.collectFeePayment(
          student = student,
          academicYear = year,
          monthIndex = mIdx,
          monthName = mName,
          tuitionFee = tuition,
          otherFee = other,
          lateFee = late,
          discount = discount,
          previousBalance = prevBal,
          amountPaid = amtPaid,
          paymentType = payType,
          paymentMode = mode,
          remarks = remarks,
          collectedBy = collector,
          successMessage = strings.feeCollectedSuccess,
          onSuccess = { onDone() }
        )
      },
      onSaveFeeStructure = { session, cls, tuition, admission, exam, development, otherCharges, lateFee, discount, onDone ->
        viewModel.saveFeeStructure(
          academicSession = session,
          className = cls,
          monthlyTuitionFee = tuition,
          admissionFee = admission,
          examFee = exam,
          developmentFee = development,
          otherCharges = otherCharges,
          lateFee = lateFee,
          discount = discount,
          computerFee = 0L,
          transportFee = 0L,
          successMessage = strings.structureSavedMsg,
          onSuccess = onDone
        )
      },
      onSaveTeacher = { id, code, name, subj, cls, phone, qual, status, onDone ->
        viewModel.saveTeacher(id, code, name, subj, cls, phone, qual, status, strings.teacherSavedMsg, onDone)
      },
      onDeleteTeacher = { teacherId ->
        viewModel.deleteTeacher(teacherId, strings.teacherDeletedMsg)
      },
      onMarkAttendance = { student, dateKey, status ->
        val session = schoolSettings?.academicSession?.ifBlank { "2026-27" } ?: "2026-27"
        viewModel.markAttendance(
          student = student,
          dateKey = dateKey,
          status = status,
          markedBy = resolvedUserName,
          academicSession = session,
          successMessage = strings.attendanceSavedMsg
        )
      },
      onMarkAllPresent = { studentsList, dateKey ->
        val session = schoolSettings?.academicSession?.ifBlank { "2026-27" } ?: "2026-27"
        viewModel.markAllUnmarkedPresent(
          students = studentsList,
          dateKey = dateKey,
          markedBy = resolvedUserName,
          academicSession = session,
          successMessage = strings.attendanceSavedMsg
        )
      },
      onEditAttendance = { record, newStatus, reason ->
        viewModel.editAttendanceRecord(
          record = record,
          newStatus = newStatus,
          editedBy = resolvedUserName,
          editReason = reason,
          successMessage = strings.attendanceUpdatedMsg
        )
      },
      onAddHomework = { cls, subj, title, desc, due, onDone ->
        viewModel.addHomework(cls, subj, title, desc, due, resolvedUserName, strings.homeworkSavedMsg, onDone)
      },
      onDeleteHomework = { id ->
        viewModel.deleteHomework(id, strings.studentDeletedMsg)
      },
      onAddExam = { name, cls, subj, date, marks, room, onDone ->
        viewModel.addExam(name, cls, subj, date, marks, room, strings.examSavedMsg, onDone)
      },
      onDeleteExam = { id ->
        viewModel.deleteExam(id, strings.studentDeletedMsg)
      },
      onSaveResult = { exam, student, marks, remarks, onDone ->
        viewModel.saveResult(exam, student, marks, remarks, strings.resultSavedMsg, onDone)
      },
      onDeleteResult = { id ->
        viewModel.deleteResult(id, strings.studentDeletedMsg)
      },
      onAddNotice = { title, body, audience, priority, onDone ->
        viewModel.addNotice(title, body, audience, priority, strings.noticeSavedMsg, onDone)
      },
      onDeleteNotice = { id ->
        viewModel.deleteNotice(id, strings.studentDeletedMsg)
      },
      onAddTimetableEntry = { cls, day, period, subj, teacher, onDone ->
        viewModel.addTimetableEntry(cls, day, period, subj, teacher, strings.timetableSavedMsg, onDone)
      },
      onDeleteTimetableEntry = { id ->
        viewModel.deleteTimetableEntry(id, strings.studentDeletedMsg)
      },
      onSaveSchoolSettings = { name, addr, session, principal, phone, classesCount ->
        viewModel.saveSchoolSettings(name, addr, session, principal, phone, classesCount, strings.settingsSavedMsg)
      }
    )
  } else {
    Scaffold(
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        SchoolTopBar(
          title = strings.schoolShortName,
          subtitle = strings.schoolAddress,
          language = language,
          onLanguageChange = onLanguageChange,
          onSwitchRole = handleSwitchRole,
          onSignOut = handleSignOut,
          strings = strings
        )
      }
    ) { innerPadding ->
      Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        when (activeRole) {
          PortalRole.TEACHER -> TeacherPortalScreen(
            strings = strings,
            teacherName = resolvedUserName,
            initialAssignedClass = userProfile?.assignedClass ?: "",
            studentsState = studentsState,
            paymentsState = feePaymentsState,
            attendanceState = attendanceState,
            isSaving = isSaving,
            activeSession = schoolSettings?.academicSession?.ifBlank { "2026-27" } ?: "2026-27",
            onMarkAttendance = { student, dateKey, status ->
              val session = schoolSettings?.academicSession?.ifBlank { "2026-27" } ?: "2026-27"
              viewModel.markAttendance(
                student = student,
                dateKey = dateKey,
                status = status,
                markedBy = resolvedUserName,
                academicSession = session,
                successMessage = strings.attendanceSavedMsg
              )
            },
            onMarkAllPresent = { studentsList, dateKey ->
              val session = schoolSettings?.academicSession?.ifBlank { "2026-27" } ?: "2026-27"
              viewModel.markAllUnmarkedPresent(
                students = studentsList,
                dateKey = dateKey,
                markedBy = resolvedUserName,
                academicSession = session,
                successMessage = strings.attendanceSavedMsg
              )
            }
          )

          PortalRole.STUDENT -> StudentPortalScreen(
            strings = strings,
            initialAdmissionNo = userProfile?.admissionNo ?: "",
            studentsState = studentsState,
            paymentsState = feePaymentsState,
            onViewReceipt = { viewModel.showReceipt(it) }
          )

          PortalRole.ADMIN -> Unit
        }
      }
    }
  }

  latestReceipt?.let { receipt ->
    ReceiptPreviewDialog(
      payment = receipt,
      strings = strings,
      canRefundOrCorrect = activeRole == PortalRole.ADMIN,
      onRefundOrCorrect = { newStatus, reason ->
        viewModel.refundOrCorrectFeePayment(
          payment = receipt,
          newStatus = newStatus,
          reason = reason,
          successMessage = strings.refundRecordedMsg
        )
      },
      onShowMessage = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
      },
      onDismiss = { viewModel.dismissReceipt() }
    )
  }
}

@Composable
internal fun AdminPortalScaffold(
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  adminDisplayName: String,
  currentUserId: String,
  schoolSettings: SchoolSettings?,
  studentsState: UiState<List<StudentRecord>>,
  teachersState: UiState<List<TeacherRecord>>,
  feePaymentsState: UiState<List<FeePayment>>,
  feeStructuresState: UiState<List<FeeStructure>>,
  attendanceState: UiState<List<AttendanceRecord>>,
  homeworkState: UiState<List<HomeworkItem>>,
  examsState: UiState<List<ExamSchedule>>,
  resultsState: UiState<List<ExamResult>>,
  noticesState: UiState<List<SchoolNotice>>,
  timetableState: UiState<List<TimetableEntry>>,
  isSaving: Boolean,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  initialDestination: AdminDestination = AdminDestination.HOME,
  onSwitchRole: () -> Unit = {},
  onSignOut: () -> Unit = {},
  onReceiptClick: (FeePayment) -> Unit = {},
  onSaveStudent: (String?, String, String, String, String, String, String, String, Gender, String, String, Long, StudentStatus, String, String, String, String, String, ByteArray?, () -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, done -> done() },
  onToggleStudentStatus: (String, StudentStatus) -> Unit = { _, _ -> },
  onDeleteStudent: (String) -> Unit = {},
  onShowStudentMessage: (String) -> Unit = {},
  onCollectFee: (StudentRecord, String, Int, String, Long, Long, Long, Long, Long, Long, PaymentType, PaymentMode, String, String, () -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, done -> done() },
  onSaveFeeStructure: (String, String, Long, Long, Long, Long, Long, Long, Long, () -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, done -> done() },
  onSaveTeacher: (String?, String, String, String, String, String, String, StudentStatus, () -> Unit) -> Unit = { _, _, _, _, _, _, _, _, done -> done() },
  onDeleteTeacher: (String) -> Unit = {},
  onMarkAttendance: (StudentRecord, String, AttendanceStatus) -> Unit = { _, _, _ -> },
  onMarkAllPresent: (List<StudentRecord>, String) -> Unit = { _, _ -> },
  onEditAttendance: (AttendanceRecord, AttendanceStatus, String) -> Unit = { _, _, _ -> },
  onAddHomework: (String, String, String, String, String, () -> Unit) -> Unit = { _, _, _, _, _, done -> done() },
  onDeleteHomework: (String) -> Unit = {},
  onAddExam: (String, String, String, String, Long, String, () -> Unit) -> Unit = { _, _, _, _, _, _, done -> done() },
  onDeleteExam: (String) -> Unit = {},
  onSaveResult: (ExamSchedule, StudentRecord, Long, String, () -> Unit) -> Unit = { _, _, _, _, done -> done() },
  onDeleteResult: (String) -> Unit = {},
  onAddNotice: (String, String, NoticeAudience, NoticePriority, () -> Unit) -> Unit = { _, _, _, _, done -> done() },
  onDeleteNotice: (String) -> Unit = {},
  onAddTimetableEntry: (String, String, String, String, String, () -> Unit) -> Unit = { _, _, _, _, _, done -> done() },
  onDeleteTimetableEntry: (String) -> Unit = {},
  onSaveSchoolSettings: (String, String, String, String, String, Long) -> Unit = { _, _, _, _, _, _ -> }
) {
  var adminDestination by remember { mutableStateOf(initialDestination) }
  var openAddStudentOnNav by remember { mutableStateOf(false) }
  var openFeeSetupTabOnNav by remember { mutableIntStateOf(0) }
  var preselectedFeeStudent by remember { mutableStateOf<StudentRecord?>(null) }

  if (adminDestination != AdminDestination.HOME) {
    BackHandler { adminDestination = AdminDestination.HOME }
  }

  val activeSession = schoolSettings?.academicSession?.ifBlank { "2026-27" } ?: "2026-27"
  val noticeCount = (noticesState as? UiState.Success)?.data?.size ?: 0

  Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      SchoolTopBar(
        title = schoolSettings?.schoolName?.ifBlank { strings.schoolShortName } ?: strings.schoolShortName,
        subtitle = schoolSettings?.schoolAddress?.ifBlank { strings.schoolAddress } ?: strings.schoolAddress,
        language = language,
        onLanguageChange = onLanguageChange,
        onSwitchRole = onSwitchRole,
        onSignOut = onSignOut,
        strings = strings,
        showBackButton = adminDestination != AdminDestination.HOME,
        onBackClick = { adminDestination = AdminDestination.HOME },
        showAdminActions = true,
        adminDisplayName = adminDisplayName,
        sessionLabel = "${strings.session} $activeSession",
        noticeCount = noticeCount,
        onNotificationsClick = { adminDestination = AdminDestination.NOTICE_BOARD },
        onProfileClick = { adminDestination = AdminDestination.PROFILE }
      )
    },
    bottomBar = {
      BoxWithConstraints {
        if (maxWidth < 600.dp) {
          AdminBottomBar(
            currentDestination = adminDestination,
            strings = strings,
            onSelectTab = { dest ->
              if (dest != AdminDestination.FEES) openFeeSetupTabOnNav = 0
              adminDestination = dest
            }
          )
        }
      }
    }
  ) { innerPadding ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      val useRail = maxWidth >= 600.dp

      Row(modifier = Modifier.fillMaxSize()) {
        if (useRail) {
          AdminNavigationRail(
            currentDestination = adminDestination,
            strings = strings,
            onSelectTab = { dest ->
              if (dest != AdminDestination.FEES) openFeeSetupTabOnNav = 0
              adminDestination = dest
            }
          )
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          when (adminDestination) {
            AdminDestination.HOME -> AdminDashboardScreen(
              strings = strings,
              adminDisplayName = adminDisplayName,
              studentsState = studentsState,
              teachersState = teachersState,
              paymentsState = feePaymentsState,
              structuresState = feeStructuresState,
              attendanceState = attendanceState,
              noticesState = noticesState,
              schoolSettings = schoolSettings,
              onNavigate = { target, openAddStudent ->
                openAddStudentOnNav = openAddStudent
                if (target != AdminDestination.FEES) openFeeSetupTabOnNav = 0
                adminDestination = target
              },
              onReceiptClick = onReceiptClick
            )

            AdminDestination.STUDENTS -> {
              val structures = (feeStructuresState as? UiState.Success)?.data ?: emptyList()
              StudentManagementScreen(
                strings = strings,
                studentsState = studentsState,
                feeStructures = structures,
                activeSession = activeSession,
                isSaving = isSaving,
                initialOpenAddDialog = openAddStudentOnNav,
                onConsumedInitialAddDialog = { openAddStudentOnNav = false },
                onSaveStudent = onSaveStudent,
                onToggleStudentStatus = onToggleStudentStatus,
                onDeleteStudent = onDeleteStudent,
                onCollectFeeForStudent = { student ->
                  preselectedFeeStudent = student
                  openFeeSetupTabOnNav = 0
                  adminDestination = AdminDestination.FEES
                },
                onShowMessage = onShowStudentMessage
              )
            }

            AdminDestination.FEES -> FeesCollectionScreen(
              strings = strings,
              adminDisplayName = adminDisplayName,
              studentsState = studentsState,
              paymentsState = feePaymentsState,
              structuresState = feeStructuresState,
              isSaving = isSaving,
              activeSession = activeSession,
              initialTabIndex = openFeeSetupTabOnNav,
              preselectedStudent = preselectedFeeStudent,
              onConsumedPreselection = { preselectedFeeStudent = null },
              onCollectFee = onCollectFee,
              onSaveFeeStructure = onSaveFeeStructure,
              onViewReceipt = onReceiptClick,
              onShowMessage = onShowStudentMessage
            )

            AdminDestination.TEACHERS -> TeacherManagementScreen(
              strings = strings,
              teachersState = teachersState,
              isSaving = isSaving,
              onSaveTeacher = onSaveTeacher,
              onDeleteTeacher = onDeleteTeacher
            )

            AdminDestination.ATTENDANCE -> AttendanceScreen(
              strings = strings,
              studentsState = studentsState,
              attendanceState = attendanceState,
              isSaving = isSaving,
              isAdmin = true,
              activeSession = activeSession,
              onMarkAttendance = onMarkAttendance,
              onMarkAllPresent = onMarkAllPresent,
              onEditAttendance = onEditAttendance
            )

            AdminDestination.HOMEWORK -> HomeworkScreen(
              strings = strings,
              homeworkState = homeworkState,
              isSaving = isSaving,
              onAddHomework = onAddHomework,
              onDeleteHomework = onDeleteHomework
            )

            AdminDestination.EXAMINATION -> ExaminationScreen(
              strings = strings,
              examsState = examsState,
              isSaving = isSaving,
              onAddExam = onAddExam,
              onDeleteExam = onDeleteExam
            )

            AdminDestination.RESULTS -> ResultsScreen(
              strings = strings,
              examsState = examsState,
              studentsState = studentsState,
              resultsState = resultsState,
              isSaving = isSaving,
              onSaveResult = onSaveResult,
              onDeleteResult = onDeleteResult
            )

            AdminDestination.NOTICE_BOARD -> NoticeBoardScreen(
              strings = strings,
              noticesState = noticesState,
              isSaving = isSaving,
              onAddNotice = onAddNotice,
              onDeleteNotice = onDeleteNotice
            )

            AdminDestination.TIMETABLE -> TimetableScreen(
              strings = strings,
              timetableState = timetableState,
              isSaving = isSaving,
              onAddTimetableEntry = onAddTimetableEntry,
              onDeleteTimetableEntry = onDeleteTimetableEntry
            )

            AdminDestination.REPORTS -> ReportsScreen(
              strings = strings,
              studentsState = studentsState,
              teachersState = teachersState,
              paymentsState = feePaymentsState,
              attendanceState = attendanceState,
              resultsState = resultsState
            )

            AdminDestination.SETTINGS -> SchoolSettingsScreen(
              strings = strings,
              language = language,
              onLanguageChange = onLanguageChange,
              settings = schoolSettings,
              isSaving = isSaving,
              onSaveSettings = onSaveSchoolSettings
            )

            AdminDestination.PROFILE -> AdminProfileScreen(
              strings = strings,
              adminDisplayName = adminDisplayName,
              currentUserId = currentUserId,
              academicSession = activeSession,
              language = language,
              onLanguageChange = onLanguageChange,
              onOpenSettings = { adminDestination = AdminDestination.SETTINGS },
              onSwitchRole = onSwitchRole,
              onSignOut = onSignOut
            )
          }
        }
      }
    }
  }
}

private data class AdminNavTab(
  val destination: AdminDestination,
  val label: String,
  val filledIcon: ImageVector,
  val outlinedIcon: ImageVector,
  val tagSuffix: String
)

private fun adminNavTabs(strings: AppStrings): List<AdminNavTab> = listOf(
  AdminNavTab(AdminDestination.HOME, strings.navHome, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "home"),
  AdminNavTab(AdminDestination.STUDENTS, strings.navStudents, Icons.Filled.Groups, Icons.Outlined.Groups, "students"),
  AdminNavTab(AdminDestination.FEES, strings.navFees, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong, "fees"),
  AdminNavTab(AdminDestination.REPORTS, strings.navReports, Icons.Filled.Assessment, Icons.Outlined.Assessment, "reports"),
  AdminNavTab(AdminDestination.PROFILE, strings.navProfile, Icons.Filled.Person, Icons.Outlined.Person, "profile")
)

@Composable
internal fun AdminBottomBar(
  currentDestination: AdminDestination,
  strings: AppStrings,
  onSelectTab: (AdminDestination) -> Unit
) {
  NavigationBar(containerColor = PureWhite) {
    adminNavTabs(strings).forEach { tab ->
      val selected = currentDestination == tab.destination
      NavigationBarItem(
        selected = selected,
        onClick = { onSelectTab(tab.destination) },
        icon = {
          Icon(
            imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
            contentDescription = tab.label
          )
        },
        label = {
          Text(
            text = tab.label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = RoyalBlue900,
          selectedTextColor = RoyalBlue900,
          indicatorColor = SkyBlue100,
          unselectedIconColor = Slate500,
          unselectedTextColor = Slate500
        ),
        modifier = Modifier.testTag("bottom_nav_${tab.tagSuffix}")
      )
    }
  }
}

@Composable
internal fun AdminNavigationRail(
  currentDestination: AdminDestination,
  strings: AppStrings,
  onSelectTab: (AdminDestination) -> Unit
) {
  NavigationRail(containerColor = PureWhite) {
    adminNavTabs(strings).forEach { tab ->
      val selected = currentDestination == tab.destination
      NavigationRailItem(
        selected = selected,
        onClick = { onSelectTab(tab.destination) },
        icon = {
          Icon(
            imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
            contentDescription = tab.label
          )
        },
        label = {
          Text(
            text = tab.label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = RoyalBlue900,
          selectedTextColor = RoyalBlue900,
          indicatorColor = SkyBlue100,
          unselectedIconColor = Slate500,
          unselectedTextColor = Slate500
        ),
        modifier = Modifier.testTag("rail_nav_${tab.tagSuffix}")
      )
    }
  }
}
