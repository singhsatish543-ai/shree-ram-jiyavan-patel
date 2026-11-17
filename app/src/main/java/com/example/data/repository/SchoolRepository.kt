package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.FeePayment
import com.example.data.model.FeeStructure
import com.example.data.model.Gender
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.PortalRole
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.UserProfile
import com.example.i18n.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class StudentSaveOutcome(
  val studentId: String,
  val photoWarning: String? = null
)

class SchoolRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
  private val storageProvider: () -> FirebaseStorage = { Firebase.storage }
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

  private fun <T> Flow<T>.wrapFirestoreFlow(
    operationType: OperationType,
    path: String,
    userId: String
  ): Flow<T> = catch { error ->
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val exceptionToLog = firestoreError ?: (error as? Exception)
    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
      handleFirestoreError(exceptionToLog, operationType, path)
    }
    throw error
  }

  // --- USER PROFILE ---

  fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
    val uid = requireUserId(userId)
    val path = "users/$uid"
    emitAll(
      db.collection("users").document(uid)
        .snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(
              UserProfile::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
          } else {
            null
          }
        }
        .wrapFirestoreFlow(OperationType.GET, path, uid)
    )
  }

  suspend fun upsertUserProfile(
    userId: String,
    displayName: String,
    portalRole: PortalRole,
    language: AppLanguage,
    staffCode: String = "",
    admissionNo: String = "",
    assignedClass: String = ""
  ): Result<Unit> {
    val docPath = "users/$userId"
    return try {
      val uid = requireUserId(userId)
      val cleanName = displayName.trim().ifBlank { "School User" }.take(100)
      val cleanStaff = staffCode.trim().take(40)
      val cleanAdm = admissionNo.trim().take(40)
      val cleanClass = assignedClass.trim().take(30)

      val docRef = db.collection("users").document(uid)
      val existingSnap = docRef.get().await()

      if (existingSnap.exists()) {
        val updatePayload = mutableMapOf<String, Any>(
          "displayName" to cleanName,
          "portalMode" to portalRole.name,
          "preferredLanguage" to language.code,
          "staffCode" to cleanStaff,
          "admissionNo" to cleanAdm,
          "assignedClass" to cleanClass,
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(updatePayload).await()
      } else {
        val createPayload = mutableMapOf<String, Any>(
          "userId" to uid,
          "displayName" to cleanName,
          "portalMode" to portalRole.name,
          "preferredLanguage" to language.code,
          "updatedAt" to FieldValue.serverTimestamp(),
          "createdAt" to FieldValue.serverTimestamp()
        )
        if (cleanStaff.isNotEmpty()) createPayload["staffCode"] = cleanStaff
        if (cleanAdm.isNotEmpty()) createPayload["admissionNo"] = cleanAdm
        if (cleanClass.isNotEmpty()) createPayload["assignedClass"] = cleanClass
        docRef.set(createPayload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  // --- STUDENTS ---

  fun observeStudents(userId: String): Flow<List<StudentRecord>> = flow {
    val uid = requireUserId(userId)
    val path = "users/$uid/students"
    emitAll(
      db.collection("users").document(uid).collection("students")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            StudentRecord::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
          ).sortedWith(compareBy({ it.className }, { it.rollNumber.toIntOrNull() ?: Int.MAX_VALUE }, { it.fullName }))
        }
        .wrapFirestoreFlow(OperationType.LIST, path, uid)
    )
  }

  suspend fun saveStudent(
    userId: String,
    existingStudentId: String?,
    admissionNo: String,
    rollNumber: String,
    fullName: String,
    fatherName: String,
    motherName: String,
    className: String,
    section: String,
    gender: Gender,
    phone: String,
    address: String,
    monthlyFeeAmount: Long,
    status: StudentStatus,
    photoUrl: String = "",
    dateOfBirth: String = "",
    admissionDate: String = "",
    academicSession: String = "2026-27",
    previousSchool: String = "",
    newPhotoJpegBytes: ByteArray? = null
  ): Result<StudentSaveOutcome> {
    val basePath = "users/$userId/students"
    return try {
      val uid = requireUserId(userId)
      val cleanAdmission = admissionNo.trim().take(30)
      val cleanRoll = rollNumber.trim().take(20)
      val cleanName = fullName.trim().take(80)
      val cleanFather = fatherName.trim().take(80)
      val cleanMother = motherName.trim().take(80)
      val cleanClass = className.trim().take(30)
      val cleanSection = section.trim().ifBlank { "A" }.take(10)
      val cleanPhone = phone.trim().take(20)
      val cleanAddress = address.trim().take(200)
      val cleanDob = dateOfBirth.trim().take(30)
      val cleanAdmissionDate = admissionDate.trim().take(30)
      val cleanSession = academicSession.trim().ifBlank { "2026-27" }.take(20)
      val cleanPreviousSchool = previousSchool.trim().take(120)
      val safeFee = monthlyFeeAmount.coerceIn(0L, 100000L)

      require(cleanAdmission.isNotEmpty()) { "Admission number is required" }
      require(cleanRoll.isNotEmpty()) { "Roll number is required" }
      require(cleanName.length >= 2) { "Full name must be at least 2 characters" }
      require(cleanFather.length >= 2) { "Father's name must be at least 2 characters" }
      require(cleanClass.isNotEmpty()) { "Class is required" }

      val collectionRef = db.collection("users").document(uid).collection("students")

      // Prevent duplicate Admission Numbers case-insensitively across school records
      val existingDocs = collectionRef.get().await().documents
      val duplicateFound = existingDocs.any { doc ->
        doc.id != existingStudentId &&
          doc.getString("admissionNo")?.trim()?.equals(cleanAdmission, ignoreCase = true) == true
      }
      require(!duplicateFound) { "Duplicate Admission Number: $cleanAdmission already exists" }

      val docRef = if (existingStudentId.isNullOrBlank()) {
        collectionRef.document()
      } else {
        collectionRef.document(existingStudentId)
      }

      var resolvedPhotoUrl = photoUrl.trim().take(1000)
      var photoWarning: String? = null
      if (newPhotoJpegBytes != null && newPhotoJpegBytes.isNotEmpty()) {
        try {
          val storageRef = storageProvider().reference
            .child("users/$uid/students/${docRef.id}_photo.jpg")
          storageRef.putBytes(newPhotoJpegBytes).await()
          resolvedPhotoUrl = storageRef.downloadUrl.await().toString().take(1000)
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          photoWarning = e.localizedMessage ?: "Storage upload unavailable"
        }
      }

      if (existingStudentId.isNullOrBlank()) {
        val payload = mutableMapOf<String, Any>(
          "userId" to uid,
          "admissionNo" to cleanAdmission,
          "rollNumber" to cleanRoll,
          "fullName" to cleanName,
          "fatherName" to cleanFather,
          "className" to cleanClass,
          "section" to cleanSection,
          "gender" to gender.name,
          "academicSession" to cleanSession,
          "monthlyFeeAmount" to safeFee,
          "status" to status.name,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (cleanMother.isNotEmpty()) payload["motherName"] = cleanMother
        if (cleanPhone.isNotEmpty()) payload["phone"] = cleanPhone
        if (cleanAddress.isNotEmpty()) payload["address"] = cleanAddress
        if (resolvedPhotoUrl.isNotEmpty()) payload["photoUrl"] = resolvedPhotoUrl
        if (cleanDob.isNotEmpty()) payload["dateOfBirth"] = cleanDob
        if (cleanAdmissionDate.isNotEmpty()) payload["admissionDate"] = cleanAdmissionDate
        if (cleanPreviousSchool.isNotEmpty()) payload["previousSchool"] = cleanPreviousSchool

        docRef.set(payload).await()
        Result.success(StudentSaveOutcome(docRef.id, photoWarning))
      } else {
        val updatePayload = mapOf<String, Any>(
          "admissionNo" to cleanAdmission,
          "rollNumber" to cleanRoll,
          "fullName" to cleanName,
          "fatherName" to cleanFather,
          "motherName" to cleanMother,
          "className" to cleanClass,
          "section" to cleanSection,
          "gender" to gender.name,
          "phone" to cleanPhone,
          "address" to cleanAddress,
          "photoUrl" to resolvedPhotoUrl,
          "dateOfBirth" to cleanDob,
          "admissionDate" to cleanAdmissionDate,
          "academicSession" to cleanSession,
          "previousSchool" to cleanPreviousSchool,
          "monthlyFeeAmount" to safeFee,
          "status" to status.name,
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(updatePayload).await()
        Result.success(StudentSaveOutcome(existingStudentId, photoWarning))
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val op = if (existingStudentId.isNullOrBlank()) OperationType.CREATE else OperationType.UPDATE
      handleFirestoreError(e, op, "$basePath/${existingStudentId ?: "new"}")
      Result.failure(e)
    }
  }

  suspend fun toggleStudentStatus(
    userId: String,
    studentId: String,
    newStatus: StudentStatus
  ): Result<Unit> {
    val docPath = "users/$userId/students/$studentId"
    return try {
      val uid = requireUserId(userId)
      require(studentId.isNotBlank()) { "Student ID must not be blank" }
      db.collection("users").document(uid).collection("students").document(studentId)
        .update(
          mapOf(
            "status" to newStatus.name,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deleteStudent(userId: String, studentId: String): Result<Unit> {
    val docPath = "users/$userId/students/$studentId"
    return try {
      val uid = requireUserId(userId)
      require(studentId.isNotBlank()) { "Student ID must not be blank" }
      db.collection("users").document(uid).collection("students").document(studentId)
        .delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  // --- FEE STRUCTURES ---

  fun observeFeeStructures(userId: String): Flow<List<FeeStructure>> = flow {
    val uid = requireUserId(userId)
    val path = "users/$uid/feeStructures"
    emitAll(
      db.collection("users").document(uid).collection("feeStructures")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            FeeStructure::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
          )
        }
        .wrapFirestoreFlow(OperationType.LIST, path, uid)
    )
  }

  suspend fun saveFeeStructure(
    userId: String,
    academicSession: String = "2026-27",
    className: String,
    monthlyTuitionFee: Long,
    admissionFee: Long = 0L,
    examFee: Long = 0L,
    developmentFee: Long = 0L,
    otherCharges: Long = 0L,
    lateFee: Long = 0L,
    discount: Long = 0L,
    computerFee: Long = 0L,
    transportFee: Long = 0L
  ): Result<Unit> {
    val cleanSession = academicSession.trim().ifBlank { "2026-27" }.take(20)
    val cleanClass = className.trim().take(30)
    val safeSessionSlug = cleanSession.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
    val safeClassSlug = cleanClass.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
    val structureId = "${safeSessionSlug}_${safeClassSlug}".take(120)
    val docPath = "users/$userId/feeStructures/$structureId"
    return try {
      val uid = requireUserId(userId)
      require(safeClassSlug.isNotEmpty()) { "Invalid class name" }
      val docRef = db.collection("users").document(uid)
        .collection("feeStructures").document(structureId)
      val exists = docRef.get().await().exists()

      val tuition = monthlyTuitionFee.coerceIn(0L, 100000L)
      val admission = admissionFee.coerceIn(0L, 100000L)
      val exam = examFee.coerceIn(0L, 100000L)
      val development = developmentFee.coerceIn(0L, 100000L)
      val other = otherCharges.coerceIn(0L, 100000L)
      val late = lateFee.coerceIn(0L, 50000L)
      val disc = discount.coerceIn(0L, 100000L)
      val computer = computerFee.coerceIn(0L, 100000L)
      val transport = transportFee.coerceIn(0L, 100000L)

      if (exists) {
        docRef.update(
          mapOf(
            "academicSession" to cleanSession,
            "className" to cleanClass,
            "monthlyTuitionFee" to tuition,
            "admissionFee" to admission,
            "examFee" to exam,
            "developmentFee" to development,
            "otherCharges" to other,
            "lateFee" to late,
            "discount" to disc,
            "computerFee" to computer,
            "transportFee" to transport,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      } else {
        docRef.set(
          mapOf(
            "userId" to uid,
            "academicSession" to cleanSession,
            "className" to cleanClass,
            "monthlyTuitionFee" to tuition,
            "admissionFee" to admission,
            "examFee" to exam,
            "developmentFee" to development,
            "otherCharges" to other,
            "lateFee" to late,
            "discount" to disc,
            "computerFee" to computer,
            "transportFee" to transport,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  // --- FEE PAYMENTS ---

  fun observeFeePayments(userId: String): Flow<List<FeePayment>> = flow {
    val uid = requireUserId(userId)
    val path = "users/$uid/feePayments"
    emitAll(
      db.collection("users").document(uid).collection("feePayments")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            FeePayment::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
          ).sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .wrapFirestoreFlow(OperationType.LIST, path, uid)
    )
  }

  suspend fun collectFeePayment(
    userId: String,
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
    paidOnDate: String,
    paidOnIsoDate: String,
    remarks: String,
    collectedBy: String,
    idempotencyKey: String
  ): Result<FeePayment> {
    val cleanSession = academicYear.trim().ifBlank { "2026-27" }.take(15)
    val safeYear = cleanSession.replace(Regex("[^a-zA-Z0-9_-]+"), "_")
    val lockId = "${student.id}_${safeYear}_m${monthIndex}".take(120)
    val docPath = "users/$userId/feePayments/$lockId"
    return try {
      val uid = requireUserId(userId)
      require(student.id.isNotBlank()) { "Student ID cannot be blank" }
      require(monthIndex in 1..12) { "Month index must be between 1 and 12" }

      val safeTuition = tuitionFee.coerceIn(0L, 100000L)
      val safeOther = otherFee.coerceIn(0L, 100000L)
      val safeLate = lateFee.coerceIn(0L, 50000L)
      val safeDiscount = discount.coerceIn(0L, 100000L)
      val safePrevBalance = previousBalance.coerceIn(0L, 1000000L)
      val netMonthTotal = (safeTuition + safeOther + safeLate - safeDiscount).coerceIn(0L, 500000L)

      val cleanRemarks = remarks.trim().take(200)
      val cleanCollector = collectedBy.trim().ifBlank { "School Admin" }.take(80)
      val cleanDate = paidOnDate.trim().take(30)
      val cleanIsoDate = paidOnIsoDate.trim().take(20)
      val cleanMonthName = monthName.trim().ifBlank { "Month $monthIndex" }.take(20)
      val cleanIdempotency = idempotencyKey.trim().take(80)
      val sessionStartYear = Regex("""(\d{4})""").find(cleanSession)?.groupValues?.getOrNull(1) ?: "2026"

      val userRef = db.collection("users").document(uid)
      val studentRef = userRef.collection("students").document(student.id)
      val counterRef = userRef.collection("feeMeta").document("receipt_counter")
      val lockRef = userRef.collection("feeMonthLocks").document(lockId)

      val saved = db.runTransaction { transaction ->
        val studentSnap = transaction.get(studentRef)
        check(studentSnap.exists()) { "Selected student does not exist in school database." }

        val counterSnap = transaction.get(counterRef)
        val nextSeq = (counterSnap.getLong("lastSeq") ?: 0L) + 1L

        val lockSnap = transaction.get(lockRef)
        val lockExists = lockSnap.exists()
        val alreadyFullyPaid = lockSnap.getBoolean("isFullyPaid") == true
        check(!alreadyFullyPaid) {
          "Duplicate payment prevented: $cleanMonthName ($cleanSession) is already fully paid."
        }
        val lastKey = lockSnap.getString("lastSubmissionKey").orEmpty()
        if (cleanIdempotency.isNotEmpty() && lastKey == cleanIdempotency) {
          throw IllegalStateException("Duplicate submission prevented for $cleanMonthName.")
        }

        val existingPaidForMonth = (lockSnap.getLong("paidAmount") ?: 0L).coerceAtLeast(0L)
        val dueForMonthBefore = (netMonthTotal - existingPaidForMonth).coerceAtLeast(0L)
        if (dueForMonthBefore == 0L && existingPaidForMonth > 0L) {
          throw IllegalStateException("Fee for $cleanMonthName ($cleanSession) is already settled.")
        }

        val actualPaid = when (paymentType) {
          PaymentType.FULL -> dueForMonthBefore
          PaymentType.PARTIAL -> {
            require(amountPaid in 1L..dueForMonthBefore) {
              "Partial payment must be between INR 1 and INR $dueForMonthBefore"
            }
            amountPaid
          }
        }

        val newPaidForMonth = (existingPaidForMonth + actualPaid).coerceIn(0L, 500000L)
        val monthRemainingAfter = (netMonthTotal - newPaidForMonth).coerceAtLeast(0L)
        val isNowFullyPaid = monthRemainingAfter == 0L
        val resolvedStatus = if (isNowFullyPaid) PaymentStatus.PAID else PaymentStatus.PARTIAL
        val resolvedRemainingBalance = (safePrevBalance + monthRemainingAfter).coerceIn(0L, 1000000L)

        val receiptNo = "SRJP-$sessionStartYear-${nextSeq.toString().padStart(5, '0')}"
        val paymentId = "pay_${student.id}_${safeYear}_m${monthIndex}_${nextSeq}".take(120)
        val paymentRef = userRef.collection("feePayments").document(paymentId)

        if (counterSnap.exists()) {
          transaction.update(
            counterRef,
            mapOf(
              "lastSeq" to nextSeq,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
        } else {
          transaction.set(
            counterRef,
            mapOf(
              "userId" to uid,
              "lastSeq" to nextSeq,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
        }

        if (lockExists) {
          transaction.update(
            lockRef,
            mapOf(
              "expectedAmount" to netMonthTotal,
              "paidAmount" to newPaidForMonth,
              "isFullyPaid" to isNowFullyPaid,
              "lastReceiptNo" to receiptNo,
              "lastSubmissionKey" to cleanIdempotency,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
        } else {
          transaction.set(
            lockRef,
            mapOf(
              "userId" to uid,
              "studentId" to student.id,
              "academicYear" to cleanSession,
              "monthIndex" to monthIndex.toLong(),
              "expectedAmount" to netMonthTotal,
              "paidAmount" to newPaidForMonth,
              "isFullyPaid" to isNowFullyPaid,
              "lastReceiptNo" to receiptNo,
              "lastSubmissionKey" to cleanIdempotency,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
        }

        val payload = mutableMapOf<String, Any>(
          "userId" to uid,
          "receiptNo" to receiptNo,
          "studentId" to student.id,
          "studentName" to student.fullName.take(80),
          "fatherName" to student.fatherName.take(80),
          "admissionNo" to student.admissionNo.take(30),
          "className" to student.className.take(30),
          "academicYear" to cleanSession,
          "monthIndex" to monthIndex.toLong(),
          "monthName" to cleanMonthName,
          "tuitionFee" to safeTuition,
          "otherFee" to safeOther,
          "lateFee" to safeLate,
          "discount" to safeDiscount,
          "previousBalance" to safePrevBalance,
          "totalAmount" to netMonthTotal,
          "amountPaid" to actualPaid,
          "remainingBalance" to resolvedRemainingBalance,
          "paymentType" to paymentType.name,
          "paymentStatus" to resolvedStatus.name,
          "paymentMode" to paymentMode.name,
          "paidOnDate" to cleanDate,
          "paidOnIsoDate" to cleanIsoDate,
          "idempotencyKey" to cleanIdempotency,
          "collectedBy" to cleanCollector,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (cleanRemarks.isNotEmpty()) {
          payload["remarks"] = cleanRemarks
        }
        transaction.set(paymentRef, payload)

        FeePayment(
          id = paymentId,
          userId = uid,
          receiptNo = receiptNo,
          studentId = student.id,
          studentName = student.fullName,
          fatherName = student.fatherName,
          admissionNo = student.admissionNo,
          className = student.className,
          academicYear = cleanSession,
          monthIndex = monthIndex.toLong(),
          monthName = cleanMonthName,
          tuitionFee = safeTuition,
          otherFee = safeOther,
          lateFee = safeLate,
          discount = safeDiscount,
          previousBalance = safePrevBalance,
          totalAmount = netMonthTotal,
          amountPaid = actualPaid,
          remainingBalance = resolvedRemainingBalance,
          paymentType = paymentType.name,
          paymentStatus = resolvedStatus.name,
          paymentMode = paymentMode.name,
          paidOnDate = cleanDate,
          paidOnIsoDate = cleanIsoDate,
          idempotencyKey = cleanIdempotency,
          remarks = cleanRemarks,
          collectedBy = cleanCollector
        )
      }.await()

      Result.success(saved)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun refundOrCorrectFeePayment(
    userId: String,
    payment: FeePayment,
    newStatus: PaymentStatus,
    reason: String
  ): Result<FeePayment> {
    val docPath = "users/$userId/feePayments/${payment.id}"
    return try {
      val uid = requireUserId(userId)
      require(payment.id.isNotBlank()) { "Payment ID must not be blank" }
      require(newStatus == PaymentStatus.REFUNDED || newStatus == PaymentStatus.CORRECTED) {
        "Only REFUNDED or CORRECTED status updates are permitted"
      }
      val cleanReason = reason.trim().take(200)
      require(cleanReason.length >= 3) { "Please provide a valid reason (at least 3 characters)" }

      val safeYear = payment.academicYear.trim().replace(Regex("[^a-zA-Z0-9_-]+"), "_")
      val lockId = "${payment.studentId}_${safeYear}_m${payment.monthIndex}".take(120)
      val userRef = db.collection("users").document(uid)
      val paymentRef = userRef.collection("feePayments").document(payment.id)
      val lockRef = userRef.collection("feeMonthLocks").document(lockId)

      val updated = db.runTransaction { transaction ->
        val paymentSnap = transaction.get(paymentRef)
        check(paymentSnap.exists()) { "Payment record does not exist" }
        val currentStatus = PaymentStatus.fromRaw(paymentSnap.getString("paymentStatus"))
        check(currentStatus != PaymentStatus.REFUNDED && currentStatus != PaymentStatus.CORRECTED) {
          "Payment receipt is already marked as ${currentStatus.name}"
        }

        val lockSnap = transaction.get(lockRef)
        if (lockSnap.exists()) {
          val oldPaid = (lockSnap.getLong("paidAmount") ?: 0L).coerceAtLeast(0L)
          val reversedAmount = payment.effectivePaidAmount()
          val newPaid = (oldPaid - reversedAmount).coerceAtLeast(0L)
          transaction.update(
            lockRef,
            mapOf(
              "paidAmount" to newPaid,
              "isFullyPaid" to false,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
        }

        transaction.update(
          paymentRef,
          mapOf(
            "paymentStatus" to newStatus.name,
            "refundOrCorrectionReason" to cleanReason,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        )

        payment.copy(
          paymentStatus = newStatus.name,
          refundOrCorrectionReason = cleanReason
        )
      }.await()

      Result.success(updated)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }
}
