const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_uid_123";
const OTHER_UID = "other_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated access is denied across all school collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ADMIN_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ADMIN_UID).collection("students").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ADMIN_UID).collection("feePayments").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ADMIN_UID).collection("students").doc("s1").set({
      userId: ADMIN_UID,
      admissionNo: "SRJP-01",
      rollNumber: "1",
      fullName: "Aarav Patel",
      fatherName: "Rajesh Patel",
      className: "Class 6",
      section: "A",
      gender: "MALE",
      monthlyFeeAmount: 500,
      status: "ACTIVE",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("UserProfile: owner can create and update portalMode; cross-user and ghost fields fail", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      displayName: "Principal Singh",
      portalMode: "ADMIN",
      preferredLanguage: "EN",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update on allowed fields
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).update({
      portalMode: "TEACHER",
      preferredLanguage: "HI",
      assignedClass: "Class 7",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user read/write denied
  await assertFails(otherDb.collection("users").doc(ADMIN_UID).get());
  await assertFails(
    otherDb.collection("users").doc(ADMIN_UID).update({
      preferredLanguage: "EN",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Ghost field rejected (Shadow Update Test)
  await assertFails(
    adminDb.collection("users").doc(ADMIN_UID).update({
      isSuperAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Students: Kotlin-mirrored create, list, partial update, and validation bounds", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = new Date(Date.now() - 5000);

  // Empty list query succeeds without undefined property error
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("students").get()
  );

  // Valid create (no @DocumentId `id` key) with full 15 admission fields
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("students")
      .doc("stu_01")
      .set({
        userId: ADMIN_UID,
        admissionNo: "SRJP-2026-001",
        rollNumber: "101",
        fullName: "Ananya Verma",
        fatherName: "Suresh Verma",
        motherName: "Sunita Verma",
        dateOfBirth: "14 Aug 2014",
        className: "PG",
        section: "A",
        gender: "FEMALE",
        phone: "9876543210",
        address: "Bechu Ka Purwa, Airayan",
        admissionDate: "01 Apr 2026",
        academicSession: "2026-27",
        previousSchool: "Bal Vidya Mandir",
        photoUrl: "https://firebasestorage.googleapis.com/v0/b/example/o/stu_01.jpg",
        monthlyFeeAmount: 650,
        status: "ACTIVE",
        createdAt: now,
        updatedAt: now,
      })
  );

  // Reject `@DocumentId` id field leakage
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("students")
      .doc("stu_02")
      .set({
        id: "stu_02",
        userId: ADMIN_UID,
        admissionNo: "SRJP-2026-002",
        rollNumber: "102",
        fullName: "Rohan Singh",
        fatherName: "Mahesh Singh",
        className: "Class 7",
        section: "A",
        gender: "MALE",
        monthlyFeeAmount: 700,
        status: "ACTIVE",
        createdAt: now,
        updatedAt: now,
      })
  );

  // Reject future timestamp
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("students")
      .doc("stu_future")
      .set({
        userId: ADMIN_UID,
        admissionNo: "SRJP-2026-003",
        rollNumber: "103",
        fullName: "Future Kid",
        fatherName: "Parent",
        className: "Class 8",
        section: "A",
        gender: "MALE",
        monthlyFeeAmount: 700,
        status: "ACTIVE",
        createdAt: new Date(Date.now() + 60000),
        updatedAt: new Date(Date.now() + 60000),
      })
  );

  // Partial update on status and previousSchool succeeds; immutable createdAt mutation fails
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("students")
      .doc("stu_01")
      .update({
        monthlyFeeAmount: 700,
        status: "INACTIVE",
        previousSchool: "Updated School",
        updatedAt: new Date(Date.now() - 1000),
      })
  );
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("students")
      .doc("stu_01")
      .update({
        createdAt: new Date(Date.now() - 500),
        updatedAt: new Date(Date.now() - 500),
      })
  );

  // Admin permission enforcement: non-ADMIN portalMode cannot create, update, or delete students
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      displayName: "Teacher User",
      portalMode: "TEACHER",
      preferredLanguage: "EN",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("students")
      .doc("stu_03")
      .set({
        userId: ADMIN_UID,
        admissionNo: "SRJP-2026-004",
        rollNumber: "104",
        fullName: "Blocked Student",
        fatherName: "Parent",
        className: "Class 6",
        section: "A",
        gender: "MALE",
        monthlyFeeAmount: 500,
        status: "ACTIVE",
        createdAt: now,
        updatedAt: now,
      })
  );
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("students")
      .doc("stu_01")
      .delete()
  );
  // Reading students in teacher portal still allowed
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("students").get()
  );

  // Cross-user isolation
  await assertFails(
    otherDb.collection("users").doc(ADMIN_UID).collection("students").get()
  );
});

