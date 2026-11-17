package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.FeeStructure
import com.example.data.model.Gender
import com.example.data.model.StudentRecord
import com.example.data.model.StudentStatus
import com.example.i18n.AppStrings
import com.example.ui.components.formatInr
import com.example.ui.theme.AzureBlue600
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun StudentAvatar(
  photoUrl: String,
  fullName: String,
  sizeDp: Int = 48,
  pickedUri: Uri? = null
) {
  val initials = remember(fullName) {
    fullName.split(" ")
      .filter { it.isNotBlank() }
      .take(2)
      .joinToString("") { it.first().uppercase() }
      .ifBlank { "S" }
  }
  val model: Any? = pickedUri ?: photoUrl.takeIf { it.isNotBlank() }

  Box(
    modifier = Modifier
      .size(sizeDp.dp)
      .clip(CircleShape)
      .background(RoyalBlue800)
      .border(1.5.dp, SkyBlue100, CircleShape),
    contentAlignment = Alignment.Center
  ) {
    if (model != null) {
      AsyncImage(
        model = model,
        contentDescription = fullName,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(sizeDp.dp)
          .clip(CircleShape)
      )
    } else {
      Text(
        text = initials,
        style = if (sizeDp >= 64) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = PureWhite
      )
    }
  }
}

@Composable
internal fun StudentProfileDialog(
  student: StudentRecord,
  strings: AppStrings,
  isSaving: Boolean,
  onToggleStatus: (StudentStatus) -> Unit,
  onEdit: () -> Unit,
  onCollectFee: () -> Unit,
  onDismiss: () -> Unit
) {
  val isActive = StudentStatus.fromRaw(student.status) == StudentStatus.ACTIVE
  val genderLabel = when (Gender.fromRaw(student.gender)) {
    Gender.MALE -> strings.male
    Gender.FEMALE -> strings.female
    Gender.OTHER -> strings.other
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = PureWhite,
      shadowElevation = 10.dp,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 560.dp)
        .testTag("student_profile_dialog")
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Blue Gradient Header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(RoyalBlue900, AzureBlue600)))
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            StudentAvatar(
              photoUrl = student.photoUrl,
              fullName = student.fullName,
              sizeDp = 64
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = student.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PureWhite
              )
              Text(
                text = "${strings.admissionNo}: ${student.admissionNo}",
                style = MaterialTheme.typography.bodySmall,
                color = PureWhite.copy(alpha = 0.9f)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                  shape = RoundedCornerShape(50),
                  color = PureWhite.copy(alpha = 0.2f)
                ) {
                  Text(
                    text = "${student.className} (${student.section}) • ${strings.rollNumber}: ${student.rollNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }
              }
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.testTag("profile_close_icon")
            ) {
              Icon(Icons.Filled.Close, contentDescription = strings.close, tint = PureWhite)
            }
          }
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Status Quick Switch
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isActive) EmeraldPaidBg else RoseDueBg,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = strings.toggleStatusLabel,
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate700
                )
                Text(
                  text = if (isActive) strings.active else strings.inactive,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isActive) EmeraldPaid else RoseDue
                )
              }
              Switch(
                checked = isActive,
                enabled = !isSaving,
                onCheckedChange = { checked ->
                  onToggleStatus(if (checked) StudentStatus.ACTIVE else StudentStatus.INACTIVE)
                },
                modifier = Modifier.testTag("profile_status_switch")
              )
            }
          }

          ProfileSectionTitle(strings.academicDetailsSection)
          ProfileInfoRow(strings.admissionNo, student.admissionNo)
          ProfileInfoRow(strings.classLabel, "${student.className} • ${strings.section} ${student.section}")
          ProfileInfoRow(strings.rollNumber, student.rollNumber)
          ProfileInfoRow(strings.academicSessionField, student.academicSession.ifBlank { "2026-27" })
          ProfileInfoRow(strings.admissionDate, student.admissionDate.ifBlank { "-" })
          ProfileInfoRow(strings.monthlyFee, formatInr(student.monthlyFeeAmount))

          HorizontalDivider(color = Slate200)

          ProfileSectionTitle(strings.familyPersonalSection)
          ProfileInfoRow(strings.fatherName, student.fatherName)
          ProfileInfoRow(strings.motherName, student.motherName.ifBlank { "-" })
          ProfileInfoRow(strings.dateOfBirth, student.dateOfBirth.ifBlank { "-" })
          ProfileInfoRow(strings.gender, genderLabel)

          HorizontalDivider(color = Slate200)

          ProfileSectionTitle(strings.contactHistorySection)
          ProfileInfoRow(strings.phone, student.phone.ifBlank { "-" })
          ProfileInfoRow(strings.address, student.address.ifBlank { "-" })
          ProfileInfoRow(strings.previousSchool, student.previousSchool.ifBlank { "-" })
        }

        HorizontalDivider(color = Slate200)

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onEdit,
            border = BorderStroke(1.dp, SkyBlue100),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("profile_edit_button")
          ) {
            Icon(Icons.Filled.Edit, contentDescription = strings.edit, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.edit, color = RoyalBlue800, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = onCollectFee,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("profile_collect_fee_button")
          ) {
            Icon(Icons.Filled.CurrencyRupee, contentDescription = strings.collectFee, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.collectFee, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileSectionTitle(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelLarge,
    fontWeight = FontWeight.Bold,
    color = RoyalBlue800
  )
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = Slate500,
      modifier = Modifier.weight(0.9f)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = Slate900,
      modifier = Modifier.weight(1.1f)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditStudentDialog(
  existingStudent: StudentRecord?,
  allStudents: List<StudentRecord>,
  feeStructures: List<FeeStructure>,
  activeSession: String,
  strings: AppStrings,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (
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
    newPhotoJpegBytes: ByteArray?
  ) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val todayFormatted = remember {
    SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
  }
  val defaultClass = existingStudent?.className?.ifBlank { "Class 6" } ?: "Class 6"
  val feeForClass = feeStructures.firstOrNull { it.className == defaultClass }?.monthlyTuitionFee

  var admissionNo by remember {
    mutableStateOf(
      existingStudent?.admissionNo
        ?: StudentExportUtils.generateUniqueAdmissionNo(allStudents, activeSession)
    )
  }
  var rollNumber by remember { mutableStateOf(existingStudent?.rollNumber ?: "") }
  var fullName by remember { mutableStateOf(existingStudent?.fullName ?: "") }
  var fatherName by remember { mutableStateOf(existingStudent?.fatherName ?: "") }
  var motherName by remember { mutableStateOf(existingStudent?.motherName ?: "") }
  var dateOfBirth by remember { mutableStateOf(existingStudent?.dateOfBirth ?: "") }
  var className by remember { mutableStateOf(defaultClass) }
  var section by remember { mutableStateOf(existingStudent?.section?.ifBlank { "A" } ?: "A") }
  var gender by remember { mutableStateOf(Gender.fromRaw(existingStudent?.gender)) }
  var phone by remember { mutableStateOf(existingStudent?.phone ?: "") }
  var address by remember { mutableStateOf(existingStudent?.address ?: "") }
  var admissionDate by remember {
    mutableStateOf(existingStudent?.admissionDate?.ifBlank { todayFormatted } ?: todayFormatted)
  }
  var academicSession by remember {
    mutableStateOf(existingStudent?.academicSession?.ifBlank { activeSession } ?: activeSession)
  }
  var previousSchool by remember { mutableStateOf(existingStudent?.previousSchool ?: "") }
  var photoUrl by remember { mutableStateOf(existingStudent?.photoUrl ?: "") }
  var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }

  var monthlyFeeText by remember {
    mutableStateOf(
      existingStudent?.monthlyFeeAmount?.toString()
        ?: feeForClass?.toString()
        ?: "500"
    )
  }
  var isActive by remember {
    mutableStateOf(StudentStatus.fromRaw(existingStudent?.status) == StudentStatus.ACTIVE)
  }
  var classExpanded by remember { mutableStateOf(false) }
  var activeDatePickerTarget by remember { mutableStateOf<String?>(null) }
  var isCompressingPhoto by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      pickedPhotoUri = uri
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = PureWhite,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 580.dp)
        .testTag("add_edit_student_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (existingStudent == null) strings.addStudent else strings.editStudent,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = RoyalBlue900
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = strings.cancel)
          }
        }

        // Student Photo Picker Row
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = SkyBlue50,
          border = BorderStroke(1.dp, SkyBlue100),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            if (pickedPhotoUri != null || photoUrl.isNotBlank()) {
              StudentAvatar(
                photoUrl = photoUrl,
                fullName = fullName.ifBlank { "Student" },
                sizeDp = 56,
                pickedUri = pickedPhotoUri
              )
            } else {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(PureWhite)
                  .border(1.dp, SkyBlue100, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.Person,
                  contentDescription = strings.studentPhoto,
                  tint = RoyalBlue700,
                  modifier = Modifier.size(28.dp)
                )
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.studentPhoto,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue900
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                  onClick = {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  },
                  border = BorderStroke(1.dp, RoyalBlue700),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("pick_student_photo_button")
                ) {
                  Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = strings.pickPhoto,
                    tint = RoyalBlue800,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (pickedPhotoUri == null && photoUrl.isBlank()) strings.pickPhoto else strings.changePhoto,
                    color = RoyalBlue800,
                    style = MaterialTheme.typography.labelMedium
                  )
                }

                if (pickedPhotoUri != null || photoUrl.isNotBlank()) {
                  TextButton(
                    onClick = {
                      pickedPhotoUri = null
                      photoUrl = ""
                    }
                  ) {
                    Text(strings.clear, color = RoseDue, style = MaterialTheme.typography.labelMedium)
                  }
                }
              }
            }
          }
        }

        // Admission No (Auto-generated + Manual override) & Roll No
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = admissionNo,
            onValueChange = { admissionNo = it },
            label = { Text("${strings.admissionNo} *") },
            trailingIcon = {
              TextButton(
                onClick = {
                  admissionNo = StudentExportUtils.generateUniqueAdmissionNo(allStudents, academicSession)
                },
                contentPadding = PaddingValues(horizontal = 6.dp),
                modifier = Modifier.testTag("auto_admission_button")
              ) {
                Icon(
                  imageVector = Icons.Filled.AutoAwesome,
                  contentDescription = strings.autoGenerateAdmNo,
                  tint = RoyalBlue700,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(strings.autoGenerateAdmNo, style = MaterialTheme.typography.labelSmall, color = RoyalBlue800)
              }
            },
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .weight(1.35f)
              .testTag("student_admission_input")
          )
          OutlinedTextField(
            value = rollNumber,
            onValueChange = { rollNumber = it },
            label = { Text("${strings.rollNumber} *") },
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .weight(0.65f)
              .testTag("student_roll_input")
          )
        }

        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("${strings.fullName} *") },
          singleLine = true,
          colors = schoolTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("student_name_input")
        )

        OutlinedTextField(
          value = fatherName,
          onValueChange = { fatherName = it },
          label = { Text("${strings.fatherName} *") },
          singleLine = true,
          colors = schoolTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("student_father_input")
        )

        OutlinedTextField(
          value = motherName,
          onValueChange = { motherName = it },
          label = { Text(strings.motherName) },
          singleLine = true,
          colors = schoolTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("student_mother_input")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { dateOfBirth = it },
            label = { Text(strings.dateOfBirth) },
            placeholder = { Text("15 Aug 2014") },
            trailingIcon = {
              IconButton(
                onClick = { activeDatePickerTarget = "DOB" },
                modifier = Modifier.testTag("dob_picker_button")
              ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = strings.dateOfBirth, tint = RoyalBlue700)
              }
            },
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .weight(1f)
              .testTag("student_dob_input")
          )

          OutlinedTextField(
            value = admissionDate,
            onValueChange = { admissionDate = it },
            label = { Text(strings.admissionDate) },
            trailingIcon = {
              IconButton(
                onClick = { activeDatePickerTarget = "ADM" },
                modifier = Modifier.testTag("admission_date_picker_button")
              ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = strings.admissionDate, tint = RoyalBlue700)
              }
            },
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .weight(1f)
              .testTag("student_admission_date_input")
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          ExposedDropdownMenuBox(
            expanded = classExpanded,
            onExpandedChange = { classExpanded = it },
            modifier = Modifier.weight(1.3f)
          ) {
            OutlinedTextField(
              value = className,
              onValueChange = {},
              readOnly = true,
              label = { Text("${strings.classLabel} *") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
              colors = schoolTextFieldColors(),
              modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .testTag("student_class_dropdown")
            )
            ExposedDropdownMenu(
              expanded = classExpanded,
              onDismissRequest = { classExpanded = false }
            ) {
              strings.classOptions.forEach { cls ->
                DropdownMenuItem(
                  text = { Text(cls) },
                  onClick = {
                    className = cls
                    classExpanded = false
                    if (existingStudent == null) {
                      feeStructures.firstOrNull { it.className == cls }?.let {
                        monthlyFeeText = it.monthlyTuitionFee.toString()
                      }
                    }
                  }
                )
              }
            }
          }

          OutlinedTextField(
            value = section,
            onValueChange = { section = it.take(4) },
            label = { Text(strings.section) },
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .weight(0.7f)
              .testTag("student_section_input")
          )
        }

        // Gender Chips
        Text(text = strings.gender, style = MaterialTheme.typography.labelMedium, color = Slate700)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(
            Gender.MALE to strings.male,
            Gender.FEMALE to strings.female,
            Gender.OTHER to strings.other
          ).forEach { (g, label) ->
            FilterChip(
              selected = gender == g,
              onClick = { gender = g },
              label = { Text(label) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalBlue800,
                selectedLabelColor = PureWhite
              ),
              modifier = Modifier.testTag("student_gender_${g.name.lowercase()}")
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { ch -> ch.isDigit() || ch == '+' }.take(15) },
            label = { Text("${strings.phone} *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .weight(1f)
              .testTag("student_phone_input")
          )
          OutlinedTextField(
            value = academicSession,
            onValueChange = { academicSession = it.take(15) },
            label = { Text(strings.academicSessionField) },
            singleLine = true,
            colors = schoolTextFieldColors(),
            modifier = Modifier
              .weight(1f)
              .testTag("student_session_input")
          )
        }

        OutlinedTextField(
          value = monthlyFeeText,
          onValueChange = { monthlyFeeText = it.filter { ch -> ch.isDigit() }.take(6) },
          label = { Text(strings.monthlyFee) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          colors = schoolTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("student_fee_input")
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text(strings.address) },
          minLines = 2,
          maxLines = 3,
          colors = schoolTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("student_address_input")
        )

        OutlinedTextField(
          value = previousSchool,
          onValueChange = { previousSchool = it },
          label = { Text(strings.previousSchool) },
          singleLine = true,
          colors = schoolTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("student_prev_school_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${strings.status}: ${if (isActive) strings.active else strings.inactive}",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate700
          )
          Switch(
            checked = isActive,
            onCheckedChange = { isActive = it },
            modifier = Modifier.testTag("student_status_switch")
          )
        }

        if (validationError != null) {
          Text(
            text = validationError ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = RoseDue,
            modifier = Modifier.testTag("student_validation_error")
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text(strings.cancel, color = Slate500)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              val digits = phone.filter { it.isDigit() }
              when {
                admissionNo.isBlank() || rollNumber.isBlank() ||
                  fullName.trim().length < 2 || fatherName.trim().length < 2 -> {
                  validationError = strings.validationRequiredField
                }

                digits.length < 10 -> {
                  validationError = strings.invalidMobileError
                }

                StudentExportUtils.isAdmissionNoDuplicate(
                  students = allStudents,
                  admissionNo = admissionNo,
                  excludeStudentId = existingStudent?.id
                ) -> {
                  validationError = strings.duplicateAdmissionError
                }

                else -> {
                  validationError = null
                  val selectedUri = pickedPhotoUri
                  if (selectedUri == null) {
                    onSave(
                      admissionNo,
                      rollNumber,
                      fullName,
                      fatherName,
                      motherName,
                      className,
                      section.ifBlank { "A" },
                      gender,
                      phone,
                      address,
                      monthlyFeeText.toLongOrNull() ?: 0L,
                      if (isActive) StudentStatus.ACTIVE else StudentStatus.INACTIVE,
                      photoUrl,
                      dateOfBirth,
                      admissionDate,
                      academicSession.ifBlank { activeSession },
                      previousSchool,
                      null
                    )
                  } else {
                    isCompressingPhoto = true
                    scope.launch {
                      val bytes = withContext(Dispatchers.IO) {
                        runCatching {
                          StudentExportUtils.compressImageUriToJpegBytes(context, selectedUri)
                        }.getOrNull()
                      }
                      isCompressingPhoto = false
                      onSave(
                        admissionNo,
                        rollNumber,
                        fullName,
                        fatherName,
                        motherName,
                        className,
                        section.ifBlank { "A" },
                        gender,
                        phone,
                        address,
                        monthlyFeeText.toLongOrNull() ?: 0L,
                        if (isActive) StudentStatus.ACTIVE else StudentStatus.INACTIVE,
                        photoUrl,
                        dateOfBirth,
                        admissionDate,
                        academicSession.ifBlank { activeSession },
                        previousSchool,
                        bytes
                      )
                    }
                  }
                }
              }
            },
            enabled = !isSaving && !isCompressingPhoto,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
            modifier = Modifier.testTag("save_student_button")
          ) {
            if (isSaving || isCompressingPhoto) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PureWhite, strokeWidth = 2.dp)
            } else {
              Text(strings.save, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  activeDatePickerTarget?.let { target ->
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
      onDismissRequest = { activeDatePickerTarget = null },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { millis ->
              val formatted = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(millis))
              if (target == "DOB") dateOfBirth = formatted else admissionDate = formatted
            }
            activeDatePickerTarget = null
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { activeDatePickerTarget = null }) {
          Text(strings.cancel)
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }
}
