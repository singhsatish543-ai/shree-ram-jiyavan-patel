package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExamResult
import com.example.data.model.ExamSchedule
import com.example.data.model.HomeworkItem
import com.example.data.model.StudentRecord
import com.example.data.model.UiState
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun HomeworkScreen(
  strings: AppStrings,
  homeworkState: UiState<List<HomeworkItem>>,
  isSaving: Boolean,
  onAddHomework: (String, String, String, String, String, () -> Unit) -> Unit,
  onDeleteHomework: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedClass by remember { mutableStateOf(strings.allClasses) }
  var showDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize().background(OffWhiteBg).testTag("homework_screen")) {
    Column(modifier = Modifier.fillMaxSize()) {
      Surface(color = PureWhite, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(strings.homeworkModule, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue900)
          Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(strings.allClasses).plus(strings.classOptions).forEach { cls ->
              FilterChip(
                selected = selectedClass == cls,
                onClick = { selectedClass = cls },
                label = { Text(cls) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalBlue800, selectedLabelColor = PureWhite)
              )
            }
          }
        }
      }

      when (homeworkState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RoyalBlue800) }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(Icons.AutoMirrored.Outlined.MenuBook, strings.errorLoadingData, homeworkState.message)
        }
        is UiState.Success -> {
          val itemsList = remember(homeworkState.data, selectedClass) {
            homeworkState.data.filter { selectedClass == strings.allClasses || it.className == selectedClass }
          }
          if (itemsList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
              EmptyStateCard(Icons.AutoMirrored.Outlined.MenuBook, strings.noHomeworkFound, strings.homeworkModule, strings.addHomework, { showDialog = true })
            }
          } else {
            LazyColumn(
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(itemsList, key = { it.id }) { hw ->
                Surface(shape = RoundedCornerShape(16.dp), color = PureWhite, border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth()) {
                  Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                      Surface(shape = RoundedCornerShape(50), color = SkyBlue50) {
                        Text("${hw.className} • ${hw.subject}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RoyalBlue800, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                      }
                      IconButton(onClick = { onDeleteHomework(hw.id) }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = strings.delete, tint = RoseDue)
                      }
                    }
                    Text(hw.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(hw.description, style = MaterialTheme.typography.bodyMedium, color = Slate700)
                    Text("${strings.dueDateLabel}: ${hw.dueDate}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                  }
                }
              }
            }
          }
        }
      }
    }

    FloatingActionButton(
      onClick = { showDialog = true },
      containerColor = RoyalBlue800,
      contentColor = PureWhite,
      modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).testTag("add_homework_fab")
    ) {
      Icon(Icons.Filled.Add, contentDescription = strings.addHomework)
    }
  }

  if (showDialog) {
    AddHomeworkDialog(strings, isSaving, { showDialog = false }) { cls, subj, title, desc, due ->
      onAddHomework(cls, subj, title, desc, due) { showDialog = false }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHomeworkDialog(
  strings: AppStrings,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String) -> Unit
) {
  var className by remember { mutableStateOf(strings.classOptions.first()) }
  var subject by remember { mutableStateOf("") }
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dueDate by remember { mutableStateOf("") }
  var expanded by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = PureWhite, modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(strings.addHomework, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalBlue900)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
          OutlinedTextField(
            value = className, onValueChange = {}, readOnly = true, label = { Text(strings.classLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            strings.classOptions.forEach { cls ->
              DropdownMenuItem(text = { Text(cls) }, onClick = { className = cls; expanded = false })
            }
          }
        }
        OutlinedTextField(value = subject, onValueChange = { subject = it; error = null }, label = { Text(strings.subjectLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = title, onValueChange = { title = it; error = null }, label = { Text(strings.titleLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it; error = null }, label = { Text(strings.descriptionLabel) }, minLines = 3, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dueDate, onValueChange = { dueDate = it; error = null }, label = { Text("${strings.dueDateLabel} (DD MMM YYYY)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (error != null) Text(error!!, color = RoseDue, style = MaterialTheme.typography.bodySmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text(strings.cancel) }
          Spacer(Modifier.width(8.dp))
          Button(
            enabled = !isSaving,
            onClick = {
              if (subject.isBlank() || title.trim().length < 2 || description.isBlank() || dueDate.trim().length < 4) error = strings.validationRequiredField
              else onSave(className, subject, title, description, dueDate)
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
          ) { Text(strings.save) }
        }
      }
    }
  }
}

@Composable
fun ExaminationScreen(
  strings: AppStrings,
  examsState: UiState<List<ExamSchedule>>,
  isSaving: Boolean,
  onAddExam: (String, String, String, String, Long, String, () -> Unit) -> Unit,
  onDeleteExam: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize().background(OffWhiteBg).testTag("examination_screen")) {
    Column(modifier = Modifier.fillMaxSize()) {
      Surface(color = PureWhite, shadowElevation = 2.dp) {
        Text(
          text = strings.examinationModule,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
        )
      }

      when (examsState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RoyalBlue800) }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(Icons.Outlined.EditNote, strings.errorLoadingData, examsState.message)
        }
        is UiState.Success -> {
          if (examsState.data.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
              EmptyStateCard(Icons.Outlined.EditNote, strings.noExamsFound, strings.examinationModule, strings.addExam, { showDialog = true })
            }
          } else {
            LazyColumn(
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(examsState.data, key = { it.id }) { exam ->
                Surface(shape = RoundedCornerShape(16.dp), color = PureWhite, border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth()) {
                  Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      Text(exam.examName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBlue900)
                      Text("${exam.className} • ${exam.subject} • ${exam.examDate}", style = MaterialTheme.typography.bodySmall, color = Slate700)
                      Text("${strings.maxMarksLabel}: ${exam.maxMarks}" + if (exam.roomNo.isNotBlank()) " • ${exam.roomNo}" else "", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    }
                    IconButton(onClick = { onDeleteExam(exam.id) }) {
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

    FloatingActionButton(
      onClick = { showDialog = true },
      containerColor = RoyalBlue800,
      contentColor = PureWhite,
      modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).testTag("add_exam_fab")
    ) {
      Icon(Icons.Filled.Add, contentDescription = strings.addExam)
    }
  }

  if (showDialog) {
    AddExamDialog(strings, isSaving, { showDialog = false }) { name, cls, subj, date, marks, room ->
      onAddExam(name, cls, subj, date, marks, room) { showDialog = false }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExamDialog(
  strings: AppStrings,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, Long, String) -> Unit
) {
  var examName by remember { mutableStateOf("") }
  var className by remember { mutableStateOf(strings.classOptions.first()) }
  var subject by remember { mutableStateOf("") }
  var examDate by remember { mutableStateOf("") }
  var maxMarks by remember { mutableStateOf("100") }
  var roomNo by remember { mutableStateOf("") }
  var expanded by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = PureWhite, modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(strings.addExam, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalBlue900)
        OutlinedTextField(value = examName, onValueChange = { examName = it; error = null }, label = { Text(strings.examNameLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
          OutlinedTextField(
            value = className, onValueChange = {}, readOnly = true, label = { Text(strings.classLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            strings.classOptions.forEach { cls ->
              DropdownMenuItem(text = { Text(cls) }, onClick = { className = cls; expanded = false })
            }
          }
        }
        OutlinedTextField(value = subject, onValueChange = { subject = it; error = null }, label = { Text(strings.subjectLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = examDate, onValueChange = { examDate = it; error = null }, label = { Text(strings.examDateLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = maxMarks, onValueChange = { maxMarks = it.filter(Char::isDigit) }, label = { Text(strings.maxMarksLabel) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = roomNo, onValueChange = { roomNo = it }, label = { Text(strings.roomNoLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (error != null) Text(error!!, color = RoseDue, style = MaterialTheme.typography.bodySmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text(strings.cancel) }
          Spacer(Modifier.width(8.dp))
          Button(
            enabled = !isSaving,
            onClick = {
              val marks = maxMarks.toLongOrNull() ?: 100L
              if (examName.trim().length < 2 || subject.isBlank() || examDate.trim().length < 4) error = strings.validationRequiredField
              else onSave(examName, className, subject, examDate, marks, roomNo)
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
          ) { Text(strings.save) }
        }
      }
    }
  }
}

@Composable
fun ResultsScreen(
  strings: AppStrings,
  examsState: UiState<List<ExamSchedule>>,
  studentsState: UiState<List<StudentRecord>>,
  resultsState: UiState<List<ExamResult>>,
  isSaving: Boolean,
  onSaveResult: (ExamSchedule, StudentRecord, Long, String, () -> Unit) -> Unit,
  onDeleteResult: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showDialog by remember { mutableStateOf(false) }
  val exams = (examsState as? UiState.Success)?.data ?: emptyList()
  val students = (studentsState as? UiState.Success)?.data ?: emptyList()

  Box(modifier = modifier.fillMaxSize().background(OffWhiteBg).testTag("results_screen")) {
    Column(modifier = Modifier.fillMaxSize()) {
      Surface(color = PureWhite, shadowElevation = 2.dp) {
        Text(
          text = strings.resultsModule,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
        )
      }

      when (resultsState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RoyalBlue800) }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(Icons.AutoMirrored.Filled.FactCheck, strings.errorLoadingData, resultsState.message)
        }
        is UiState.Success -> {
          if (resultsState.data.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
              EmptyStateCard(
                icon = Icons.AutoMirrored.Filled.FactCheck,
                title = strings.noResultsFound,
                description = if (exams.isEmpty() || students.isEmpty()) "${strings.examinationModule} & ${strings.studentManagement}" else strings.resultsModule,
                actionLabel = if (exams.isNotEmpty() && students.isNotEmpty()) strings.addResult else null,
                onAction = if (exams.isNotEmpty() && students.isNotEmpty()) ({ showDialog = true }) else null
              )
            }
          } else {
            LazyColumn(
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(resultsState.data, key = { it.id }) { res ->
                Surface(shape = RoundedCornerShape(16.dp), color = PureWhite, border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth()) {
                  Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      Text(res.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
                      Text("${res.examName} • ${res.className} • ${res.subject}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                    Surface(shape = RoundedCornerShape(50), color = EmeraldPaidBg) {
                      Text(
                        text = "${res.marksObtained}/${res.maxMarks} • ${res.grade}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPaid,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                      )
                    }
                    IconButton(onClick = { onDeleteResult(res.id) }) {
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

    if (exams.isNotEmpty() && students.isNotEmpty()) {
      FloatingActionButton(
        onClick = { showDialog = true },
        containerColor = RoyalBlue800,
        contentColor = PureWhite,
        modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).testTag("add_result_fab")
      ) {
        Icon(Icons.Filled.Add, contentDescription = strings.addResult)
      }
    }
  }

  if (showDialog && exams.isNotEmpty() && students.isNotEmpty()) {
    AddResultDialog(strings, exams, students, isSaving, { showDialog = false }) { exam, student, marks, remarks ->
      onSaveResult(exam, student, marks, remarks) { showDialog = false }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddResultDialog(
  strings: AppStrings,
  exams: List<ExamSchedule>,
  students: List<StudentRecord>,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (ExamSchedule, StudentRecord, Long, String) -> Unit
) {
  var selectedExam by remember { mutableStateOf(exams.first()) }
  var selectedStudent by remember { mutableStateOf(students.first()) }
  var marksStr by remember { mutableStateOf("") }
  var remarks by remember { mutableStateOf("") }
  var examExpanded by remember { mutableStateOf(false) }
  var studentExpanded by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = PureWhite, modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(strings.addResult, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalBlue900)
        ExposedDropdownMenuBox(expanded = examExpanded, onExpandedChange = { examExpanded = !examExpanded }) {
          OutlinedTextField(
            value = "${selectedExam.examName} (${selectedExam.className} • ${selectedExam.subject})",
            onValueChange = {}, readOnly = true, label = { Text(strings.selectExamLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(examExpanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = examExpanded, onDismissRequest = { examExpanded = false }) {
            exams.forEach { exam ->
              DropdownMenuItem(text = { Text("${exam.examName} (${exam.className} • ${exam.subject})") }, onClick = { selectedExam = exam; examExpanded = false })
            }
          }
        }
        ExposedDropdownMenuBox(expanded = studentExpanded, onExpandedChange = { studentExpanded = !studentExpanded }) {
          OutlinedTextField(
            value = "${selectedStudent.fullName} (${selectedStudent.className})",
            onValueChange = {}, readOnly = true, label = { Text(strings.selectStudentLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(studentExpanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = studentExpanded, onDismissRequest = { studentExpanded = false }) {
            students.forEach { st ->
              DropdownMenuItem(text = { Text("${st.fullName} (${st.className})") }, onClick = { selectedStudent = st; studentExpanded = false })
            }
          }
        }
        OutlinedTextField(
          value = marksStr,
          onValueChange = { marksStr = it.filter(Char::isDigit); error = null },
          label = { Text("${strings.marksObtainedLabel} (/${selectedExam.maxMarks})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text(strings.remarksHint) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (error != null) Text(error!!, color = RoseDue, style = MaterialTheme.typography.bodySmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text(strings.cancel) }
          Spacer(Modifier.width(8.dp))
          Button(
            enabled = !isSaving,
            onClick = {
              val marks = marksStr.toLongOrNull()
              if (marks == null) error = strings.validationRequiredField
              else onSave(selectedExam, selectedStudent, marks, remarks)
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
          ) { Text(strings.save) }
        }
      }
    }
  }
}
