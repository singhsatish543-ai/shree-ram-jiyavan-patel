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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.NoticeAudience
import com.example.data.model.NoticePriority
import com.example.data.model.SchoolNotice
import com.example.data.model.SchoolSettings
import com.example.data.model.TimetableEntry
import com.example.data.model.UiState
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LanguageTogglePill
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
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
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun NoticeBoardScreen(
  strings: AppStrings,
  noticesState: UiState<List<SchoolNotice>>,
  isSaving: Boolean,
  onAddNotice: (String, String, NoticeAudience, NoticePriority, () -> Unit) -> Unit,
  onDeleteNotice: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize().background(OffWhiteBg).testTag("notice_board_screen")) {
    Column(modifier = Modifier.fillMaxSize()) {
      Surface(color = PureWhite, shadowElevation = 2.dp) {
        Text(
          text = strings.noticeBoardModule,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = RoyalBlue900,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
        )
      }

      when (noticesState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RoyalBlue800) }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(Icons.Outlined.Campaign, strings.errorLoadingData, noticesState.message)
        }
        is UiState.Success -> {
          if (noticesState.data.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
              EmptyStateCard(Icons.Outlined.Campaign, strings.noNoticesFound, strings.noticeBoardModule, strings.addNotice, { showDialog = true })
            }
          } else {
            LazyColumn(
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(noticesState.data, key = { it.id }) { notice ->
                val priority = NoticePriority.fromRaw(notice.priority)
                val (pColor, pBg) = when (priority) {
                  NoticePriority.URGENT -> RoseDue to RoseDueBg
                  NoticePriority.IMPORTANT -> AmberWarning to AmberWarningBg
                  NoticePriority.NORMAL -> RoyalBlue800 to SkyBlue50
                }
                Surface(shape = RoundedCornerShape(16.dp), color = PureWhite, border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth()) {
                  Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(50), color = pBg) {
                          Text(priority.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = pColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Surface(shape = RoundedCornerShape(50), color = SkyBlue50) {
                          Text(notice.audience, style = MaterialTheme.typography.labelSmall, color = RoyalBlue800, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                      }
                      IconButton(onClick = { onDeleteNotice(notice.id) }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = strings.delete, tint = RoseDue)
                      }
                    }
                    Text(notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(notice.body, style = MaterialTheme.typography.bodyMedium, color = Slate700)
                    Text(notice.publishedDate, style = MaterialTheme.typography.labelSmall, color = Slate500)
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
      modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).testTag("add_notice_fab")
    ) {
      Icon(Icons.Filled.Add, contentDescription = strings.addNotice)
    }
  }

  if (showDialog) {
    AddNoticeDialog(strings, isSaving, { showDialog = false }) { title, body, audience, priority ->
      onAddNotice(title, body, audience, priority) { showDialog = false }
    }
  }
}

@Composable
private fun AddNoticeDialog(
  strings: AppStrings,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (String, String, NoticeAudience, NoticePriority) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var body by remember { mutableStateOf("") }
  var audience by remember { mutableStateOf(NoticeAudience.ALL) }
  var priority by remember { mutableStateOf(NoticePriority.NORMAL) }
  var error by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = PureWhite, modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(strings.addNotice, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalBlue900)
        OutlinedTextField(value = title, onValueChange = { title = it; error = null }, label = { Text(strings.titleLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = body, onValueChange = { body = it; error = null }, label = { Text(strings.noticeBodyLabel) }, minLines = 3, modifier = Modifier.fillMaxWidth())
        Text(strings.audienceLabel, style = MaterialTheme.typography.labelMedium, color = Slate700)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          NoticeAudience.entries.forEach { aud ->
            FilterChip(selected = audience == aud, onClick = { audience = aud }, label = { Text(aud.name) })
          }
        }
        Text(strings.priorityLabel, style = MaterialTheme.typography.labelMedium, color = Slate700)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          NoticePriority.entries.forEach { pri ->
            FilterChip(selected = priority == pri, onClick = { priority = pri }, label = { Text(pri.name) })
          }
        }
        if (error != null) Text(error!!, color = RoseDue, style = MaterialTheme.typography.bodySmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text(strings.cancel) }
          Spacer(Modifier.width(8.dp))
          Button(
            enabled = !isSaving,
            onClick = {
              if (title.trim().length < 2 || body.trim().length < 2) error = strings.validationRequiredField
              else onSave(title, body, audience, priority)
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
          ) { Text(strings.save) }
        }
      }
    }
  }
}

