package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class PortalRole {
  ADMIN,
  TEACHER,
  STUDENT;

  companion object {
    fun fromRaw(raw: String?): PortalRole =
      entries.firstOrNull { it.name == raw } ?: ADMIN
  }
}

enum class AdminDestination {
  HOME,
  STUDENTS,
  FEES,
  REPORTS,
  PROFILE,
  TEACHERS,
  ATTENDANCE,
  HOMEWORK,
  EXAMINATION,
  RESULTS,
  NOTICE_BOARD,
  TIMETABLE,
  SETTINGS
}

enum class Gender {
  MALE,
  FEMALE,
  OTHER;

  companion object {
    fun fromRaw(raw: String?): Gender =
      entries.firstOrNull { it.name == raw } ?: MALE
  }
}

enum class StudentStatus {
  ACTIVE,
  INACTIVE;

  companion object {
    fun fromRaw(raw: String?): StudentStatus =
      entries.firstOrNull { it.name == raw } ?: ACTIVE
  }
}

enum class PaymentMode {
  CASH,
  UPI,
  BANK_TRANSFER,
  CHEQUE;

  companion object {
    fun fromRaw(raw: String?): PaymentMode =
      entries.firstOrNull { it.name == raw } ?: CASH
  }
}

enum class PaymentType {
  FULL,
  PARTIAL;

  companion object {
    fun fromRaw(raw: String?): PaymentType =
      entries.firstOrNull { it.name == raw } ?: FULL
  }
}

enum class PaymentStatus {
  PAID,
  PARTIAL,
  REFUNDED,
  CORRECTED;

  companion object {
    fun fromRaw(raw: String?): PaymentStatus =
      entries.firstOrNull { it.name == raw } ?: PAID
  }
}

enum class AttendanceStatus {
  PRESENT,
  ABSENT,
  LEAVE;

  companion object {
    fun fromRaw(raw: String?): AttendanceStatus =
      entries.firstOrNull { it.name == raw } ?: PRESENT
  }
}

enum class NoticeAudience {
  ALL,
  TEACHERS,
  PARENTS;

  companion object {
    fun fromRaw(raw: String?): NoticeAudience =
      entries.firstOrNull { it.name == raw } ?: ALL
  }
}

enum class NoticePriority {
  NORMAL,
  IMPORTANT,
  URGENT;

