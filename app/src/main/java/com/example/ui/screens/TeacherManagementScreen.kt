package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudentStatus
import com.example.data.model.TeacherRecord
import com.example.data.model.UiState
import com.example.i18n.AppStrings
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue100
import com.example.ui.theme.SkyBlue50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900

@Composable
fun TeacherManagementScreen(
  strings: AppStrings,
  teachersState: UiState<List<TeacherRecord>>,
  isSaving: Boolean,
  onSaveTeacher: (
    existingId: String?,
    staffCode: String,
    fullName: String,
    subject: String,
    assignedClass: String,
    phone: String,
    qualification: String,
    status: StudentStatus,
    onDone: () -> Unit
  ) -> Unit,
  onDeleteTeacher: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var editingTeacher by remember { mutableStateOf<TeacherRecord?>(null) }
  var showDialog by remember { mutableStateOf(false) }
  var pendingDelete by remember { mutableStateOf<TeacherRecord?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(OffWhiteBg)
      .testTag("teacher_management_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Surface(color = PureWhite, shadowElevation = 2.dp) {
        Column(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = strings.teacherManagement,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RoyalBlue900
          )
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(strings.searchStudentsHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = strings.teacherManagement) },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Filled.Close, contentDescription = strings.close)
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("teacher_search_input")
          )
        }
      }

      when (teachersState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoyalBlue800)
          }
        }

        is UiState.Error -> {
          Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            EmptyStateCard(
              icon = Icons.Outlined.School,
              title = strings.errorLoadingData,
              description = teachersState.message
            )
          }
        }

        is UiState.Success -> {
          val filtered = remember(teachersState.data, searchQuery) {
            val q = searchQuery.trim().lowercase()
            teachersState.data.filter { t ->
              q.isEmpty() ||
                t.fullName.lowercase().contains(q) ||
                t.staffCode.lowercase().contains(q) ||
                t.subject.lowercase().contains(q) ||
                t.assignedClass.lowercase().contains(q)
            }
          }

          if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
              EmptyStateCard(
                icon = Icons.Outlined.School,
                title = strings.noTeachersFound,
                description = strings.noTeachersHint,
                actionLabel = strings.addTeacher,
                onAction = {
                  editingTeacher = null
                  showDialog = true
                }
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(filtered, key = { it.id }) { teacher ->
                val isActive = StudentStatus.fromRaw(teacher.status) == StudentStatus.ACTIVE
                Surface(
                  shape = RoundedCornerShape(18.dp),
                  color = PureWhite,
                  border = BorderStroke(1.dp, Slate200),
                  shadowElevation = 1.dp,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Box(
                        modifier = Modifier
                          .size(44.dp)
                          .clip(RoundedCornerShape(12.dp))
                          .background(SkyBlue50),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = teacher.fullName.take(1).uppercase(),
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = RoyalBlue800
                        )
                      }
                      Spacer(modifier = Modifier.width(12.dp))
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = teacher.fullName,
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold,
                          color = Slate900
                        )
                        Text(
                          text = "${teacher.staffCode} • ${teacher.subject}" +
                            if (teacher.assignedClass.isNotBlank()) " • ${teacher.assignedClass}" else "",
                          style = MaterialTheme.typography.bodySmall,
                          color = Slate500
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
                      Text(
                        text = listOfNotNull(
                          teacher.qualification.takeIf { it.isNotBlank() },
                          teacher.phone.takeIf { it.isNotBlank() }
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        modifier = Modifier.weight(1f)
                      )
                      Row {
                        IconButton(onClick = {
                          editingTeacher = teacher
                          showDialog = true
                        }) {
                          Icon(Icons.Filled.Edit, contentDescription = strings.edit, tint = RoyalBlue800)
                        }
                        IconButton(onClick = { pendingDelete = teacher }) {
                          Icon(Icons.Filled.DeleteOutline, contentDescription = strings.delete, tint = RoseDue)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    FloatingActionButton(
      onClick = {
        editingTeacher = null
        showDialog = true
      },
      containerColor = RoyalBlue800,
      contentColor = PureWhite,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(18.dp)
        .testTag("add_teacher_fab")
    ) {
      Icon(Icons.Filled.Add, contentDescription = strings.addTeacher)
    }
  }

  if (showDialog) {
    AddEditTeacherDialog(
      strings = strings,
      existing = editingTeacher,
      isSaving = isSaving,
      onDismiss = { showDialog = false },
      onSave = { code, name, subj, cls, phone, qual, status ->
        onSaveTeacher(editingTeacher?.id, code, name, subj, cls, phone, qual, status) {
          showDialog = false
        }
      }
    )
  }

  pendingDelete?.let { teacher ->
    ConfirmDeleteDialog(
      title = strings.delete,
      message = "${teacher.fullName} (${teacher.staffCode})",
      confirmLabel = strings.delete,
      cancelLabel = strings.cancel,
      onConfirm = {
        onDeleteTeacher(teacher.id)
        pendingDelete = null
      },
      onDismiss = { pendingDelete = null }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTeacherDialog(
  strings: AppStrings,
  existing: TeacherRecord?,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String, String, StudentStatus) -> Unit
) {
  var staffCode by remember { mutableStateOf(existing?.staffCode ?: "") }
  var fullName by remember { mutableStateOf(existing?.fullName ?: "") }
  var subject by remember { mutableStateOf(existing?.subject ?: "") }
  var assignedClass by remember { mutableStateOf(existing?.assignedClass ?: strings.classOptions.first()) }
  var phone by remember { mutableStateOf(existing?.phone ?: "") }
  var qualification by remember { mutableStateOf(existing?.qualification ?: "") }
  var status by remember { mutableStateOf(StudentStatus.fromRaw(existing?.status)) }
  var classExpanded by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = PureWhite,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp)
    ) {
      Column(
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = if (existing == null) strings.addTeacher else strings.editTeacher,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900
        )

        OutlinedTextField(
          value = staffCode,
          onValueChange = { staffCode = it; validationError = null },
          label = { Text(strings.staffCodeHint) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("teacher_staff_code_input")
        )
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it; validationError = null },
          label = { Text(strings.teacherNameLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("teacher_name_input")
        )
        OutlinedTextField(
          value = subject,
          onValueChange = { subject = it; validationError = null },
          label = { Text(strings.subjectLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("teacher_subject_input")
        )

        ExposedDropdownMenuBox(
          expanded = classExpanded,
          onExpandedChange = { classExpanded = !classExpanded }
        ) {
          OutlinedTextField(
            value = assignedClass,
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.assignedClassHint) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
            modifier = Modifier
              .menuAnchor(MenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = classExpanded,
            onDismissRequest = { classExpanded = false }
          ) {
            strings.classOptions.forEach { cls ->
              DropdownMenuItem(
                text = { Text(cls) },
                onClick = {
                  assignedClass = cls
                  classExpanded = false
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = qualification,
          onValueChange = { qualification = it },
          label = { Text(strings.qualificationLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text(strings.phone) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StudentStatus.entries.forEach { st ->
            FilterChip(
              selected = status == st,
              onClick = { status = st },
              label = { Text(if (st == StudentStatus.ACTIVE) strings.active else strings.inactive) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SkyBlue100)
            )
          }
        }

        if (validationError != null) {
          Text(validationError!!, color = RoseDue, style = MaterialTheme.typography.bodySmall)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) { Text(strings.cancel) }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            enabled = !isSaving,
            onClick = {
              if (staffCode.isBlank() || fullName.trim().length < 2 || subject.isBlank()) {
                validationError = strings.validationRequiredField
              } else {
                onSave(staffCode, fullName, subject, assignedClass, phone, qualification, status)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
            modifier = Modifier.testTag("save_teacher_button")
          ) {
            if (isSaving) CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text(strings.save)
          }
        }
      }
    }
  }
}
