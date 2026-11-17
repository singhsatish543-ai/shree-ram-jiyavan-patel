package com.example.i18n

interface AppStrings {
  val schoolName: String
  val schoolShortName: String
  val schoolAddress: String
  val schoolMotto: String
  val academicSessionLabel: String
  val continueBtn: String
  val splashLoading: String

  // Login & Roles
  val portalTitle: String
  val portalSubtitle: String
  val adminRole: String
  val teacherRole: String
  val studentRole: String
  val adminLoginTitle: String
  val adminLoginDesc: String
  val teacherLoginTitle: String
  val teacherLoginDesc: String
  val studentLoginTitle: String
  val studentLoginDesc: String
  val adminNameHint: String
  val staffCodeHint: String
  val assignedClassHint: String
  val admissionNoHint: String
  val signInWithGoogle: String
  val signingIn: String
  val googleSignInNote: String
  val signOut: String
  val switchPortal: String

  // Bottom Navigation (5 items: Home, Students, Fees, Reports, Profile)
  val navHome: String
  val navDashboard: String
  val navStudents: String
  val navFees: String
  val navReports: String
  val navProfile: String

  // Admin Dashboard Stats & Sections
  val welcomeAdmin: String
  val totalStudents: String
  val totalTeachers: String
  val totalClasses: String
  val todaysAttendance: String
  val totalFeeCollection: String
  val pendingFees: String
  val activeStudents: String
  val monthlyCollection: String
  val pendingThisMonth: String
  val sessionCollection: String
  val feesQuickSummary: String
  val todaysCollection: String
  val thisMonthsCollection: String
  val totalOutstandingFees: String
  val quickActions: String
  val schoolModulesTitle: String
  val recentActivitiesTitle: String
  val recentlyAddedStudents: String
  val recentAttendanceTitle: String
  val noRecentAttendance: String
  val noRecentStudents: String
  val backToDashboard: String
  val notificationsLabel: String

  // 11 Module Titles
  val studentManagement: String
  val teacherManagement: String
  val collectMonthlyFees: String
  val feesCollectionModule: String
  val attendanceModule: String
  val homeworkModule: String
  val examinationModule: String
  val resultsModule: String
  val noticeBoardModule: String
  val timetableModule: String
  val reportsModule: String
  val schoolSettingsModule: String

  val addNewStudent: String
  val feeStructureSetup: String
  val classWiseOverview: String
  val recentReceipts: String
  val noReceiptsYet: String
  val noStudentsOverview: String
  val viewAll: String

  // Student Management
  val searchStudentsHint: String
  val allClasses: String
  val addStudent: String
  val editStudent: String
  val deleteStudent: String
  val deleteStudentConfirm: String
  val cancel: String
  val save: String
  val delete: String
  val admissionNo: String
  val rollNumber: String
  val fullName: String
  val fatherName: String
  val motherName: String
  val classLabel: String
  val section: String
  val gender: String
  val male: String
  val female: String
  val other: String
  val phone: String
  val address: String
  val dateOfBirth: String
  val admissionDate: String
  val academicSessionField: String
  val previousSchool: String
  val studentPhoto: String
  val pickPhoto: String
  val changePhoto: String
  val autoGenerateAdmNo: String
  val viewProfile: String
  val studentProfileTitle: String
  val classWiseStrengthTitle: String
  val exportPdf: String
  val exportExcel: String
  val exportingReport: String
  val exportReadyTitle: String
  val noStudentsToExport: String
  val duplicateAdmissionError: String
  val invalidMobileError: String
  val academicDetailsSection: String
  val familyPersonalSection: String
  val contactHistorySection: String
  val inactiveStudents: String
  val toggleStatusLabel: String
  val clear: String
  val monthlyFee: String
  val status: String
  val active: String
  val inactive: String
  val collectFee: String
  val edit: String
  val noStudentsFound: String
  val noStudentsHint: String
  val studentSavedMsg: String
  val studentDeletedMsg: String
  val validationRequiredField: String

  // Monthly Fees Collection & Complete Fees Management Module
  val feesModuleTitle: String
  val tabFeesDashboard: String
  val tabCollectFees: String
  val tabPendingFees: String
  val tabMonthlyTracker: String
  val tabReceipts: String
  val tabFeeStructure: String
  val tabFeeReports: String
  val session: String
  val selectMonth: String
  val paidCount: String
  val dueCount: String
  val collectedTotal: String
  val paidBadge: String
  val partialBadge: String
  val unpaidBadge: String
  val refundedBadge: String
  val correctedBadge: String
  val dueBadge: String
  val viewReceipt: String
  val tuitionFee: String
  val admissionFeeLabel: String
  val developmentFeeLabel: String
  val otherChargesLabel: String
  val otherFee: String
  val lateFee: String
  val discount: String
  val netTotal: String
  val totalAmountLabel: String
  val previousBalanceLabel: String
  val amountPaidLabel: String
  val remainingBalanceLabel: String
  val fullPaymentLabel: String
  val partialPaymentLabel: String
  val paymentTypeLabel: String
  val paymentMode: String
  val modeCash: String
  val modeUpi: String
  val modeBank: String
  val modeCheque: String
  val remarksHint: String
  val confirmCollectFee: String
  val feeCollectedSuccess: String
  val receiptTitle: String
  val receiptNoLabel: String
  val dateLabel: String
  val monthLabel: String
  val collectedByLabel: String
  val authorizedSignatureLabel: String
  val downloadPdfLabel: String
  val printReceiptLabel: String
  val shareReceipt: String
  val receiptSavedMsg: String
  val close: String
  val refundCorrectionBtn: String
  val refundCorrectionTitle: String
  val refundReasonHint: String
  val markRefundedBtn: String
  val markCorrectedBtn: String
  val refundRecordedMsg: String
  val deleteReceipt: String
  val deleteReceiptConfirm: String
  val feeStructureTitle: String
  val examFee: String
  val computerFee: String
  val transportFee: String
  val saveStructure: String
  val structureSavedMsg: String
  val applyToClassStudents: String
  val yearlyTrackerLabel: String
  val totalExpectedFeesLabel: String
  val totalFeesCollectedLabel: String
  val totalPendingFeesLabel: String
  val fullyPaidStudentsLabel: String
  val partiallyPaidStudentsLabel: String
  val unpaidStudentsLabel: String
  val paidMonthsLabel: String
  val pendingMonthsLabel: String
  val selectStudentForFeeHint: String
  val monthAlreadyPaidWarning: String
  val invalidAmountError: String
  val studentWiseLabel: String
  val classWiseLabel: String
  val monthWiseLabel: String
  val feeReminderListTitle: String
  val sendReminderLabel: String
  val noPendingFeesTitle: String
  val noPendingFeesDesc: String
  val reportDailyCollection: String
  val reportMonthlyCollection: String
  val reportAnnualCollection: String
  val reportClassWiseCollection: String
  val reportCashCollection: String
  val reportUpiCollection: String
  val reportPendingFees: String
  val reportStudentWiseFee: String
  val selectDateLabel: String
  val collectionReportTitle: String