test("FeePayments, FeeStructures, FeeMonthLocks & FeeMeta: Admin-only atomic ledger and non-deletable receipts", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const now = new Date(Date.now() - 5000);

  // Create student first
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("students")
      .doc("stu_01")
      .set({
        userId: ADMIN_UID,
        admissionNo: "SRJP-2026-001",
        rollNumber: "101",
        fullName: "Ananya Verma",
        fatherName: "Suresh Verma",
        className: "Class 6",
        section: "A",
        gender: "FEMALE",
        monthlyFeeAmount: 650,
        status: "ACTIVE",
        createdAt: now,
        updatedAt: now,
      })
  );

  // Fee structure create & update with all fee heads and session
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feeStructures")
      .doc("2026_27_class_6")
      .set({
        userId: ADMIN_UID,
        academicSession: "2026-27",
        className: "Class 6",
        monthlyTuitionFee: 650,
        admissionFee: 500,
        examFee: 100,
        developmentFee: 200,
        otherCharges: 50,
        lateFee: 25,
        discount: 0,
        computerFee: 50,
        transportFee: 0,
        createdAt: now,
        updatedAt: now,
      })
  );

  // Payment for non-existent student must fail (Global Consistency Invariant)
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feePayments")
      .doc("ghost_2026_27_m1")
      .set({
        userId: ADMIN_UID,
        receiptNo: "SRJP-2026-00001",
        studentId: "missing_student",
        studentName: "Ghost",
        admissionNo: "SRJP-999",
        className: "Class 6",
        academicYear: "2026-27",
        monthIndex: 1,
        monthName: "April",
        tuitionFee: 650,
        otherFee: 0,
        lateFee: 0,
        discount: 0,
        totalAmount: 650,
        paymentMode: "CASH",
        paidOnDate: "16 Nov 2026",
        collectedBy: "Principal",
        createdAt: now,
        updatedAt: now,
      })
  );

  // Valid partial fee payment for existing student succeeds
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feePayments")
      .doc("pay_stu_01_2026_27_m1_1")
      .set({
        userId: ADMIN_UID,
        receiptNo: "SRJP-2026-00001",
        studentId: "stu_01",
        studentName: "Ananya Verma",
        fatherName: "Suresh Verma",
        admissionNo: "SRJP-2026-001",
        className: "Class 6",
        academicYear: "2026-27",
        monthIndex: 1,
        monthName: "April",
        tuitionFee: 650,
        otherFee: 50,
        lateFee: 0,
        discount: 0,
        previousBalance: 0,
        totalAmount: 700,
        amountPaid: 400,
        remainingBalance: 300,
        paymentType: "PARTIAL",
        paymentStatus: "PARTIAL",
        paymentMode: "UPI",
        paidOnDate: "16 Nov 2026",
        paidOnIsoDate: "2026-11-16",
        idempotencyKey: "key_1",
        remarks: "UPI Ref 12345",
        collectedBy: "Principal",
        createdAt: now,
        updatedAt: now,
      })
  );

  // FeeMonthLock & FeeMetaCounter transactional docs succeed
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feeMonthLocks")
      .doc("stu_01_2026_27_m1")
      .set({
        userId: ADMIN_UID,
        studentId: "stu_01",
        academicYear: "2026-27",
        monthIndex: 1,
        expectedAmount: 700,
        paidAmount: 400,
        isFullyPaid: false,
        lastReceiptNo: "SRJP-2026-00001",
        lastSubmissionKey: "key_1",
        updatedAt: now,
      })
  );
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feeMeta")
      .doc("receipt_counter")
      .set({
        userId: ADMIN_UID,
        lastSeq: 1,
        updatedAt: now,
      })
  );

  // Payment records can NEVER be deleted (audit safety)
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feePayments")
      .doc("pay_stu_01_2026_27_m1_1")
      .delete()
  );

  // Admin refund/correction update succeeds; tampering receiptNo fails
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feePayments")
      .doc("pay_stu_01_2026_27_m1_1")
      .update({
        paymentStatus: "REFUNDED",
        refundOrCorrectionReason: "Duplicate UPI check",
        updatedAt: new Date(Date.now() - 1000),
      })
  );
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feePayments")
      .doc("pay_stu_01_2026_27_m1_1")
      .update({
        receiptNo: "TAMPERED",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Non-ADMIN portal cannot collect fees, save structures, or issue refunds
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      displayName: "Teacher User",
      portalMode: "TEACHER",
      preferredLanguage: "EN",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feePayments")
      .doc("pay_stu_01_2026_27_m1_1")
      .update({
        paymentStatus: "CORRECTED",
        updatedAt: new Date(Date.now() - 500),
      })
  );
  await assertFails(
    adminDb
      .collection("users")
      .doc(ADMIN_UID)
      .collection("feeStructures")
      .doc("2026_27_class_7")
      .set({
        userId: ADMIN_UID,
        className: "Class 7",
        monthlyTuitionFee: 700,
        examFee: 100,
        computerFee: 0,
        transportFee: 0,
        createdAt: now,
        updatedAt: now,
      })
  );
});

