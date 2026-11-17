package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FeeStructure
import com.example.data.model.Gender
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.data.model.UiState
import com.example.i18n.AppStrings
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.formatInr
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.RoyalBlue700
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue100
import com.example.ui.theme.SkyBlue50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.StudentExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StudentManagementScreen(
  strings: AppStrings,
  studentsState: UiState<List<StudentRecord>>,
  feeStructures: List<FeeStructure>,
  activeSession: String = "2026-27",
  isSaving: Boolean,
  initialOpenAddDialog: Boolean,
  onConsumedInitialAddDialog: () -> Unit,
  onSaveStudent: (
    existingId: String?,
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
    photoUrl: String,
    dateOfBirth: String,
    admissionDate: String,
    academicSession: String,
    previousSchool: String,
    newPhotoJpegBytes: ByteArray?,
    onDone: () -> Unit
  ) -> Unit,
  onToggleStudentStatus: (studentId: String, newStatus: StudentStatus) -> Unit = { _, _ -> },
  onDeleteStudent: (studentId: String) -> Unit,
  onCollectFeeForStudent: (StudentRecord) -> Unit,
  onShowMessage: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var searchQuery by remember { mutableStateOf("") }
  var selectedClassFilter by remember { mutableStateOf<String?>(null) }
  var editingStudent by remember { mutableStateOf<StudentRecord?>(null) }
  var viewingStudentId by remember { mutableStateOf<String?>(null) }
  var showAddEditDialog by remember { mutableStateOf(false) }
  var studentPendingDelete by remember { mutableStateOf<StudentRecord?>(null) }
  var isExporting by remember { mutableStateOf(false) }

  LaunchedEffect(initialOpenAddDialog) {
    if (initialOpenAddDialog) {
      editingStudent = null
      showAddEditDialog = true
      onConsumedInitialAddDialog()
    }
  }

  val allStudents = (studentsState as? UiState.Success)?.data ?: emptyList()
  val classCounts = remember(allStudents) {
    allStudents.groupingBy { it.className }.eachCount()
  }
  val filteredStudents = remember(allStudents, searchQuery, selectedClassFilter) {
    val q = searchQuery.trim().lowercase()
    allStudents.filter { s ->
      val matchesClass = selectedClassFilter == null || s.className == selectedClassFilter
      val matchesQuery = q.isEmpty() ||
        s.fullName.lowercase().contains(q) ||
        s.admissionNo.lowercase().contains(q) ||
        s.fatherName.lowercase().contains(q) ||
        s.phone.lowercase().contains(q)
      matchesClass && matchesQuery
    }
  }

  val viewingStudent = remember(allStudents, viewingStudentId) {
    viewingStudentId?.let { id -> allStudents.firstOrNull { it.id == id } }
  }

  fun handleExport(isPdf: Boolean) {
    if (filteredStudents.isEmpty()) {
      onShowMessage(strings.noStudentsToExport)
      return
    }
    if (isExporting) return
    isExporting = true
    scope.launch {
      try {
        val file = withContext(Dispatchers.IO) {
          if (isPdf) {
            StudentExportUtils.exportStudentsToPdf(
              context = context,
              students = filteredStudents,
              strings = strings,
              classFilter = selectedClassFilter,
              academicSession = activeSession
            )
          } else {
            StudentExportUtils.exportStudentsToExcel(
              context = context,
              students = filteredStudents,
              strings = strings,
              classFilter = selectedClassFilter,
              academicSession = activeSession
            )
          }
        }
        val mime = if (isPdf) {
          "application/pdf"
        } else {
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        StudentExportUtils.shareExportedFile(context, file, mime, strings.exportReadyTitle)
      } catch (e: Exception) {
        onShowMessage(e.localizedMessage ?: "Export failed")
      } finally {
        isExporting = false
      }
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = OffWhiteBg,
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          editingStudent = null
          showAddEditDialog = true
        },
        containerColor = RoyalBlue800,
        contentColor = PureWhite,
        icon = { Icon(Icons.Filled.Add, contentDescription = strings.addStudent) },
        text = { Text(strings.addStudent, fontWeight = FontWeight.Bold) },
        modifier = Modifier.testTag("add_student_fab")
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Search, Dashboard Summary & Class-wise Filter Strip
      Surface(
        color = PureWhite,
        shadowElevation = 2.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Student Management Summary & Export Row
          val totalCount = allStudents.size
          val activeCount = allStudents.count { StudentStatus.fromRaw(it.status) == StudentStatus.ACTIVE }
          val inactiveCount = totalCount - activeCount

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = SkyBlue50,
                modifier = Modifier.testTag("student_mgmt_total_count")
              ) {
                Text(
                  text = "${strings.totalStudents}: $totalCount",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue900,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = EmeraldPaidBg
              ) {
                Text(
                  text = "$activeCount ${strings.active}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPaid,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
              if (inactiveCount > 0) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = RoseDueBg
                ) {
                  Text(
                    text = "$inactiveCount ${strings.inactiveStudents}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = RoseDue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                  )
                }
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedButton(
                onClick = { handleExport(isPdf = true) },
                enabled = !isExporting,
                border = BorderStroke(1.dp, SkyBlue100),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("export_pdf_button")
              ) {
                Icon(
                  imageVector = Icons.Filled.PictureAsPdf,
                  contentDescription = strings.exportPdf,
                  tint = RoseDue,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.exportPdf, color = RoyalBlue900, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = { handleExport(isPdf = false) },
                enabled = !isExporting,
                border = BorderStroke(1.dp, SkyBlue100),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("export_excel_button")
              ) {
                Icon(
                  imageVector = Icons.Filled.TableChart,
                  contentDescription = strings.exportExcel,
                  tint = EmeraldPaid,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.exportExcel, color = RoyalBlue900, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              }
            }
          }

          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(strings.searchStudentsHint) },
            leadingIcon = {
              Icon(Icons.Filled.Search, contentDescription = strings.searchStudentsHint, tint = RoyalBlue700)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Filled.Close, contentDescription = strings.cancel)
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("student_search_input")
          )

          // Class-wise Student Count & Filter Strip (PG to Class 8)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = selectedClassFilter == null,
              onClick = { selectedClassFilter = null },
              label = { Text("${strings.allClasses} ($totalCount)") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalBlue800,
                selectedLabelColor = PureWhite
              ),
              modifier = Modifier.testTag("class_filter_all")
            )
            strings.classOptions.forEach { cls ->
              val selected = selectedClassFilter == cls
              val clsCount = classCounts[cls] ?: 0
              FilterChip(
                selected = selected,
                onClick = { selectedClassFilter = if (selected) null else cls },
                label = { Text("$cls ($clsCount)") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = RoyalBlue800,
                  selectedLabelColor = PureWhite
                ),
                modifier = Modifier.testTag("class_filter_${cls.lowercase().replace(" ", "_")}")
              )
            }
          }
        }
      }

      when (studentsState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoyalBlue800)
          }
        }

        is UiState.Error -> {
          Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            EmptyStateCard(
              icon = Icons.Outlined.PersonAdd,
              title = strings.errorLoadingData,
              description = studentsState.message
            )
          }
        }

        is UiState.Success -> {
          if (filteredStudents.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
              contentAlignment = Alignment.Center
            ) {
              EmptyStateCard(
                icon = Icons.Outlined.PersonAdd,
                title = strings.noStudentsFound,
                description = strings.noStudentsHint,
                actionLabel = strings.addStudent,
                onAction = {
                  editingStudent = null
                  showAddEditDialog = true
                }
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .testTag("student_list"),
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 92.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(filteredStudents, key = { it.id }) { student ->
                StudentRecordCard(
                  student = student,
                  strings = strings,
                  onViewProfile = { viewingStudentId = student.id },
                  onCollectFee = { onCollectFeeForStudent(student) },
                  onEdit = {
                    editingStudent = student
                    showAddEditDialog = true
                  },
                  onDelete = { studentPendingDelete = student }
                )
              }
            }
          }
        }
      }
    }
  }

  viewingStudent?.let { target ->
    StudentProfileDialog(
      student = target,
      strings = strings,
      isSaving = isSaving,
      onToggleStatus = { newStatus ->
        onToggleStudentStatus(target.id, newStatus)
      },
      onEdit = {
        viewingStudentId = null
        editingStudent = target
        showAddEditDialog = true
      },
      onCollectFee = {
        viewingStudentId = null
        onCollectFeeForStudent(target)
      },
      onDismiss = { viewingStudentId = null }
    )
  }

  if (showAddEditDialog) {
    AddEditStudentDialog(
      existingStudent = editingStudent,
      allStudents = allStudents,
      feeStructures = feeStructures,
      activeSession = activeSession,
      strings = strings,
      isSaving = isSaving,
      onDismiss = { showAddEditDialog = false },
      onSave = { adm, roll, name, father, mother, cls, sec, gender, phone, addr, fee, status, photoUrl, dob, admDate, session, prevSchool, photoBytes ->
        onSaveStudent(
          editingStudent?.id,
          adm, roll, name, father, mother, cls, sec, gender, phone, addr, fee, status,
          photoUrl, dob, admDate, session, prevSchool, photoBytes
        ) {
          showAddEditDialog = false
        }
      }
    )
  }

  studentPendingDelete?.let { target ->
    ConfirmDeleteDialog(
      title = strings.deleteStudent,
      message = "${target.fullName} (${target.admissionNo})\n${strings.deleteStudentConfirm}",
      confirmLabel = strings.delete,
      cancelLabel = strings.cancel,
      onConfirm = {
        onDeleteStudent(target.id)
        studentPendingDelete = null
      },
      onDismiss = { studentPendingDelete = null }
    )
  }
}