  // Teacher Management
  val addTeacher: String
  val editTeacher: String
  val subjectLabel: String
  val qualificationLabel: String
  val noTeachersFound: String
  val noTeachersHint: String
  val teacherSavedMsg: String
  val teacherDeletedMsg: String

  // Attendance
  val attendanceDateLabel: String
  val presentLabel: String
  val absentLabel: String
  val leaveLabel: String
  val markAllPresent: String
  val attendanceSavedMsg: String
  val notMarkedYet: String
  val tabMarkAttendance: String
  val tabDailyReport: String
  val tabMonthlyReport: String
  val tabStudentWiseReport: String
  val allSections: String
  val pickDateLabel: String
  val todayLabel: String
  val markedCountLabel: String
  val unmarkedCountLabel: String
  val attendancePercentageLabel: String
  val markedLockedLabel: String
  val duplicateAttendanceBanner: String
  val adminEditOnlyHint: String
  val editAttendanceTitle: String
  val editAttendanceBtn: String
  val editReasonHint: String
  val attendanceUpdatedMsg: String
  val markedByLabel: String
  val editedByLabel: String
  val workingDaysLabel: String
  val monthlySummaryTitle: String
  val dailyBreakdownTitle: String
  val studentHistoryTitle: String
  val noAttendanceRecordsTitle: String
  val noAttendanceRecordsDesc: String
  val teacherAttendanceTab: String
  val teacherRosterTab: String
  val attendanceHistoryEmpty: String
  val attendanceMarkHint: String

  // Homework, Exams, Results
  val addHomework: String
  val titleLabel: String
  val descriptionLabel: String
  val dueDateLabel: String
  val noHomeworkFound: String
  val homeworkSavedMsg: String
  val addExam: String
  val examNameLabel: String
  val examDateLabel: String
  val maxMarksLabel: String
  val roomNoLabel: String
  val noExamsFound: String
  val examSavedMsg: String
  val addResult: String
  val marksObtainedLabel: String
  val gradeLabel: String
  val selectStudentLabel: String
  val selectExamLabel: String
  val noResultsFound: String
  val resultSavedMsg: String

  // Notice Board, Timetable, Reports, Settings, Profile
  val addNotice: String
  val noticeBodyLabel: String
  val audienceLabel: String
  val priorityLabel: String
  val noNoticesFound: String
  val noticeSavedMsg: String
  val addTimetable: String
  val dayOfWeekLabel: String
  val periodLabel: String
  val teacherNameLabel: String
  val noTimetableFound: String
  val timetableSavedMsg: String
  val reportsSummaryTitle: String
  val feeModeBreakdown: String
  val genderBreakdown: String
  val attendanceSummaryLabel: String
  val principalNameLabel: String
  val contactPhoneLabel: String
  val totalConfiguredClassesLabel: String
  val settingsSavedMsg: String
  val adminProfileTitle: String
  val accountUidLabel: String
  val languageSettingLabel: String

  // Teacher & Student Portals
  val teacherPortalTitle: String
  val teacherPortalSubtitle: String
  val studentPortalTitle: String
  val studentPortalSubtitle: String
  val enterAdmissionLookup: String
  val noMatchingStudent: String
  val feeHistoryTitle: String
  val retry: String
  val errorLoadingData: String

  val months: List<String>
  val monthsShort: List<String>
  val classOptions: List<String>
  val daysOfWeek: List<String>
}

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
  AppLanguage.EN -> EnglishStrings
  AppLanguage.HI -> HindiStrings
}

val SchoolClasses = listOf(
  "PG", "Nursery", "LKG", "UKG",
  "Class 1", "Class 2", "Class 3", "Class 4", "Class 5",
  "Class 6", "Class 7", "Class 8"
)

val AcademicSessions = listOf("2025-26", "2026-27", "2027-28")

private object EnglishStrings : AppStrings {
  override val schoolName = "Shree Ram Jiyavan Patel Junior High School"
  override val schoolShortName = "SRJP Junior High School"
  override val schoolAddress = "Bechu Ka Purwa, Airayan, Khaga, Fatehpur, Uttar Pradesh"
  override val schoolMotto = "Knowledge • Discipline • Excellence"
  override val academicSessionLabel = "Academic Session 2026-27"
  override val continueBtn = "Continue to Portal"
  override val splashLoading = "Initializing School Portal..."

  override val portalTitle = "School Portal Login"
  override val portalSubtitle = "Select your role to sign in securely"
  override val adminRole = "Admin"
  override val teacherRole = "Teacher"
  override val studentRole = "Student"
  override val adminLoginTitle = "Admin Login"
  override val adminLoginDesc = "Principal & Office Administration — Manage student records, fee structures, and monthly fee collection."
  override val teacherLoginTitle = "Teacher Login"
  override val teacherLoginDesc = "Faculty Portal — View assigned class roster, student profiles, and monthly fee status."
  override val studentLoginTitle = "Student Login"
  override val studentLoginDesc = "Student & Parent Portal — Check monthly fee ledger, dues, and official fee receipts."
  override val adminNameHint = "Administrator Name (e.g., Principal)"
  override val staffCodeHint = "Staff ID / Teacher Code (Optional)"
  override val assignedClassHint = "Assigned Class"
  override val admissionNoHint = "Student Admission No. (Optional)"
  override val signInWithGoogle = "Sign in with Google"
  override val signingIn = "Signing in..."
  override val googleSignInNote = "Secured with Google Sign-In & Cloud Firestore"
  override val signOut = "Sign Out"
  override val switchPortal = "Switch Role"

  override val navHome = "Home"
  override val navDashboard = "Home"
  override val navStudents = "Students"
  override val navFees = "Fees"
  override val navReports = "Reports"
  override val navProfile = "Profile"