test("Admin modules: teachers, attendance, homework, exams, results, notices, timetable, settings", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = new Date(Date.now() - 5000);

  // Create student for attendance & results FK check
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("students").doc("stu_01").set({
      userId: ADMIN_UID,
      admissionNo: "SRJP-2026-001",
      rollNumber: "1",
      fullName: "Aarav Patel",
      fatherName: "Rajesh Patel",
      className: "Class 6",
      section: "A",
      gender: "MALE",
      monthlyFeeAmount: 500,
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Teacher create & partial update
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("teachers").doc("tch_01").set({
      userId: ADMIN_UID,
      staffCode: "T-101",
      fullName: "Ramesh Tiwari",
      subject: "Mathematics",
      assignedClass: "Class 6",
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("teachers").doc("tch_01").update({
      subject: "Science",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Attendance requires existing student
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_16_stu_01").set({
      userId: ADMIN_UID,
      studentId: "stu_01",
      studentName: "Aarav Patel",
      admissionNo: "SRJP-2026-001",
      className: "Class 6",
      dateKey: "2026-11-16",
      status: "PRESENT",
      markedBy: "Principal",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_16_ghost").set({
      userId: ADMIN_UID,
      studentId: "ghost_id",
      studentName: "Ghost",
      admissionNo: "SRJP-999",
      className: "Class 6",
      dateKey: "2026-11-16",
      status: "PRESENT",
      markedBy: "Principal",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Homework, Exams, Results, Notices, Timetable, Settings
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("homeworks").doc("hw_1").set({
      userId: ADMIN_UID,
      className: "Class 6",
      subject: "Hindi",
      title: "Chapter 3 Questions",
      description: "Write answers 1 to 5 in notebook",
      dueDate: "20 Nov 2026",
      assignedBy: "Principal",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("exams").doc("ex_1").set({
      userId: ADMIN_UID,
      examName: "Half Yearly",
      className: "Class 6",
      subject: "Mathematics",
      examDate: "05 Dec 2026",
      maxMarks: 100,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("results").doc("ex_1_stu_01").set({
      userId: ADMIN_UID,
      examId: "ex_1",
      examName: "Half Yearly",
      studentId: "stu_01",
      studentName: "Aarav Patel",
      admissionNo: "SRJP-2026-001",
      className: "Class 6",
      subject: "Mathematics",
      marksObtained: 88,
      maxMarks: 100,
      grade: "A+",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("notices").doc("nt_1").set({
      userId: ADMIN_UID,
      title: "PTM Announcement",
      body: "Parents Teacher Meeting on Saturday at 10 AM.",
      audience: "ALL",
      priority: "IMPORTANT",
      publishedDate: "16 Nov 2026",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("timetable").doc("tt_1").set({
      userId: ADMIN_UID,
      className: "Class 6",
      dayOfWeek: "Monday",
      periodLabel: "Period 1 (08:30)",
      subject: "Mathematics",
      teacherName: "Ramesh Tiwari",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("schoolConfig").doc("main").set({
      userId: ADMIN_UID,
      schoolName: "Shree Ram Jiyavan Patel Junior High School",
      schoolAddress: "Bechu Ka Purwa, Airayan, Khaga, Fatehpur, Uttar Pradesh",
      academicSession: "2026-27",
      principalName: "Principal",
      totalConfiguredClasses: 11,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user isolation across new collections
  await assertFails(otherDb.collection("users").doc(ADMIN_UID).collection("teachers").get());
  await assertFails(otherDb.collection("users").doc(ADMIN_UID).collection("notices").get());
  await assertFails(otherDb.collection("users").doc(ADMIN_UID).collection("schoolConfig").doc("main").get());
});

test("Attendance: Staff (Admin/Teacher) can create; Student denied; only Admin can edit existing records", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = new Date(Date.now() - 5000);

  // Create two students as Admin
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("students").doc("stu_01").set({
      userId: ADMIN_UID,
      admissionNo: "SRJP-2026-001",
      rollNumber: "1",
      fullName: "Aarav Patel",
      fatherName: "Rajesh Patel",
      className: "Class 6",
      section: "A",
      gender: "MALE",
      monthlyFeeAmount: 500,
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("students").doc("stu_02").set({
      userId: ADMIN_UID,
      admissionNo: "SRJP-2026-002",
      rollNumber: "2",
      fullName: "Diya Verma",
      fatherName: "Suresh Verma",
      className: "Class 6",
      section: "B",
      gender: "FEMALE",
      monthlyFeeAmount: 500,
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin creates attendance with section and academicSession
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_17_stu_01").set({
      userId: ADMIN_UID,
      studentId: "stu_01",
      studentName: "Aarav Patel",
      admissionNo: "SRJP-2026-001",
      className: "Class 6",
      section: "A",
      academicSession: "2026-27",
      dateKey: "2026-11-17",
      status: "PRESENT",
      markedBy: "Principal",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Switch portalMode to TEACHER: can create attendance for unmarked stu_02, CANNOT edit stu_01
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      displayName: "Ramesh Sir",
      portalMode: "TEACHER",
      preferredLanguage: "EN",
      assignedClass: "Class 6",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_17_stu_02").set({
      userId: ADMIN_UID,
      studentId: "stu_02",
      studentName: "Diya Verma",
      admissionNo: "SRJP-2026-002",
      className: "Class 6",
      section: "B",
      academicSession: "2026-27",
      dateKey: "2026-11-17",
      status: "ABSENT",
      markedBy: "Ramesh Sir",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Teacher CANNOT update or delete an already-marked attendance record (Admin-only edit enforcement)
  await assertFails(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_17_stu_01").update({
      status: "LEAVE",
      editedBy: "Ramesh Sir",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_17_stu_02").delete()
  );

  // Switch portalMode to STUDENT: CANNOT create attendance at all
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).update({
      portalMode: "STUDENT",
      updatedAt: new Date(Date.now() - 800),
    })
  );
  await assertFails(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_18_stu_01").set({
      userId: ADMIN_UID,
      studentId: "stu_01",
      studentName: "Aarav Patel",
      admissionNo: "SRJP-2026-001",
      className: "Class 6",
      section: "A",
      dateKey: "2026-11-18",
      status: "PRESENT",
      markedBy: "Student",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Restore ADMIN portalMode: CAN edit existing attendance with editedBy & editReason; cannot mutate dateKey or studentId
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).update({
      portalMode: "ADMIN",
      updatedAt: new Date(Date.now() - 600),
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_17_stu_02").update({
      status: "LEAVE",
      editedBy: "Principal",
      editReason: "Medical leave note submitted",
      updatedAt: new Date(Date.now() - 400),
    })
  );
  await assertFails(
    adminDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").doc("2026_11_17_stu_02").update({
      dateKey: "2026-11-19",
      updatedAt: new Date(Date.now() - 200),
    })
  );
  await assertFails(
    otherDb.collection("users").doc(ADMIN_UID).collection("attendanceRecords").get()
  );
});