@Composable
private fun StudentRecordCard(
  student: StudentRecord,
  strings: AppStrings,
  onViewProfile: () -> Unit,
  onCollectFee: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val isActive = StudentStatus.fromRaw(student.status) == StudentStatus.ACTIVE

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = PureWhite,
    border = BorderStroke(1.dp, Slate200),
    shadowElevation = 2.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onViewProfile)
      .testTag("student_card_${student.id}")
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        StudentAvatar(
          photoUrl = student.photoUrl,
          fullName = student.fullName,
          sizeDp = 48
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = student.fullName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${student.className} (${student.section}) • ${strings.rollNumber}: ${student.rollNumber} • ${student.admissionNo}",
            style = MaterialTheme.typography.bodySmall,
            color = RoyalBlue700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Surface(
          shape = RoundedCornerShape(50),
          color = if (isActive) EmeraldPaidBg else RoseDueBg
        ) {
          Text(
            text = if (isActive) strings.active else strings.inactive,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isActive) EmeraldPaid else RoseDue,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
          Text(
            text = "${strings.fatherName}: ${student.fatherName}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate700
          )
          if (student.dateOfBirth.isNotBlank()) {
            Text(
              text = "${strings.dateOfBirth}: ${student.dateOfBirth}",
              style = MaterialTheme.typography.bodySmall,
              color = Slate500
            )
          }
          if (student.phone.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = strings.phone,
                tint = Slate500,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = student.phone,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
          }
          if (student.address.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = strings.address,
                tint = Slate500,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = student.address,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SkyBlue50
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = formatInr(student.monthlyFeeAmount),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = RoyalBlue900
            )
            Text(
              text = "/ ${strings.selectMonth}",
              style = MaterialTheme.typography.labelSmall,
              color = Slate500
            )
          }
        }
      }

      HorizontalDivider(color = Slate200)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onViewProfile,
          border = BorderStroke(1.dp, SkyBlue100),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("student_view_${student.id}")
        ) {
          Icon(Icons.Filled.Person, contentDescription = strings.viewProfile, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(strings.viewProfile, color = RoyalBlue800, style = MaterialTheme.typography.labelMedium)
        }

        Button(
          onClick = onCollectFee,
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("student_collect_fee_${student.id}")
        ) {
          Icon(Icons.Filled.CurrencyRupee, contentDescription = strings.collectFee, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(strings.collectFee, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }

        IconButton(
          onClick = onEdit,
          modifier = Modifier.testTag("student_edit_${student.id}")
        ) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = strings.edit,
            tint = RoyalBlue800
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("student_delete_${student.id}")
        ) {
          Icon(
            imageVector = Icons.Filled.DeleteOutline,
            contentDescription = strings.delete,
            tint = RoseDue
          )
        }
      }
    }
  }
}