  override val welcomeAdmin = "Administration Desk"
  override val totalStudents = "Total Students"
  override val totalTeachers = "Total Teachers"
  override val totalClasses = "Total Classes"
  override val todaysAttendance = "Today's Attendance"
  override val totalFeeCollection = "Total Fee Collection"
  override val pendingFees = "Pending Fees"
  override val activeStudents = "Active Enrolled"
  override val monthlyCollection = "Monthly Collection"
  override val pendingThisMonth = "Pending This Month"
  override val sessionCollection = "Session Total"
  override val feesQuickSummary = "Fees Collection Quick Summary"
  override val todaysCollection = "Today's Collection"
  override val thisMonthsCollection = "This Month's Collection"
  override val totalOutstandingFees = "Total Outstanding Fees"
  override val quickActions = "Quick Actions"
  override val schoolModulesTitle = "School Management Modules"
  override val recentActivitiesTitle = "Recent Activities"
  override val recentlyAddedStudents = "Recently Added Students"
  override val recentAttendanceTitle = "Recent Attendance"
  override val noRecentAttendance = "No attendance marked today. Open Attendance to mark daily attendance."
  override val noRecentStudents = "No students enrolled yet. Add students to see recent admissions."
  override val backToDashboard = "Back"
  override val notificationsLabel = "Notices"

  override val studentManagement = "Student Management"
  override val teacherManagement = "Teacher Management"
  override val collectMonthlyFees = "Collect Monthly Fees"
  override val feesCollectionModule = "Fees Collection"
  override val attendanceModule = "Attendance"
  override val homeworkModule = "Homework"
  override val examinationModule = "Examination"
  override val resultsModule = "Results"
  override val noticeBoardModule = "Notice Board"
  override val timetableModule = "Time Table"
  override val reportsModule = "Reports"
  override val schoolSettingsModule = "School Settings"

  override val addNewStudent = "Add Student"
  override val feeStructureSetup = "Fee Structure"
  override val classWiseOverview = "Class-Wise Strength & Fees"
  override val recentReceipts = "Recent Fee Payments"
  override val noReceiptsYet = "No fee payments recorded yet. Collect monthly fees to see live receipts here."
  override val noStudentsOverview = "No students enrolled yet. Add students to see class-wise strength."
  override val viewAll = "View All"

  override val searchStudentsHint = "Search name, admission no. or phone..."
  override val allClasses = "All Classes"
  override val addStudent = "Add Student"
  override val editStudent = "Edit Student"
  override val deleteStudent = "Delete Student"
  override val deleteStudentConfirm = "Remove this student record permanently?"
  override val cancel = "Cancel"
  override val save = "Save"
  override val delete = "Delete"
  override val admissionNo = "Admission No."
  override val rollNumber = "Roll No."
  override val fullName = "Student Full Name"
  override val fatherName = "Father's Name"
  override val motherName = "Mother's Name"
  override val classLabel = "Class"
  override val section = "Section"
  override val gender = "Gender"
  override val male = "Male"
  override val female = "Female"
  override val other = "Other"
  override val phone = "Mobile Number"
  override val address = "Full Address"
  override val dateOfBirth = "Date of Birth"
  override val admissionDate = "Admission Date"
  override val academicSessionField = "Academic Session"
  override val previousSchool = "Previous School Details"
  override val studentPhoto = "Student Photo"
  override val pickPhoto = "Pick Photo"
  override val changePhoto = "Change Photo"
  override val autoGenerateAdmNo = "Auto"
  override val viewProfile = "Profile"
  override val studentProfileTitle = "Student Profile"
  override val classWiseStrengthTitle = "Class-Wise Student Count"
  override val exportPdf = "PDF"
  override val exportExcel = "Excel"
  override val exportingReport = "Preparing export..."
  override val exportReadyTitle = "Share Student List"
  override val noStudentsToExport = "No students in selected list to export"
  override val duplicateAdmissionError = "Admission number already exists. Please use a unique number."
  override val invalidMobileError = "Enter a valid 10-digit mobile number"
  override val academicDetailsSection = "Academic Details"
  override val familyPersonalSection = "Family & Personal Info"
  override val contactHistorySection = "Contact & Previous School"
  override val inactiveStudents = "Inactive"
  override val toggleStatusLabel = "Enrollment Status"
  override val clear = "Clear"
  override val monthlyFee = "Monthly Tuition Fee (₹)"
  override val status = "Status"
  override val active = "Active"
  override val inactive = "Inactive"
  override val collectFee = "Collect Fee"
  override val edit = "Edit"
  override val noStudentsFound = "No Student Records Yet"
  override val noStudentsHint = "Tap '+ Add Student' to register real students of Shree Ram Jiyavan Patel Junior High School."
  override val studentSavedMsg = "Student record saved"
  override val studentDeletedMsg = "Student record removed"
  override val validationRequiredField = "Please fill all required fields"

