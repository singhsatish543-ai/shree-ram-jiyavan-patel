package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ExamResult
import com.example.data.model.ExamSchedule
import com.example.data.model.HomeworkItem
import com.example.data.model.NoticeAudience
import com.example.data.model.NoticePriority
import com.example.data.model.SchoolNotice
import com.example.data.model.SchoolSettings
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.TeacherRecord
import com.example.data.model.TimetableEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AcademicRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(expectedUserId: String): String {
    val uid = auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
    return uid
  }

  private fun <T> Flow<T>.wrapFlow(
    op: OperationType,
    path: String,
    userId: String
  ): Flow<T> = catch { error ->
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val toLog = firestoreError ?: (error as? Exception)
    if (toLog != null && auth.currentUser?.uid == userId) {
      handleFirestoreError(toLog, op, path)
    }
    throw error
  }

  // --- TEACHERS ---

  fun observeTeachers(userId: String): Flow<List<TeacherRecord>> = flow {
    val uid = requireUserId(userId)
    emitAll(
      db.collection("users").document(uid).collection("teachers")
        .snapshots()
        .map { snap ->
          snap.toObjects(TeacherRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.fullName }
        }
        .wrapFlow(OperationType.LIST, "users/$uid/teachers", uid)
    )
  }

  suspend fun saveTeacher(
    userId: String,
    existingTeacherId: String?,
    staffCode: String,
    fullName: String,
    subject: String,
    assignedClass: String,
    phone: String,
    qualification: String,
    status: StudentStatus
  ): Result<Unit> {
    val path = "users/$userId/teachers/${existingTeacherId ?: "new"}"
    return try {
      val uid = requireUserId(userId)
      val cleanStaff = staffCode.trim().take(30)
      val cleanName = fullName.trim().take(80)
      val cleanSubject = subject.trim().take(60)
      val cleanClass = assignedClass.trim().take(30)
      val cleanPhone = phone.trim().take(20)
      val cleanQual = qualification.trim().take(60)
      require(cleanStaff.isNotEmpty() && cleanName.length >= 2 && cleanSubject.isNotEmpty()) {
        "Staff Code, Full Name, and Subject are required"
      }
      val col = db.collection("users").document(uid).collection("teachers")
      if (existingTeacherId.isNullOrBlank()) {
        val payload = mutableMapOf<String, Any>(
          "userId" to uid,
          "staffCode" to cleanStaff,
          "fullName" to cleanName,
          "subject" to cleanSubject,
          "assignedClass" to cleanClass,
          "status" to status.name,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (cleanPhone.isNotEmpty()) payload["phone"] = cleanPhone
        if (cleanQual.isNotEmpty()) payload["qualification"] = cleanQual
        col.document().set(payload).await()
      } else {
        col.document(existingTeacherId).update(
          mapOf(
            "staffCode" to cleanStaff,
            "fullName" to cleanName,
            "subject" to cleanSubject,
            "assignedClass" to cleanClass,
            "phone" to cleanPhone,
            "qualification" to cleanQual,
            "status" to status.name,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteTeacher(userId: String, teacherId: String): Result<Unit> =
    deleteDoc(userId, "teachers", teacherId)

  // --- ATTENDANCE ---

  data class BatchAttendanceOutcome(
    val markedCount: Int,
    val skippedDuplicateCount: Int
  )

  fun observeAttendance(userId: String): Flow<List<AttendanceRecord>> = flow {
    val uid = requireUserId(userId)
    emitAll(
      db.collection("users").document(uid).collection("attendanceRecords")
        .snapshots()
        .map { snap ->
          snap.toObjects(AttendanceRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.updatedAt?.seconds ?: 0L }
        }
        .wrapFlow(OperationType.LIST, "users/$uid/attendanceRecords", uid)
    )
  }

  suspend fun markAttendance(
    userId: String,
    student: StudentRecord,
    dateKey: String,
    status: AttendanceStatus,
    markedBy: String,
    academicSession: String = "2026-27",
    allowAdminOverwrite: Boolean = false
  ): Result<Unit> {
    val cleanDate = dateKey.trim().take(20)
    val safeDateId = cleanDate.replace(Regex("[^a-zA-Z0-9]+"), "_")
    val docId = "${safeDateId}_${student.id}"
    val path = "users/$userId/attendanceRecords/$docId"
    return try {
      val uid = requireUserId(userId)
      require(student.id.isNotBlank() && cleanDate.length >= 8) { "Valid student and date required" }
      val col = db.collection("users").document(uid).collection("attendanceRecords")
      val docRef = col.document(docId)
      val cleanMarker = markedBy.trim().ifBlank { "Principal" }.take(80)
      val cleanSection = student.section.trim().ifBlank { "A" }.take(10)
      val cleanSession = academicSession.trim().ifBlank { "2026-27" }.take(20)

      // Guard against duplicate documents even if a legacy doc exists under another ID
      val existingMatches = col
        .whereEqualTo("studentId", student.id)
        .whereEqualTo("dateKey", cleanDate)
        .limit(1)
        .get()
        .await()
      val existingLegacyDoc = existingMatches.documents.firstOrNull { it.id != docId }
      if (existingLegacyDoc != null && !allowAdminOverwrite) {
        throw IllegalStateException("Duplicate attendance prevented: already marked for ${student.fullName} on $cleanDate")
      }

      db.runTransaction { tx ->
        val snap = tx.get(docRef)
        if (snap.exists()) {
          if (!allowAdminOverwrite) {
            throw IllegalStateException("Duplicate attendance prevented: already marked for ${student.fullName} on $cleanDate")
          }
          tx.update(
            docRef,
            mapOf(
              "status" to status.name,
              "markedBy" to cleanMarker,
              "editedBy" to cleanMarker,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
        } else {
          tx.set(
            docRef,
            mapOf(
              "userId" to uid,
              "studentId" to student.id,
              "studentName" to student.fullName.take(80),
              "admissionNo" to student.admissionNo.take(30),
              "className" to student.className.take(30),
              "section" to cleanSection,
              "academicSession" to cleanSession,
              "dateKey" to cleanDate,
              "status" to status.name,
              "markedBy" to cleanMarker,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
        }
        Unit
      }.await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun markClassAttendance(
    userId: String,
    students: List<StudentRecord>,
    dateKey: String,
    status: AttendanceStatus,
    markedBy: String,
    academicSession: String = "2026-27"
  ): Result<BatchAttendanceOutcome> {
    val cleanDate = dateKey.trim().take(20)
    val safeDateId = cleanDate.replace(Regex("[^a-zA-Z0-9]+"), "_")
    val path = "users/$userId/attendanceRecords"
    return try {
      val uid = requireUserId(userId)
      require(cleanDate.length >= 8 && students.isNotEmpty()) { "Students and valid date required" }
      val col = db.collection("users").document(uid).collection("attendanceRecords")
      val cleanMarker = markedBy.trim().ifBlank { "Principal" }.take(80)
      val cleanSession = academicSession.trim().ifBlank { "2026-27" }.take(20)

      val daySnapshot = col.whereEqualTo("dateKey", cleanDate).get().await()
      val alreadyMarkedStudentIds = daySnapshot.documents
        .mapNotNull { it.getString("studentId") }
        .toSet()

      val distinctValidStudents = students
        .filter { it.id.isNotBlank() }
        .distinctBy { it.id }
      val unmarkedStudents = distinctValidStudents.filter { it.id !in alreadyMarkedStudentIds }
      val preSkipped = distinctValidStudents.size - unmarkedStudents.size

      if (unmarkedStudents.isEmpty()) {
        throw IllegalStateException("Attendance already marked for all selected students on $cleanDate")
      }

      var totalCreated = 0
      var totalSkipped = preSkipped

      unmarkedStudents.chunked(350).forEach { chunk ->
        val chunkCreated = db.runTransaction { tx ->
          var created = 0
          val refs = chunk.map { student ->
            student to col.document("${safeDateId}_${student.id}")
          }
          val snaps = refs.map { (student, ref) ->
            Triple(student, ref, tx.get(ref))
          }
          snaps.forEach { (student, ref, snap) ->
            if (!snap.exists()) {
              val cleanSection = student.section.trim().ifBlank { "A" }.take(10)
              tx.set(
                ref,
                mapOf(
                  "userId" to uid,
                  "studentId" to student.id,
                  "studentName" to student.fullName.take(80),
                  "admissionNo" to student.admissionNo.take(30),
                  "className" to student.className.take(30),
                  "section" to cleanSection,
                  "academicSession" to cleanSession,
                  "dateKey" to cleanDate,
                  "status" to status.name,
                  "markedBy" to cleanMarker,
                  "createdAt" to FieldValue.serverTimestamp(),
                  "updatedAt" to FieldValue.serverTimestamp()
                )
              )
              created++
            }
          }
          created
        }.await()
        totalCreated += chunkCreated
        totalSkipped += (chunk.size - chunkCreated)
      }

      Result.success(BatchAttendanceOutcome(markedCount = totalCreated, skippedDuplicateCount = totalSkipped))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun updateAttendanceRecord(
    userId: String,
    record: AttendanceRecord,
    newStatus: AttendanceStatus,
    editedBy: String,
    editReason: String
  ): Result<Unit> {
    val cleanDate = record.dateKey.trim().take(20)
    val docId = record.id.ifBlank {
      val safeDateId = cleanDate.replace(Regex("[^a-zA-Z0-9]+"), "_")
      "${safeDateId}_${record.studentId}"
    }
    val path = "users/$userId/attendanceRecords/$docId"
    return try {
      val uid = requireUserId(userId)
      require(docId.isNotBlank() && record.studentId.isNotBlank()) { "Valid attendance record required" }
      val cleanEditor = editedBy.trim().ifBlank { "Principal" }.take(80)
      val cleanReason = editReason.trim().take(200)
      val docRef = db.collection("users").document(uid).collection("attendanceRecords").document(docId)
      val updates = mutableMapOf<String, Any>(
        "status" to newStatus.name,
        "editedBy" to cleanEditor,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      if (cleanReason.isNotEmpty()) {
        updates["editReason"] = cleanReason
      }
      docRef.update(updates).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  // --- HOMEWORK ---

  fun observeHomework(userId: String): Flow<List<HomeworkItem>> = flow {
    val uid = requireUserId(userId)
    emitAll(
      db.collection("users").document(uid).collection("homeworks")
        .snapshots()
        .map { snap ->
          snap.toObjects(HomeworkItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .wrapFlow(OperationType.LIST, "users/$uid/homeworks", uid)
    )
  }

  suspend fun addHomework(
    userId: String,
    className: String,
    subject: String,
    title: String,
    description: String,
    dueDate: String,
    assignedBy: String
  ): Result<Unit> {
    val path = "users/$userId/homeworks"
    return try {
      val uid = requireUserId(userId)
      val cleanTitle = title.trim().take(120)
      val cleanDesc = description.trim().take(600)
      val cleanSubject = subject.trim().take(60)
      val cleanDue = dueDate.trim().take(30)
      require(cleanTitle.length >= 2 && cleanDesc.isNotEmpty() && cleanSubject.isNotEmpty() && cleanDue.length >= 4) {
        "All homework fields are required"
      }
      db.collection("users").document(uid).collection("homeworks").document().set(
        mapOf(
          "userId" to uid,
          "className" to className.trim().take(30),
          "subject" to cleanSubject,
          "title" to cleanTitle,
          "description" to cleanDesc,
          "dueDate" to cleanDue,
          "assignedBy" to assignedBy.trim().ifBlank { "Principal" }.take(80),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteHomework(userId: String, homeworkId: String): Result<Unit> =
    deleteDoc(userId, "homeworks", homeworkId)

  // --- EXAMS & RESULTS ---

  fun observeExams(userId: String): Flow<List<ExamSchedule>> = flow {
    val uid = requireUserId(userId)
    emitAll(
      db.collection("users").document(uid).collection("exams")
        .snapshots()
        .map { snap ->
          snap.toObjects(ExamSchedule::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .wrapFlow(OperationType.LIST, "users/$uid/exams", uid)
    )
  }

  suspend fun addExam(
    userId: String,
    examName: String,
    className: String,
    subject: String,
    examDate: String,
    maxMarks: Long,
    roomNo: String
  ): Result<Unit> {
    val path = "users/$userId/exams"
    return try {
      val uid = requireUserId(userId)
      val cleanName = examName.trim().take(80)
      val cleanSubject = subject.trim().take(60)
      val cleanDate = examDate.trim().take(30)
      val safeMarks = maxMarks.coerceIn(1L, 1000L)
      require(cleanName.length >= 2 && cleanSubject.isNotEmpty() && cleanDate.length >= 4) {
        "Exam name, subject, and date are required"
      }
      val payload = mutableMapOf<String, Any>(
        "userId" to uid,
        "examName" to cleanName,
        "className" to className.trim().take(30),
        "subject" to cleanSubject,
        "examDate" to cleanDate,
        "maxMarks" to safeMarks,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      val cleanRoom = roomNo.trim().take(30)
      if (cleanRoom.isNotEmpty()) payload["roomNo"] = cleanRoom
      db.collection("users").document(uid).collection("exams").document().set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteExam(userId: String, examId: String): Result<Unit> =
    deleteDoc(userId, "exams", examId)

  fun observeResults(userId: String): Flow<List<ExamResult>> = flow {
    val uid = requireUserId(userId)
    emitAll(
      db.collection("users").document(uid).collection("results")
        .snapshots()
        .map { snap ->
          snap.toObjects(ExamResult::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .wrapFlow(OperationType.LIST, "users/$uid/results", uid)
    )
  }

  suspend fun saveResult(
    userId: String,
    exam: ExamSchedule,
    student: StudentRecord,
    marksObtained: Long,
    remarks: String
  ): Result<Unit> {
    val docId = "${exam.id}_${student.id}"
    val path = "users/$userId/results/$docId"
    return try {
      val uid = requireUserId(userId)
      val safeMax = exam.maxMarks.coerceIn(1L, 1000L)
      val safeMarks = marksObtained.coerceIn(0L, safeMax)
      val pct = (safeMarks * 100.0) / safeMax
      val grade = when {
        pct >= 90 -> "A+"
        pct >= 80 -> "A"
        pct >= 70 -> "B+"
        pct >= 60 -> "B"
        pct >= 45 -> "C"
        pct >= 33 -> "D"
        else -> "F"
      }
      val docRef = db.collection("users").document(uid).collection("results").document(docId)
      val exists = docRef.get().await().exists()
      val cleanRemarks = remarks.trim().take(120)
      if (exists) {
        docRef.update(
          mapOf(
            "marksObtained" to safeMarks,
            "maxMarks" to safeMax,
            "grade" to grade,
            "remarks" to cleanRemarks,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      } else {
        val payload = mutableMapOf<String, Any>(
          "userId" to uid,
          "examId" to exam.id,
          "examName" to exam.examName.take(80),
          "studentId" to student.id,
          "studentName" to student.fullName.take(80),
          "admissionNo" to student.admissionNo.take(30),
          "className" to student.className.take(30),
          "subject" to exam.subject.take(60),
          "marksObtained" to safeMarks,
          "maxMarks" to safeMax,
          "grade" to grade,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (cleanRemarks.isNotEmpty()) payload["remarks"] = cleanRemarks
        docRef.set(payload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteResult(userId: String, resultId: String): Result<Unit> =
    deleteDoc(userId, "results", resultId)

  // --- NOTICES ---

  fun observeNotices(userId: String): Flow<List<SchoolNotice>> = flow {
    val uid = requireUserId(userId)
    emitAll(
      db.collection("users").document(uid).collection("notices")
        .snapshots()
        .map { snap ->
          snap.toObjects(SchoolNotice::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .wrapFlow(OperationType.LIST, "users/$uid/notices", uid)
    )
  }

  suspend fun addNotice(
    userId: String,
    title: String,
    body: String,
    audience: NoticeAudience,
    priority: NoticePriority,
    publishedDate: String
  ): Result<Unit> {
    val path = "users/$userId/notices"
    return try {
      val uid = requireUserId(userId)
      val cleanTitle = title.trim().take(120)
      val cleanBody = body.trim().take(1000)
      require(cleanTitle.length >= 2 && cleanBody.length >= 2) { "Notice title and details are required" }
      db.collection("users").document(uid).collection("notices").document().set(
        mapOf(
          "userId" to uid,
          "title" to cleanTitle,
          "body" to cleanBody,
          "audience" to audience.name,
          "priority" to priority.name,
          "publishedDate" to publishedDate.trim().take(30),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteNotice(userId: String, noticeId: String): Result<Unit> =
    deleteDoc(userId, "notices", noticeId)

  // --- TIMETABLE ---

  fun observeTimetable(userId: String): Flow<List<TimetableEntry>> = flow {
    val uid = requireUserId(userId)
    emitAll(
      db.collection("users").document(uid).collection("timetable")
        .snapshots()
        .map { snap ->
          snap.toObjects(TimetableEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedWith(compareBy({ it.className }, { it.dayOfWeek }, { it.periodLabel }))
        }
        .wrapFlow(OperationType.LIST, "users/$uid/timetable", uid)
    )
  }

  suspend fun addTimetableEntry(
    userId: String,
    className: String,
    dayOfWeek: String,
    periodLabel: String,
    subject: String,
    teacherName: String
  ): Result<Unit> {
    val path = "users/$userId/timetable"
    return try {
      val uid = requireUserId(userId)
      val cleanPeriod = periodLabel.trim().take(40)
      val cleanSubject = subject.trim().take(60)
      val cleanTeacher = teacherName.trim().take(80)
      require(cleanPeriod.isNotEmpty() && cleanSubject.isNotEmpty() && cleanTeacher.isNotEmpty()) {
        "Period, subject, and teacher are required"
      }
      db.collection("users").document(uid).collection("timetable").document().set(
        mapOf(
          "userId" to uid,
          "className" to className.trim().take(30),
          "dayOfWeek" to dayOfWeek.trim().take(20),
          "periodLabel" to cleanPeriod,
          "subject" to cleanSubject,
          "teacherName" to cleanTeacher,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteTimetableEntry(userId: String, entryId: String): Result<Unit> =
    deleteDoc(userId, "timetable", entryId)

  // --- SCHOOL SETTINGS ---

  fun observeSchoolSettings(userId: String): Flow<SchoolSettings?> = flow {
    val uid = requireUserId(userId)
    emitAll(
      db.collection("users").document(uid).collection("schoolConfig").document("main")
        .snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(SchoolSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          } else null
        }
        .wrapFlow(OperationType.GET, "users/$uid/schoolConfig/main", uid)
    )
  }

  suspend fun saveSchoolSettings(
    userId: String,
    schoolName: String,
    schoolAddress: String,
    academicSession: String,
    principalName: String,
    contactPhone: String,
    totalConfiguredClasses: Long
  ): Result<Unit> {
    val path = "users/$userId/schoolConfig/main"
    return try {
      val uid = requireUserId(userId)
      val cleanName = schoolName.trim().take(140)
      val cleanAddr = schoolAddress.trim().take(200)
      val cleanSession = academicSession.trim().take(20)
      val cleanPrincipal = principalName.trim().take(80)
      val cleanPhone = contactPhone.trim().take(20)
      val safeClasses = totalConfiguredClasses.coerceIn(1L, 30L)
      require(cleanName.length >= 2 && cleanAddr.length >= 2 && cleanSession.length >= 4 && cleanPrincipal.isNotEmpty()) {
        "School name, address, session, and principal name are required"
      }
      val docRef = db.collection("users").document(uid).collection("schoolConfig").document("main")
      val exists = docRef.get().await().exists()
      if (exists) {
        docRef.update(
          mapOf(
            "schoolName" to cleanName,
            "schoolAddress" to cleanAddr,
            "academicSession" to cleanSession,
            "principalName" to cleanPrincipal,
            "contactPhone" to cleanPhone,
            "totalConfiguredClasses" to safeClasses,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      } else {
        val payload = mutableMapOf<String, Any>(
          "userId" to uid,
          "schoolName" to cleanName,
          "schoolAddress" to cleanAddr,
          "academicSession" to cleanSession,
          "principalName" to cleanPrincipal,
          "totalConfiguredClasses" to safeClasses,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (cleanPhone.isNotEmpty()) payload["contactPhone"] = cleanPhone
        docRef.set(payload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  private suspend fun deleteDoc(userId: String, collection: String, docId: String): Result<Unit> {
    val path = "users/$userId/$collection/$docId"
    return try {
      val uid = requireUserId(userId)
      require(docId.isNotBlank())
      db.collection("users").document(uid).collection(collection).document(docId).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }
}