  companion object {
    fun fromRaw(raw: String?): NoticePriority =
      entries.firstOrNull { it.name == raw } ?: NORMAL
  }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val portalMode: String = PortalRole.ADMIN.name,
  val preferredLanguage: String = "EN",
  val staffCode: String = "",
  val admissionNo: String = "",
  val assignedClass: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StudentRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val admissionNo: String = "",
  val rollNumber: String = "",
  val fullName: String = "",
  val fatherName: String = "",
  val motherName: String = "",
  val className: String = "",
  val section: String = "A",
  val gender: String = Gender.MALE.name,
  val phone: String = "",
  val address: String = "",
  val photoUrl: String = "",
  val dateOfBirth: String = "",
  val admissionDate: String = "",
  val academicSession: String = "2026-27",
  val previousSchool: String = "",
  val monthlyFeeAmount: Long = 0L,
  val status: String = StudentStatus.ACTIVE.name,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FeeStructure(
  @DocumentId val id: String = "",
  val userId: String = "",
  val academicSession: String = "2026-27",
  val className: String = "",
  val monthlyTuitionFee: Long = 0L,
  val admissionFee: Long = 0L,
  val examFee: Long = 0L,
  val developmentFee: Long = 0L,
  val otherCharges: Long = 0L,
  val lateFee: Long = 0L,
  val discount: Long = 0L,
  val computerFee: Long = 0L,
  val transportFee: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun effectiveOtherCharges(): Long = (otherCharges + computerFee + transportFee).coerceAtLeast(0L)
  fun annualOneTimeCharges(): Long = (admissionFee + examFee + developmentFee).coerceAtLeast(0L)
}

@Keep
@IgnoreExtraProperties
data class FeePayment(
  @DocumentId val id: String = "",
  val userId: String = "",
  val receiptNo: String = "",
  val studentId: String = "",
  val studentName: String = "",
  val fatherName: String = "",
  val admissionNo: String = "",
  val className: String = "",
  val academicYear: String = "2026-27",
  val monthIndex: Long = 1L,
  val monthName: String = "April",
  val tuitionFee: Long = 0L,
  val otherFee: Long = 0L,
  val lateFee: Long = 0L,
  val discount: Long = 0L,
  val previousBalance: Long = 0L,
  val totalAmount: Long = 0L,
  val amountPaid: Long = 0L,
  val remainingBalance: Long = 0L,
  val paymentType: String = PaymentType.FULL.name,
  val paymentStatus: String = PaymentStatus.PAID.name,
  val paymentMode: String = PaymentMode.CASH.name,
  val paidOnDate: String = "",
  val paidOnIsoDate: String = "",
  val idempotencyKey: String = "",
  val refundOrCorrectionReason: String = "",
  val remarks: String = "",
  val collectedBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  fun isVoided(): Boolean {
    val st = PaymentStatus.fromRaw(paymentStatus)
    return st == PaymentStatus.REFUNDED || st == PaymentStatus.CORRECTED
  }

  fun effectivePaidAmount(): Long {
    if (isVoided()) return 0L
    return if (amountPaid > 0L) amountPaid else totalAmount
  }

  fun effectiveIsoDate(): String {
    if (paidOnIsoDate.isNotBlank()) return paidOnIsoDate
    return paidOnDate
  }
}

@Keep
@IgnoreExtraProperties
data class TeacherRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val staffCode: String = "",
  val fullName: String = "",
  val subject: String = "",
  val assignedClass: String = "",
  val phone: String = "",
  val qualification: String = "",
  val status: String = StudentStatus.ACTIVE.name,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AttendanceRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val studentId: String = "",
  val studentName: String = "",
  val admissionNo: String = "",
  val className: String = "",
  val section: String = "A",
  val academicSession: String = "2026-27",
  val dateKey: String = "",
  val status: String = AttendanceStatus.PRESENT.name,
  val markedBy: String = "",
  val editedBy: String = "",
  val editReason: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class HomeworkItem(
  @DocumentId val id: String = "",
  val userId: String = "",
  val className: String = "",
  val subject: String = "",
  val title: String = "",
  val description: String = "",
  val dueDate: String = "",
  val assignedBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ExamSchedule(
  @DocumentId val id: String = "",
  val userId: String = "",
  val examName: String = "",
  val className: String = "",
  val subject: String = "",
  val examDate: String = "",
  val maxMarks: Long = 100L,
  val roomNo: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ExamResult(
  @DocumentId val id: String = "",
  val userId: String = "",
  val examId: String = "",
  val examName: String = "",
  val studentId: String = "",
  val studentName: String = "",
  val admissionNo: String = "",
  val className: String = "",
  val subject: String = "",
  val marksObtained: Long = 0L,
  val maxMarks: Long = 100L,
  val grade: String = "A",
  val remarks: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SchoolNotice(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val body: String = "",
  val audience: String = NoticeAudience.ALL.name,
  val priority: String = NoticePriority.NORMAL.name,
  val publishedDate: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TimetableEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val className: String = "",
  val dayOfWeek: String = "",
  val periodLabel: String = "",
  val subject: String = "",
  val teacherName: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SchoolSettings(
  @DocumentId val id: String = "",
  val userId: String = "",
  val schoolName: String = "Shree Ram Jiyavan Patel Junior High School",
  val schoolAddress: String = "Bechu Ka Purwa, Airayan, Khaga, Fatehpur, Uttar Pradesh",
  val academicSession: String = "2026-27",
  val principalName: String = "Principal",
  val contactPhone: String = "",
  val totalConfiguredClasses: Long = 12L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}