  override val feesModuleTitle = "Fees Management"
  override val tabFeesDashboard = "Dashboard"
  override val tabCollectFees = "Collect"
  override val tabPendingFees = "Pending"
  override val tabMonthlyTracker = "Monthly"
  override val tabReceipts = "History"
  override val tabFeeStructure = "Structure"
  override val tabFeeReports = "Reports"
  override val session = "Session"
  override val selectMonth = "Month"
  override val paidCount = "Paid"
  override val dueCount = "Due"
  override val collectedTotal = "Collected"
  override val paidBadge = "PAID"
  override val partialBadge = "PARTIAL"
  override val unpaidBadge = "UNPAID"
  override val refundedBadge = "REFUNDED"
  override val correctedBadge = "CORRECTED"
  override val dueBadge = "DUE"
  override val viewReceipt = "Receipt"
  override val tuitionFee = "Monthly Tuition Fee (₹)"
  override val admissionFeeLabel = "Admission Fee (₹)"
  override val developmentFeeLabel = "Development Fee (₹)"
  override val otherChargesLabel = "Other Charges (₹)"
  override val otherFee = "Exam / Other Charges (₹)"
  override val lateFee = "Late Fee (₹)"
  override val discount = "Discount / Concession (₹)"
  override val netTotal = "Net Payable Total"
  override val totalAmountLabel = "Total Amount"
  override val previousBalanceLabel = "Previous Balance"
  override val amountPaidLabel = "Amount Paid (₹)"
  override val remainingBalanceLabel = "Remaining Balance"
  override val fullPaymentLabel = "Full Payment"
  override val partialPaymentLabel = "Partial Payment"
  override val paymentTypeLabel = "Payment Option"
  override val paymentMode = "Payment Mode"
  override val modeCash = "Cash"
  override val modeUpi = "UPI"
  override val modeBank = "Bank Transfer"
  override val modeCheque = "Cheque"
  override val remarksHint = "Remarks / Transaction Ref (Optional)"
  override val confirmCollectFee = "Confirm & Issue Receipt"
  override val feeCollectedSuccess = "Fee collected & receipt issued"
  override val receiptTitle = "Official Fee Receipt"
  override val receiptNoLabel = "Receipt Number"
  override val dateLabel = "Payment Date"
  override val monthLabel = "Fee Month"
  override val collectedByLabel = "Collected By"
  override val authorizedSignatureLabel = "Authorized Signature"
  override val downloadPdfLabel = "Download PDF"
  override val printReceiptLabel = "Print Receipt"
  override val shareReceipt = "Share Receipt"
  override val receiptSavedMsg = "Receipt PDF saved"
  override val close = "Close"
  override val refundCorrectionBtn = "Refund / Correction"
  override val refundCorrectionTitle = "Admin Refund / Correction"
  override val refundReasonHint = "Reason for refund or correction (Required)"
  override val markRefundedBtn = "Mark Refunded"
  override val markCorrectedBtn = "Mark Corrected"
  override val refundRecordedMsg = "Payment ledger status updated"
  override val deleteReceipt = "Cancel Receipt"
  override val deleteReceiptConfirm = "Mark this fee receipt as refunded or corrected?"
  override val feeStructureTitle = "Class-Wise Fee Structure"
  override val examFee = "Examination Fee (₹)"
  override val computerFee = "Computer Fee (₹)"
  override val transportFee = "Transport Fee (₹)"
  override val saveStructure = "Save Fee Structure"
  override val structureSavedMsg = "Class fee structure updated"
  override val applyToClassStudents = "Configure monthly & annual fee heads per class and academic session"
  override val yearlyTrackerLabel = "12-Month Fee Status (Apr–Mar)"
  override val totalExpectedFeesLabel = "Total Expected Fees"
  override val totalFeesCollectedLabel = "Total Fees Collected"
  override val totalPendingFeesLabel = "Total Pending Fees"
  override val fullyPaidStudentsLabel = "Fully Paid Students"
  override val partiallyPaidStudentsLabel = "Partially Paid Students"
  override val unpaidStudentsLabel = "Unpaid Students"
  override val paidMonthsLabel = "Paid Months"
  override val pendingMonthsLabel = "Pending Months"
  override val selectStudentForFeeHint = "Search and select a student above to view balances and collect fees."
  override val monthAlreadyPaidWarning = "Selected month is already fully paid in this session."
  override val invalidAmountError = "Enter a valid payment amount greater than ₹0"
  override val studentWiseLabel = "Student-wise"
  override val classWiseLabel = "Class-wise"
  override val monthWiseLabel = "Month-wise"
  override val feeReminderListTitle = "Fee Reminder List"
  override val sendReminderLabel = "Send Reminder"
  override val noPendingFeesTitle = "No Pending Fees"
  override val noPendingFeesDesc = "All active students match your current filter with zero pending dues."
  override val reportDailyCollection = "Daily Collection"
  override val reportMonthlyCollection = "Monthly Collection"
  override val reportAnnualCollection = "Annual Collection"
  override val reportClassWiseCollection = "Class-wise Collection"
  override val reportCashCollection = "Cash Collection"
  override val reportUpiCollection = "UPI Collection"
  override val reportPendingFees = "Pending Fees Report"
  override val reportStudentWiseFee = "Student-wise Fee Report"
  override val selectDateLabel = "Date"
  override val collectionReportTitle = "Fee Collection Reports"

  override val addTeacher = "Add Teacher"
  override val editTeacher = "Edit Teacher"
  override val subjectLabel = "Subject"
  override val qualificationLabel = "Qualification"
  override val noTeachersFound = "No Teachers Added Yet"
  override val noTeachersHint = "Tap '+ Add Teacher' to register faculty members for Shree Ram Jiyavan Patel Junior High School."
  override val teacherSavedMsg = "Teacher record saved"
  override val teacherDeletedMsg = "Teacher record removed"

  override val attendanceDateLabel = "Attendance Date"
  override val presentLabel = "Present"
  override val absentLabel = "Absent"
  override val leaveLabel = "Leave"
  override val markAllPresent = "Mark All Unmarked Present"
  override val attendanceSavedMsg = "Attendance saved"
  override val notMarkedYet = "Not Marked"
  override val tabMarkAttendance = "Mark Daily"
  override val tabDailyReport = "Daily Report"
  override val tabMonthlyReport = "Monthly Report"
  override val tabStudentWiseReport = "Student-wise"
  override val allSections = "All Sections"
  override val pickDateLabel = "Pick Date"
  override val todayLabel = "Today"
  override val markedCountLabel = "Marked"
  override val unmarkedCountLabel = "Unmarked"
  override val attendancePercentageLabel = "Attendance %"
  override val markedLockedLabel = "Marked • Locked"
  override val duplicateAttendanceBanner = "Duplicate attendance for the same student and date is automatically prevented."
  override val adminEditOnlyHint = "Only Admin can edit already-marked attendance records."
  override val editAttendanceTitle = "Admin Edit Attendance"
  override val editAttendanceBtn = "Edit"
  override val editReasonHint = "Reason for correction (Optional)"
  override val attendanceUpdatedMsg = "Attendance record updated by Admin"
  override val markedByLabel = "Marked by"
  override val editedByLabel = "Edited by"
  override val workingDaysLabel = "Working Days"
  override val monthlySummaryTitle = "Monthly Attendance Overview"
  override val dailyBreakdownTitle = "Date-wise Breakdown"
  override val studentHistoryTitle = "Student Attendance History"
  override val noAttendanceRecordsTitle = "No Attendance Recorded"
  override val noAttendanceRecordsDesc = "Mark daily attendance in the Mark Daily tab to view live reports and percentages."
  override val teacherAttendanceTab = "Daily Attendance"
  override val teacherRosterTab = "Class Roster & Fees"
  override val attendanceHistoryEmpty = "No attendance records logged for this student yet."
  override val attendanceMarkHint = "Tap Present, Absent, or Leave to record attendance for today's date."

  override val addHomework = "Assign Homework"
  override val titleLabel = "Title"
  override val descriptionLabel = "Instructions / Details"
  override val dueDateLabel = "Due Date"
  override val noHomeworkFound = "No Homework Assigned Yet"
  override val homeworkSavedMsg = "Homework published"
  override val addExam = "Schedule Exam"
  override val examNameLabel = "Exam Name (e.g., Half Yearly)"
  override val examDateLabel = "Exam Date"
  override val maxMarksLabel = "Max Marks"
  override val roomNoLabel = "Room No. (Optional)"
  override val noExamsFound = "No Examinations Scheduled"
  override val examSavedMsg = "Exam schedule saved"
  override val addResult = "Enter Result"
  override val marksObtainedLabel = "Marks Obtained"
  override val gradeLabel = "Grade"
  override val selectStudentLabel = "Student"
  override val selectExamLabel = "Exam"
  override val noResultsFound = "No Results Recorded Yet"
  override val resultSavedMsg = "Result saved"