@Composable
fun TimetableScreen(
  strings: AppStrings,
  timetableState: UiState<List<TimetableEntry>>,
  isSaving: Boolean,
  onAddTimetableEntry: (String, String, String, String, String, () -> Unit) -> Unit,
  onDeleteTimetableEntry: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedClass by remember { mutableStateOf(strings.allClasses) }
  var showDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize().background(OffWhiteBg).testTag("timetable_screen")) {
    Column(modifier = Modifier.fillMaxSize()) {
      Surface(color = PureWhite, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(strings.timetableModule, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue900)
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

      when (timetableState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RoyalBlue800) }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
          EmptyStateCard(Icons.Outlined.Schedule, strings.errorLoadingData, timetableState.message)
        }
        is UiState.Success -> {
          val entries = remember(timetableState.data, selectedClass) {
            timetableState.data.filter { selectedClass == strings.allClasses || it.className == selectedClass }
          }
          if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
              EmptyStateCard(Icons.Outlined.Schedule, strings.noTimetableFound, strings.timetableModule, strings.addTimetable, { showDialog = true })
            }
          } else {
            LazyColumn(
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(entries, key = { it.id }) { item ->
                Surface(shape = RoundedCornerShape(16.dp), color = PureWhite, border = BorderStroke(1.dp, Slate200), modifier = Modifier.fillMaxWidth()) {
                  Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      Text("${item.className} • ${item.dayOfWeek}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = RoyalBlue800)
                      Text("${item.periodLabel} — ${item.subject}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
                      Text(item.teacherName, style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                    IconButton(onClick = { onDeleteTimetableEntry(item.id) }) {
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
      modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).testTag("add_timetable_fab")
    ) {
      Icon(Icons.Filled.Add, contentDescription = strings.addTimetable)
    }
  }

  if (showDialog) {
    AddTimetableDialog(strings, isSaving, { showDialog = false }) { cls, day, period, subj, teacher ->
      onAddTimetableEntry(cls, day, period, subj, teacher) { showDialog = false }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTimetableDialog(
  strings: AppStrings,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String) -> Unit
) {
  var className by remember { mutableStateOf(strings.classOptions.first()) }
  var dayOfWeek by remember { mutableStateOf(strings.daysOfWeek.first()) }
  var periodLabel by remember { mutableStateOf("") }
  var subject by remember { mutableStateOf("") }
  var teacherName by remember { mutableStateOf("") }
  var clsExp by remember { mutableStateOf(false) }
  var dayExp by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = PureWhite, modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(strings.addTimetable, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalBlue900)
        ExposedDropdownMenuBox(expanded = clsExp, onExpandedChange = { clsExp = !clsExp }) {
          OutlinedTextField(
            value = className, onValueChange = {}, readOnly = true, label = { Text(strings.classLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(clsExp) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = clsExp, onDismissRequest = { clsExp = false }) {
            strings.classOptions.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { className = it; clsExp = false }) }
          }
        }
        ExposedDropdownMenuBox(expanded = dayExp, onExpandedChange = { dayExp = !dayExp }) {
          OutlinedTextField(
            value = dayOfWeek, onValueChange = {}, readOnly = true, label = { Text(strings.dayOfWeekLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dayExp) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = dayExp, onDismissRequest = { dayExp = false }) {
            strings.daysOfWeek.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { dayOfWeek = it; dayExp = false }) }
          }
        }
        OutlinedTextField(value = periodLabel, onValueChange = { periodLabel = it; error = null }, label = { Text(strings.periodLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = subject, onValueChange = { subject = it; error = null }, label = { Text(strings.subjectLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = teacherName, onValueChange = { teacherName = it; error = null }, label = { Text(strings.teacherNameLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (error != null) Text(error!!, color = RoseDue, style = MaterialTheme.typography.bodySmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text(strings.cancel) }
          Spacer(Modifier.width(8.dp))
          Button(
            enabled = !isSaving,
            onClick = {
              if (periodLabel.isBlank() || subject.isBlank() || teacherName.isBlank()) error = strings.validationRequiredField
              else onSave(className, dayOfWeek, periodLabel, subject, teacherName)
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
          ) { Text(strings.save) }
        }
      }
    }
  }
}

@Composable
fun SchoolSettingsScreen(
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  settings: SchoolSettings?,
  isSaving: Boolean,
  onSaveSettings: (String, String, String, String, String, Long) -> Unit,
  modifier: Modifier = Modifier
) {
  var schoolName by remember(settings) { mutableStateOf(settings?.schoolName ?: strings.schoolName) }
  var schoolAddress by remember(settings) { mutableStateOf(settings?.schoolAddress ?: strings.schoolAddress) }
  var academicSession by remember(settings) { mutableStateOf(settings?.academicSession ?: "2026-27") }
  var principalName by remember(settings) { mutableStateOf(settings?.principalName ?: "Principal") }
  var contactPhone by remember(settings) { mutableStateOf(settings?.contactPhone ?: "") }
  var totalClasses by remember(settings) { mutableStateOf((settings?.totalConfiguredClasses ?: 11L).toString()) }

  LaunchedEffect(settings) {
    if (settings != null) {
      schoolName = settings.schoolName
      schoolAddress = settings.schoolAddress
      academicSession = settings.academicSession
      principalName = settings.principalName
      contactPhone = settings.contactPhone
      totalClasses = settings.totalConfiguredClasses.toString()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OffWhiteBg)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("school_settings_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Surface(shape = RoundedCornerShape(20.dp), color = PureWhite, border = BorderStroke(1.dp, SkyBlue100), modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(strings.schoolSettingsModule, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBlue900)
        OutlinedTextField(value = schoolName, onValueChange = { schoolName = it }, label = { Text(strings.schoolName) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = schoolAddress, onValueChange = { schoolAddress = it }, label = { Text(strings.address) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = academicSession, onValueChange = { academicSession = it }, label = { Text(strings.session) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = principalName, onValueChange = { principalName = it }, label = { Text(strings.principalNameLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text(strings.contactPhoneLabel) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = totalClasses, onValueChange = { totalClasses = it.filter(Char::isDigit) }, label = { Text(strings.totalConfiguredClassesLabel) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text(strings.languageSettingLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Slate700)
          LanguageTogglePill(language = language, onLanguageChange = onLanguageChange, onDarkBackground = false)
        }

        Button(
          enabled = !isSaving,
          onClick = {
            onSaveSettings(
              schoolName,
              schoolAddress,
              academicSession,
              principalName,
              contactPhone,
              totalClasses.toLongOrNull() ?: 11L
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
          modifier = Modifier.fillMaxWidth().testTag("save_school_settings_button")
        ) {
          Text(strings.save)
        }
      }
    }
  }
}
