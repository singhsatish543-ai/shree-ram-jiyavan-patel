package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.model.UserProfile
import com.example.data.repository.AcademicRepository
import com.example.data.repository.SchoolRepository
import com.example.i18n.AppLanguage
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SchoolViewModel(
  private val repository: SchoolRepository,
  private val academicRepository: AcademicRepository,
  val currentUserId: String,
  private val authDisplayName: String
) : ViewModel() {

  private fun <T> Flow<List<T>>.toUiStateFlow(label: String): StateFlow<UiState<List<T>>> =
    this.map<List<T>, UiState<List<T>>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing $label", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load $label"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
      )

  val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
    .catch { error ->
      val firestoreError = generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
      if (firestoreError == null && error is CancellationException) throw error
      Log.w(TAG, "Error observing user profile", firestoreError ?: error)
      emit(null)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

  val schoolSettings: StateFlow<SchoolSettings?> = academicRepository.observeSchoolSettings(currentUserId)
    .catch { error ->
      val firestoreError = generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
      if (firestoreError == null && error is CancellationException) throw error
      Log.w(TAG, "Error observing school settings", firestoreError ?: error)
      emit(null)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

  val studentsState: StateFlow<UiState<List<StudentRecord>>> =
    repository.observeStudents(currentUserId).toUiStateFlow("students")

  val feeStructuresState: StateFlow<UiState<List<FeeStructure>>> =
    repository.observeFeeStructures(currentUserId).toUiStateFlow("fee structures")

  val feePaymentsState: StateFlow<UiState<List<FeePayment>>> =
    repository.observeFeePayments(currentUserId).toUiStateFlow("fee payments")

  val teachersState: StateFlow<UiState<List<TeacherRecord>>> =
    academicRepository.observeTeachers(currentUserId).toUiStateFlow("teachers")

  val attendanceState: StateFlow<UiState<List<AttendanceRecord>>> =
    academicRepository.observeAttendance(currentUserId).toUiStateFlow("attendance")

  val homeworkState: StateFlow<UiState<List<HomeworkItem>>> =
    academicRepository.observeHomework(currentUserId).toUiStateFlow("homework")

  val examsState: StateFlow<UiState<List<ExamSchedule>>> =
    academicRepository.observeExams(currentUserId).toUiStateFlow("exams")

  val resultsState: StateFlow<UiState<List<ExamResult>>> =
    academicRepository.observeResults(currentUserId).toUiStateFlow("results")

  val noticesState: StateFlow<UiState<List<SchoolNotice>>> =
    academicRepository.observeNotices(currentUserId).toUiStateFlow("notices")

  val timetableState: StateFlow<UiState<List<TimetableEntry>>> =
    academicRepository.observeTimetable(currentUserId).toUiStateFlow("timetable")

  private val _isSaving = MutableStateFlow(false)
  val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

  private val _latestReceipt = MutableStateFlow<FeePayment?>(null)
  val latestReceipt: StateFlow<FeePayment?> = _latestReceipt.asStateFlow()

  private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val events = _events.asSharedFlow()

  private fun runMutation(successMessage: String, onSuccess: () -> Unit = {}, block: suspend () -> Result<*>) {
    viewModelScope.launch {
      _isSaving.value = true
      val res = block()
      _isSaving.value = false
      res.onSuccess {
        _events.tryEmit(successMessage)
        onSuccess()
      }.onFailure { err ->
        _events.tryEmit(err.localizedMessage ?: "Operation failed")
      }
    }
  }

  fun syncUserPortalProfile(
    displayName: String,
    portalRole: PortalRole,
    language: AppLanguage,
    staffCode: String = "",
    admissionNo: String = "",
    assignedClass: String = ""
  ) {
    viewModelScope.launch {
      val resolvedName = displayName.ifBlank { authDisplayName }.ifBlank { "School User" }
      repository.upsertUserProfile(
        userId = currentUserId,
        displayName = resolvedName,
        portalRole = portalRole,
        language = language,
        staffCode = staffCode,
        admissionNo = admissionNo,
        assignedClass = assignedClass
      ).onFailure { err ->
        _events.tryEmit(err.localizedMessage ?: "Failed to save portal profile")
      }
    }
  }

  // Students & Fees
  fun saveStudent(
    existingStudentId: String?, admissionNo: String, rollNumber: String, fullName: String,
    fatherName: String, motherName: String, className: String, section: String,
    gender: Gender, phone: String, address: String, monthlyFeeAmount: Long,
    status: StudentStatus, photoUrl: String = "", dateOfBirth: String = "",
    admissionDate: String = "", academicSession: String = "2026-27",
    previousSchool: String = "", newPhotoJpegBytes: ByteArray? = null,
    successMessage: String, onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      val res = repository.saveStudent(
        userId = currentUserId,
        existingStudentId = existingStudentId,
        admissionNo = admissionNo,
        rollNumber = rollNumber,
        fullName = fullName,
        fatherName = fatherName,
        motherName = motherName,
        className = className,
        section = section,
        gender = gender,
        phone = phone,
        address = address,
        monthlyFeeAmount = monthlyFeeAmount,
        status = status,
        photoUrl = photoUrl,
        dateOfBirth = dateOfBirth,
        admissionDate = admissionDate,
        academicSession = academicSession,
        previousSchool = previousSchool,
        newPhotoJpegBytes = newPhotoJpegBytes
      )
      _isSaving.value = false
      res.onSuccess { outcome ->
        val message = if (outcome.photoWarning != null) {
          "$successMessage (Photo upload skipped: ${outcome.photoWarning})"
        } else {
          successMessage
        }
        _events.tryEmit(message)
        onSuccess()
      }.onFailure { err ->
        _events.tryEmit(err.localizedMessage ?: "Failed to save student record")
      }
    }
  }

  fun toggleStudentStatus(studentId: String, newStatus: StudentStatus, successMessage: String) =
    runMutation(successMessage) {
      repository.toggleStudentStatus(currentUserId, studentId, newStatus)
    }

  fun deleteStudent(studentId: String, successMessage: String) =
    runMutation(successMessage) { repository.deleteStudent(currentUserId, studentId) }

  fun saveFeeStructure(
    academicSession: String,
    className: String,
    monthlyTuitionFee: Long,
    admissionFee: Long,
    examFee: Long,
    developmentFee: Long,
    otherCharges: Long,
    lateFee: Long,
    discount: Long,
    computerFee: Long,
    transportFee: Long,
    successMessage: String,
    onSuccess: () -> Unit
  ) = runMutation(successMessage, onSuccess) {
    repository.saveFeeStructure(
      userId = currentUserId,
      academicSession = academicSession,
      className = className,
      monthlyTuitionFee = monthlyTuitionFee,
      admissionFee = admissionFee,
      examFee = examFee,
      developmentFee = developmentFee,
      otherCharges = otherCharges,
      lateFee = lateFee,
      discount = discount,
      computerFee = computerFee,
      transportFee = transportFee
    )
  }

  fun collectFeePayment(
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
    idempotencyKey: String = UUID.randomUUID().toString(),
    successMessage: String,
    onSuccess: (FeePayment) -> Unit
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      val now = Date()
      val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(now)
      val isoStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(now)
      val result = repository.collectFeePayment(
        userId = currentUserId,
        student = student,
        academicYear = academicYear,
        monthIndex = monthIndex,
        monthName = monthName,
        tuitionFee = tuitionFee,
        otherFee = otherFee,
        lateFee = lateFee,
        discount = discount,
        previousBalance = previousBalance,
        amountPaid = amountPaid,
        paymentType = paymentType,
        paymentMode = paymentMode,
        paidOnDate = dateStr,
        paidOnIsoDate = isoStr,
        remarks = remarks,
        collectedBy = collectedBy.ifBlank { authDisplayName }.ifBlank { "Principal" },
        idempotencyKey = idempotencyKey
      )
      _isSaving.value = false
      result.onSuccess { payment ->
        _latestReceipt.value = payment
        _events.tryEmit(successMessage)
        onSuccess(payment)
      }.onFailure { err ->
        _events.tryEmit(err.localizedMessage ?: "Failed to collect fee payment")
      }
    }
  }

  fun refundOrCorrectFeePayment(
    payment: FeePayment,
    newStatus: PaymentStatus,
    reason: String,
    successMessage: String,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      val result = repository.refundOrCorrectFeePayment(currentUserId, payment, newStatus, reason)
      _isSaving.value = false
      result.onSuccess { updated ->
        if (_latestReceipt.value?.id == updated.id) {
          _latestReceipt.value = updated
        }
        _events.tryEmit(successMessage)
        onSuccess()
      }.onFailure { err ->
        _events.tryEmit(err.localizedMessage ?: "Failed to update payment status")
      }
    }
  }

  // Teachers
  fun saveTeacher(
    existingId: String?, staffCode: String, fullName: String, subject: String,
    assignedClass: String, phone: String, qualification: String, status: StudentStatus,
    successMessage: String, onSuccess: () -> Unit
  ) = runMutation(successMessage, onSuccess) {
    academicRepository.saveTeacher(currentUserId, existingId, staffCode, fullName, subject, assignedClass, phone, qualification, status)
  }

  fun deleteTeacher(teacherId: String, successMessage: String) =
    runMutation(successMessage) { academicRepository.deleteTeacher(currentUserId, teacherId) }

  // Attendance
  fun markAttendance(
    student: StudentRecord,
    dateKey: String,
    status: AttendanceStatus,
    markedBy: String,
    academicSession: String = "2026-27",
    successMessage: String
  ) = runMutation(successMessage) {
    academicRepository.markAttendance(
      userId = currentUserId,
      student = student,
      dateKey = dateKey,
      status = status,
      markedBy = markedBy,
      academicSession = academicSession,
      allowAdminOverwrite = false
    )
  }

  fun markAllUnmarkedPresent(
    students: List<StudentRecord>,
    dateKey: String,
    markedBy: String,
    academicSession: String = "2026-27",
    successMessage: String
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      val res = academicRepository.markClassAttendance(
        userId = currentUserId,
        students = students,
        dateKey = dateKey,
        status = AttendanceStatus.PRESENT,
        markedBy = markedBy,
        academicSession = academicSession
      )
      _isSaving.value = false
      res.onSuccess { outcome ->
        val summary = if (outcome.skippedDuplicateCount > 0) {
          "$successMessage (${outcome.markedCount} marked, ${outcome.skippedDuplicateCount} already marked skipped)"
        } else {
          "$successMessage (${outcome.markedCount})"
        }
        _events.tryEmit(summary)
      }.onFailure { err ->
        _events.tryEmit(err.localizedMessage ?: "Failed to save attendance")
      }
    }
  }

  fun editAttendanceRecord(
    record: AttendanceRecord,
    newStatus: AttendanceStatus,
    editedBy: String,
    editReason: String,
    successMessage: String,
    onSuccess: () -> Unit = {}
  ) = runMutation(successMessage, onSuccess) {
    academicRepository.updateAttendanceRecord(
      userId = currentUserId,
      record = record,
      newStatus = newStatus,
      editedBy = editedBy,
      editReason = editReason
    )
  }

  // Homework
  fun addHomework(
    className: String, subject: String, title: String, description: String,
    dueDate: String, assignedBy: String, successMessage: String, onSuccess: () -> Unit
  ) = runMutation(successMessage, onSuccess) {
    academicRepository.addHomework(currentUserId, className, subject, title, description, dueDate, assignedBy)
  }

  fun deleteHomework(id: String, successMessage: String) =
    runMutation(successMessage) { academicRepository.deleteHomework(currentUserId, id) }

  // Exams & Results
  fun addExam(
    examName: String, className: String, subject: String, examDate: String,
    maxMarks: Long, roomNo: String, successMessage: String, onSuccess: () -> Unit
  ) = runMutation(successMessage, onSuccess) {
    academicRepository.addExam(currentUserId, examName, className, subject, examDate, maxMarks, roomNo)
  }

  fun deleteExam(id: String, successMessage: String) =
    runMutation(successMessage) { academicRepository.deleteExam(currentUserId, id) }

  fun saveResult(
    exam: ExamSchedule, student: StudentRecord, marksObtained: Long,
    remarks: String, successMessage: String, onSuccess: () -> Unit
  ) = runMutation(successMessage, onSuccess) {
    academicRepository.saveResult(currentUserId, exam, student, marksObtained, remarks)
  }

  fun deleteResult(id: String, successMessage: String) =
    runMutation(successMessage) { academicRepository.deleteResult(currentUserId, id) }

  // Notices
  fun addNotice(
    title: String, body: String, audience: NoticeAudience, priority: NoticePriority,
    successMessage: String, onSuccess: () -> Unit
  ) = runMutation(successMessage, onSuccess) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
    academicRepository.addNotice(currentUserId, title, body, audience, priority, dateStr)
  }

  fun deleteNotice(id: String, successMessage: String) =
    runMutation(successMessage) { academicRepository.deleteNotice(currentUserId, id) }

  // Timetable
  fun addTimetableEntry(
    className: String, dayOfWeek: String, periodLabel: String,
    subject: String, teacherName: String, successMessage: String, onSuccess: () -> Unit
  ) = runMutation(successMessage, onSuccess) {
    academicRepository.addTimetableEntry(currentUserId, className, dayOfWeek, periodLabel, subject, teacherName)
  }

  fun deleteTimetableEntry(id: String, successMessage: String) =
    runMutation(successMessage) { academicRepository.deleteTimetableEntry(currentUserId, id) }

  // School Settings
  fun saveSchoolSettings(
    schoolName: String, schoolAddress: String, academicSession: String,
    principalName: String, contactPhone: String, totalConfiguredClasses: Long,
    successMessage: String, onSuccess: () -> Unit = {}
  ) = runMutation(successMessage, onSuccess) {
    academicRepository.saveSchoolSettings(
      currentUserId, schoolName, schoolAddress, academicSession,
      principalName, contactPhone, totalConfiguredClasses
    )
  }

  fun showReceipt(payment: FeePayment) { _latestReceipt.value = payment }
  fun dismissReceipt() { _latestReceipt.value = null }

  private companion object {
    const val TAG = "SchoolViewModel"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