  override val addNotice = "Publish Notice"
  override val noticeBodyLabel = "Notice Details"
  override val audienceLabel = "Audience"
  override val priorityLabel = "Priority"
  override val noNoticesFound = "No Notices Published"
  override val noticeSavedMsg = "Notice published to Notice Board"
  override val addTimetable = "Add Period"
  override val dayOfWeekLabel = "Day"
  override val periodLabel = "Period & Time (e.g., 1st • 08:30 AM)"
  override val teacherNameLabel = "Teacher Name"
  override val noTimetableFound = "No Timetable Entries Yet"
  override val timetableSavedMsg = "Timetable entry saved"
  override val reportsSummaryTitle = "School Analytics & Reports"
  override val feeModeBreakdown = "Fee Collection by Payment Mode"
  override val genderBreakdown = "Student Strength by Gender"
  override val attendanceSummaryLabel = "Attendance Overview"
  override val principalNameLabel = "Principal / Admin Name"
  override val contactPhoneLabel = "School Contact Phone"
  override val totalConfiguredClassesLabel = "Total School Classes"
  override val settingsSavedMsg = "School settings saved"
  override val adminProfileTitle = "Admin Profile & Account"
  override val accountUidLabel = "Firebase Account UID"
  override val languageSettingLabel = "App Language"

  override val teacherPortalTitle = "Teacher Portal"
  override val teacherPortalSubtitle = "Class Roster & Fee Status Overview"
  override val studentPortalTitle = "Student & Parent Portal"
  override val studentPortalSubtitle = "Academic Fee Ledger & Receipts"
  override val enterAdmissionLookup = "Search by Admission No. or Name"
  override val noMatchingStudent = "No matching student record found. Ask school admin to enroll your admission record."
  override val feeHistoryTitle = "12-Month Fee Ledger"
  override val retry = "Retry"
  override val errorLoadingData = "Unable to load school records"

  override val months = listOf(
    "April", "May", "June", "July", "August", "September",
    "October", "November", "December", "January", "February", "March"
  )
  override val monthsShort = listOf(
    "Apr", "May", "Jun", "Jul", "Aug", "Sep",
    "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"
  )
  override val classOptions = SchoolClasses
  override val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
}

private object HindiStrings : AppStrings {
  override val schoolName = "श्री राम जियावन पटेल जूनियर हाई स्कूल"
  override val schoolShortName = "एसआरजेपी जूनियर हाई स्कूल"
  override val schoolAddress = "बेचू का पुरवा, ऐरायां, खागा, फतेहपुर, उत्तर प्रदेश"
  override val schoolMotto = "ज्ञान • अनुशासन • उत्कृष्टता"
  override val academicSessionLabel = "शैक्षणिक सत्र 2026-27"
  override val continueBtn = "पोर्टल में प्रवेश करें"
  override val splashLoading = "विद्यालय पोर्टल प्रारंभ हो रहा है..."

  override val portalTitle = "विद्यालय पोर्टल लॉगिन"
  override val portalSubtitle = "सुरक्षित लॉगिन के लिए अपनी भूमिका चुनें"
  override val adminRole = "एडमिन"
  override val teacherRole = "शिक्षक"
  override val studentRole = "छात्र"
  override val adminLoginTitle = "एडमिन / प्रधानाचार्य लॉगिन"
  override val adminLoginDesc = "प्रधानाचार्य एवं कार्यालय प्रबंधन — छात्र रिकॉर्ड, शुल्क संरचना और मासिक शुल्क संग्रह।"
  override val teacherLoginTitle = "शिक्षक लॉगिन"
  override val teacherLoginDesc = "शिक्षक पोर्टल — अपनी कक्षा के छात्र विवरण और मासिक शुल्क स्थिति देखें।"
  override val studentLoginTitle = "छात्र / अभिभावक लॉगिन"
  override val studentLoginDesc = "छात्र पोर्टल — मासिक शुल्क विवरण, बकाया और आधिकारिक रसीद देखें।"
  override val adminNameHint = "प्रशासक का नाम (जैसे, प्रधानाचार्य)"
  override val staffCodeHint = "शिक्षक कोड / स्टाफ आईडी (वैकल्पिक)"
  override val assignedClassHint = "निर्धारित कक्षा"
  override val admissionNoHint = "प्रवेश संख्या / एडमिशन नं. (वैकल्पिक)"
  override val signInWithGoogle = "Google से साइन इन करें"
  override val signingIn = "साइन इन हो रहा है..."
  override val googleSignInNote = "Google साइन-इन और क्लाउड डेटाबेस द्वारा सुरक्षित"
  override val signOut = "साइन आउट"
  override val switchPortal = "भूमिका बदलें"

  override val navHome = "होम"
  override val navDashboard = "होम"
  override val navStudents = "छात्र"
  override val navFees = "शुल्क"
  override val navReports = "रिपोर्ट"
  override val navProfile = "प्रोफ़ाइल"

  override val welcomeAdmin = "प्रशासनिक डेस्क"
  override val totalStudents = "कुल छात्र"
  override val totalTeachers = "कुल शिक्षक"
  override val totalClasses = "कुल कक्षाएं"
  override val todaysAttendance = "आज की उपस्थिति"
  override val totalFeeCollection = "कुल शुल्क संग्रह"
  override val pendingFees = "बकाया शुल्क"
  override val activeStudents = "सक्रिय नामांकन"
  override val monthlyCollection = "मासिक संग्रह"
  override val pendingThisMonth = "इस माह बकाया"
  override val sessionCollection = "सत्र कुल संग्रह"
  override val feesQuickSummary = "शुल्क संग्रह त्वरित सारांश"
  override val todaysCollection = "आज का संग्रह"
  override val thisMonthsCollection = "इस माह का संग्रह"
  override val totalOutstandingFees = "कुल बकाया शुल्क"
  override val quickActions = "त्वरित कार्य"
  override val schoolModulesTitle = "विद्यालय प्रबंधन मॉड्यूल"
  override val recentActivitiesTitle = "हाल की गतिविधियां"
  override val recentlyAddedStudents = "हाल में जोड़े गए छात्र"
  override val recentAttendanceTitle = "हाल की उपस्थिति"
  override val noRecentAttendance = "आज की उपस्थिति दर्ज नहीं है। उपस्थिति दर्ज करने के लिए उपस्थिति खोलें।"
  override val noRecentStudents = "अभी तक कोई छात्र नामांकित नहीं है।"
  override val backToDashboard = "वापस"
  override val notificationsLabel = "सूचनाएं"

  override val studentManagement = "छात्र प्रबंधन"
  override val teacherManagement = "शिक्षक प्रबंधन"
  override val collectMonthlyFees = "मासिक शुल्क जमा"
  override val feesCollectionModule = "शुल्क संग्रह"
  override val attendanceModule = "उपस्थिति"
  override val homeworkModule = "गृहकार्य"
  override val examinationModule = "परीक्षा"
  override val resultsModule = "परिणाम"
  override val noticeBoardModule = "सूचना पट्ट"
  override val timetableModule = "समय सारिणी"
  override val reportsModule = "रिपोर्ट"
  override val schoolSettingsModule = "विद्यालय सेटिंग्स"

  override val addNewStudent = "नया छात्र जोड़ें"
  override val feeStructureSetup = "शुल्क संरचना"
  override val classWiseOverview = "कक्षावार छात्र व शुल्क स्थिति"
  override val recentReceipts = "हाल की शुल्क रसीदें"
  override val noReceiptsYet = "अभी तक कोई शुल्क रसीद दर्ज नहीं है। रसीद देखने के लिए मासिक शुल्क जमा करें।"
  override val noStudentsOverview = "अभी तक कोई छात्र नामांकित नहीं है। कक्षावार विवरण के लिए छात्र जोड़ें।"
  override val viewAll = "सभी देखें"

  override val searchStudentsHint = "नाम, प्रवेश संख्या या मोबाइल खोजें..."
  override val allClasses = "सभी कक्षाएं"
  override val addStudent = "छात्र जोड़ें"
  override val editStudent = "छात्र विवरण सुधारें"
  override val deleteStudent = "छात्र हटाएं"
  override val deleteStudentConfirm = "क्या आप इस छात्र रिकॉर्ड को स्थायी रूप से हटाना चाहते हैं?"
  override val cancel = "रद्द करें"
  override val save = "सहेजें"
  override val delete = "हटाएं"
  override val admissionNo = "प्रवेश संख्या"
  override val rollNumber = "अनुक्रमांक"
  override val fullName = "छात्र का पूरा नाम"
  override val fatherName = "पिता का नाम"
  override val motherName = "माता का नाम"
  override val classLabel = "कक्षा"
  override val section = "वर्ग (Section)"
  override val gender = "लिंग"
  override val male = "छात्र (Male)"
  override val female = "छात्रा (Female)"
  override val other = "अन्य"
  override val phone = "मोबाइल नंबर"
  override val address = "पूरा पता (Full Address)"
  override val dateOfBirth = "जन्म तिथि"
  override val admissionDate = "प्रवेश तिथि"
  override val academicSessionField = "शैक्षणिक सत्र"
  override val previousSchool = "पूर्व विद्यालय का विवरण"
  override val studentPhoto = "छात्र का फोटो"
  override val pickPhoto = "फोटो चुनें"
  override val changePhoto = "फोटो बदलें"
  override val autoGenerateAdmNo = "ऑटो"
  override val viewProfile = "प्रोफ़ाइल"
  override val studentProfileTitle = "छात्र प्रोफ़ाइल"
  override val classWiseStrengthTitle = "कक्षावार छात्र संख्या"
  override val exportPdf = "पीडीएफ"
  override val exportExcel = "एक्सेल"
  override val exportingReport = "फ़ाइल तैयार हो रही है..."
  override val exportReadyTitle = "छात्र सूची साझा करें"
  override val noStudentsToExport = "निर्यात के लिए सूची में कोई छात्र नहीं है"
  override val duplicateAdmissionError = "यह प्रवेश संख्या पहले से दर्ज है। कृपया नई संख्या उपयोग करें।"
  override val invalidMobileError = "कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें"
  override val academicDetailsSection = "शैक्षणिक विवरण"
  override val familyPersonalSection = "पारिवारिक एवं व्यक्तिगत जानकारी"
  override val contactHistorySection = "संपर्क एवं पूर्व विद्यालय"
  override val inactiveStudents = "निष्क्रिय"
  override val toggleStatusLabel = "नामांकन स्थिति"
  override val clear = "साफ़ करें"
  override val monthlyFee = "मासिक शिक्षण शुल्क (₹)"
  override val status = "स्थिति"
  override val active = "सक्रिय"
  override val inactive = "निष्क्रिय"
  override val collectFee = "शुल्क जमा करें"
  override val edit = "संपादित करें"
  override val noStudentsFound = "कोई छात्र रिकॉर्ड नहीं मिला"
  override val noStudentsHint = "विद्यालय के वास्तविक छात्रों को जोड़ने के लिए '+ छात्र जोड़ें' पर टैप करें।"
  override val studentSavedMsg = "छात्र रिकॉर्ड सहेजा गया"
  override val studentDeletedMsg = "छात्र रिकॉर्ड हटाया गया"
  override val validationRequiredField = "कृपया सभी आवश्यक विवरण भरें"

  override val feesModuleTitle = "शुल्क प्रबंधन"
  override val tabFeesDashboard = "डैशबोर्ड"
  override val tabCollectFees = "शुल्क जमा"
  override val tabPendingFees = "बकाया"
  override val tabMonthlyTracker = "मासिक"
  override val tabReceipts = "इतिहास"
  override val tabFeeStructure = "संरचना"
  override val tabFeeReports = "रिपोर्ट"
  override val session = "सत्र"
  override val selectMonth = "माह"
  override val paidCount = "जमा"
  override val dueCount = "बकाया"
  override val collectedTotal = "कुल जमा"
  override val paidBadge = "पूर्ण जमा"
  override val partialBadge = "आंशिक"
  override val unpaidBadge = "अदत्त"
  override val refundedBadge = "रिफंड"
  override val correctedBadge = "संशोधित"
  override val dueBadge = "बकाया"
  override val viewReceipt = "रसीद"
  override val tuitionFee = "मासिक शिक्षण शुल्क (₹)"
  override val admissionFeeLabel = "प्रवेश शुल्क (₹)"
  override val developmentFeeLabel = "विकास शुल्क (₹)"
  override val otherChargesLabel = "अन्य शुल्क (₹)"
  override val otherFee = "परीक्षा / अन्य शुल्क (₹)"
  override val lateFee = "विलंब शुल्क (₹)"
  override val discount = "छूट / रियायत (₹)"
  override val netTotal = "कुल देय राशि"
  override val totalAmountLabel = "कुल राशि"
  override val previousBalanceLabel = "पिछला बकाया"
  override val amountPaidLabel = "जमा की गई राशि (₹)"
  override val remainingBalanceLabel = "शेष बकाया"
  override val fullPaymentLabel = "पूर्ण भुगतान"
  override val partialPaymentLabel = "आंशिक भुगतान"
  override val paymentTypeLabel = "भुगतान विकल्प"
  override val paymentMode = "भुगतान का माध्यम"
  override val modeCash = "नकद (Cash)"
  override val modeUpi = "यूपीआई (UPI)"
  override val modeBank = "बैंक ट्रांसफर"
  override val modeCheque = "चेक"
  override val remarksHint = "टिप्पणी / संदर्भ संख्या (वैकल्पिक)"
  override val confirmCollectFee = "शुल्क जमा करें व रसीद बनाएं"
  override val feeCollectedSuccess = "शुल्क जमा हुआ व रसीद जारी की गई"
  override val receiptTitle = "आधिकारिक शुल्क रसीद"
  override val receiptNoLabel = "रसीद संख्या"
  override val dateLabel = "भुगतान दिनांक"
  override val monthLabel = "शुल्क माह"
  override val collectedByLabel = "संग्रहकर्ता"
  override val authorizedSignatureLabel = "अधिकृत हस्ताक्षर"
  override val downloadPdfLabel = "पीडीएफ डाउनलोड"
  override val printReceiptLabel = "रसीद प्रिंट करें"
  override val shareReceipt = "रसीद साझा करें"
  override val receiptSavedMsg = "रसीद पीडीएफ सहेजी गई"
  override val close = "बंद करें"
  override val refundCorrectionBtn = "रिफंड / सुधार"
  override val refundCorrectionTitle = "एडमिन रिफंड / सुधार"
  override val refundReasonHint = "रिफंड या सुधार का कारण लिखें (आवश्यक)"
  override val markRefundedBtn = "रिफंड दर्ज करें"
  override val markCorrectedBtn = "सुधार दर्ज करें"
  override val refundRecordedMsg = "भुगतान स्थिति अपडेट की गई"
  override val deleteReceipt = "रसीद निरस्त करें"
  override val deleteReceiptConfirm = "क्या आप इस रसीद को रिफंड या संशोधित के रूप में चिह्नित करना चाहते हैं?"
  override val feeStructureTitle = "कक्षावार शुल्क संरचना"
  override val examFee = "परीक्षा शुल्क (₹)"
  override val computerFee = "कंप्यूटर शुल्क (₹)"
  override val transportFee = "वाहन शुल्क (₹)"
  override val saveStructure = "शुल्क संरचना सहेजें"
  override val structureSavedMsg = "कक्षा शुल्क संरचना अपडेट हो गई"
  override val applyToClassStudents = "कक्षा (PG से Class 8) व सत्र के अनुसार मासिक व वार्षिक शुल्क निर्धारित करें"
  override val yearlyTrackerLabel = "12 माह की शुल्क स्थिति (अप्रैल–मार्च)"
  override val totalExpectedFeesLabel = "कुल अपेक्षित शुल्क"
  override val totalFeesCollectedLabel = "कुल जमा शुल्क"
  override val totalPendingFeesLabel = "कुल बकाया शुल्क"
  override val fullyPaidStudentsLabel = "पूर्ण जमा छात्र"
  override val partiallyPaidStudentsLabel = "आंशिक जमा छात्र"
  override val unpaidStudentsLabel = "अदत्त छात्र"
  override val paidMonthsLabel = "जमा माह"
  override val pendingMonthsLabel = "बकाया माह"
  override val selectStudentForFeeHint = "बकाया देखने व शुल्क जमा करने के लिए ऊपर छात्र खोजें और चुनें।"
  override val monthAlreadyPaidWarning = "इस सत्र में चयनित माह का शुल्क पहले से पूर्ण जमा है।"
  override val invalidAmountError = "₹0 से अधिक मान्य भुगतान राशि दर्ज करें"
  override val studentWiseLabel = "छात्रवार"
  override val classWiseLabel = "कक्षावार"
  override val monthWiseLabel = "माहवार"
  override val feeReminderListTitle = "शुल्क अनुस्मारक सूची"
  override val sendReminderLabel = "अनुस्मारक भेजें"
  override val noPendingFeesTitle = "कोई बकाया नहीं"
  override val noPendingFeesDesc = "चयनित श्रेणी में सभी छात्रों का शुल्क जमा है।"
  override val reportDailyCollection = "दैनिक संग्रह"
  override val reportMonthlyCollection = "मासिक संग्रह"
  override val reportAnnualCollection = "वार्षिक संग्रह"
  override val reportClassWiseCollection = "कक्षावार संग्रह"
  override val reportCashCollection = "नकद संग्रह"
  override val reportUpiCollection = "यूपीआई संग्रह"
  override val reportPendingFees = "बकाया शुल्क रिपोर्ट"
  override val reportStudentWiseFee = "छात्रवार शुल्क रिपोर्ट"
  override val selectDateLabel = "दिनांक"
  override val collectionReportTitle = "शुल्क संग्रह रिपोर्ट"

  override val addTeacher = "शिक्षक जोड़ें"
  override val editTeacher = "शिक्षक संपादित करें"
  override val subjectLabel = "विषय"
  override val qualificationLabel = "योग्यता"
  override val noTeachersFound = "अभी कोई शिक्षक नहीं जोड़ा गया"
  override val noTeachersHint = "विद्यालय के शिक्षकों को जोड़ने के लिए '+ शिक्षक जोड़ें' पर टैप करें।"
  override val teacherSavedMsg = "शिक्षक रिकॉर्ड सहेजा गया"
  override val teacherDeletedMsg = "शिक्षक रिकॉर्ड हटाया गया"

  override val attendanceDateLabel = "उपस्थिति दिनांक"
  override val presentLabel = "उपस्थित"
  override val absentLabel = "अनुपस्थित"
  override val leaveLabel = "अवकाश"
  override val markAllPresent = "शेष सभी उपस्थित चिह्नित करें"
  override val attendanceSavedMsg = "उपस्थिति सहेजी गई"
  override val notMarkedYet = "दर्ज नहीं"
  override val tabMarkAttendance = "दैनिक उपस्थिति"
  override val tabDailyReport = "दैनिक रिपोर्ट"
  override val tabMonthlyReport = "मासिक रिपोर्ट"
  override val tabStudentWiseReport = "छात्रवार रिपोर्ट"
  override val allSections = "सभी वर्ग"
  override val pickDateLabel = "दिनांक चुनें"
  override val todayLabel = "आज"
  override val markedCountLabel = "दर्ज"
  override val unmarkedCountLabel = "शेष"
  override val attendancePercentageLabel = "उपस्थिति %"
  override val markedLockedLabel = "दर्ज • सुरक्षित"
  override val duplicateAttendanceBanner = "एक ही छात्र की एक ही तिथि पर दोहरी उपस्थिति स्वतः रोकी जाती है।"
  override val adminEditOnlyHint = "पहले से दर्ज उपस्थिति में केवल एडमिन ही संशोधन कर सकते हैं।"
  override val editAttendanceTitle = "एडमिन उपस्थिति संशोधन"
  override val editAttendanceBtn = "संपादित करें"
  override val editReasonHint = "संशोधन का कारण (वैकल्पिक)"
  override val attendanceUpdatedMsg = "एडमिन द्वारा उपस्थिति अपडेट की गई"
  override val markedByLabel = "दर्जकर्ता"
  override val editedByLabel = "संशोधक"
  override val workingDaysLabel = "कार्य दिवस"
  override val monthlySummaryTitle = "मासिक उपस्थिति सारांश"
  override val dailyBreakdownTitle = "तिथिवार विवरण"
  override val studentHistoryTitle = "छात्र उपस्थिति इतिहास"
  override val noAttendanceRecordsTitle = "कोई उपस्थिति दर्ज नहीं"
  override val noAttendanceRecordsDesc = "रिपोर्ट और प्रतिशत देखने के लिए दैनिक उपस्थिति टैब से उपस्थिति दर्ज करें।"
  override val teacherAttendanceTab = "दैनिक उपस्थिति"
  override val teacherRosterTab = "कक्षा सूची व शुल्क"
  override val attendanceHistoryEmpty = "इस छात्र के लिए अभी तक कोई उपस्थिति दर्ज नहीं है।"
  override val attendanceMarkHint = "उपस्थिति दर्ज करने के लिए उपस्थित, अनुपस्थित या अवकाश चुनें।"

  override val addHomework = "गृहकार्य जोड़ें"
  override val titleLabel = "शीर्षक"
  override val descriptionLabel = "निर्देश / विवरण"
  override val dueDateLabel = "अंतिम तिथि"
  override val noHomeworkFound = "अभी कोई गृहकार्य नहीं है"
  override val homeworkSavedMsg = "गृहकार्य प्रकाशित किया गया"
  override val addExam = "परीक्षा जोड़ें"
  override val examNameLabel = "परीक्षा का नाम (जैसे, अर्धवार्षिक)"
  override val examDateLabel = "परीक्षा तिथि"
  override val maxMarksLabel = "पूर्णांक"
  override val roomNoLabel = "कक्ष संख्या (वैकल्पिक)"
  override val noExamsFound = "कोई परीक्षा निर्धारित नहीं है"
  override val examSavedMsg = "परीक्षा समय-सारिणी सहेजी गई"
  override val addResult = "परिणाम दर्ज करें"
  override val marksObtainedLabel = "प्राप्तांक"
  override val gradeLabel = "ग्रेड"
  override val selectStudentLabel = "छात्र"
  override val selectExamLabel = "परीक्षा"
  override val noResultsFound = "अभी कोई परिणाम दर्ज नहीं है"
  override val resultSavedMsg = "परिणाम सहेजा गया"

  override val addNotice = "सूचना प्रकाशित करें"
  override val noticeBodyLabel = "सूचना विवरण"
  override val audienceLabel = "लक्षित वर्ग"
  override val priorityLabel = "प्राथमिकता"
  override val noNoticesFound = "कोई सूचना नहीं है"
  override val noticeSavedMsg = "सूचना पट्ट पर प्रकाशित की गई"
  override val addTimetable = "पीरियड जोड़ें"
  override val dayOfWeekLabel = "दिन"
  override val periodLabel = "पीरियड व समय"
  override val teacherNameLabel = "शिक्षक का नाम"
  override val noTimetableFound = "कोई समय सारिणी दर्ज नहीं है"
  override val timetableSavedMsg = "समय सारिणी सहेजी गई"
  override val reportsSummaryTitle = "विद्यालय रिपोर्ट व विश्लेषण"
  override val feeModeBreakdown = "भुगतान माध्यम के अनुसार संग्रह"
  override val genderBreakdown = "छात्र-छात्रा संख्या"
  override val attendanceSummaryLabel = "उपस्थिति सारांश"
  override val principalNameLabel = "प्रधानाचार्य / प्रशासक का नाम"
  override val contactPhoneLabel = "विद्यालय संपर्क नंबर"
  override val totalConfiguredClassesLabel = "कुल कक्षाएं"
  override val settingsSavedMsg = "विद्यालय सेटिंग्स सहेजी गईं"
  override val adminProfileTitle = "एडमिन प्रोफ़ाइल व खाता"
  override val accountUidLabel = "Firebase खाता UID"
  override val languageSettingLabel = "ऐप की भाषा"

  override val teacherPortalTitle = "शिक्षक पोर्टल"
  override val teacherPortalSubtitle = "कक्षा छात्र सूची व शुल्क स्थिति"
  override val studentPortalTitle = "छात्र एवं अभिभावक पोर्टल"
  override val studentPortalSubtitle = "मासिक शुल्क विवरण व रसीदें"
  override val enterAdmissionLookup = "प्रवेश संख्या या नाम से खोजें"
  override val noMatchingStudent = "कोई छात्र रिकॉर्ड नहीं मिला। कृपया विद्यालय कार्यालय से नामांकन सुनिश्चित करें।"
  override val feeHistoryTitle = "12 माह का शुल्क विवरण"
  override val retry = "पुनः प्रयास करें"
  override val errorLoadingData = "विद्यालय रिकॉर्ड लोड करने में त्रुटि"

  override val months = listOf(
    "अप्रैल", "मई", "जून", "जुलाई", "अगस्त", "सितंबर",
    "अक्टूबर", "नवंबर", "दिसंबर", "जनवरी", "फरवरी", "मार्च"
  )
  override val monthsShort = listOf(
    "अप्रै", "मई", "जून", "जुला", "अग", "सितं",
    "अक्टू", "नवं", "दिसं", "जन", "फर", "मार्च"
  )
  override val classOptions = SchoolClasses
  override val daysOfWeek = listOf("सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार")
}
