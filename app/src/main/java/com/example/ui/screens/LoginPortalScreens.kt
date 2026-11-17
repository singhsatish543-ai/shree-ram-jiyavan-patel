package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.auth.onGoogleSignInClicked
import com.example.data.model.PortalRole
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.LanguageTogglePill
import com.example.ui.theme.AzureBlue600
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
import com.example.ui.theme.Slate900

data class PendingLoginMetadata(
  val role: PortalRole,
  val displayName: String,
  val staffCode: String = "",
  val admissionNo: String = "",
  val assignedClass: String = ""
)

@Composable
fun AdminLoginScreen(
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onSelectRoleScreen: (PortalRole) -> Unit,
  onLoginSuccess: (PendingLoginMetadata) -> Unit
) {
  var adminName by remember { mutableStateOf("") }

  RoleLoginScaffold(
    activeRole = PortalRole.ADMIN,
    title = strings.adminLoginTitle,
    description = strings.adminLoginDesc,
    roleIcon = Icons.Filled.AdminPanelSettings,
    strings = strings,
    language = language,
    onLanguageChange = onLanguageChange,
    onSelectRoleScreen = onSelectRoleScreen,
    buildMetadata = {
      PendingLoginMetadata(
        role = PortalRole.ADMIN,
        displayName = adminName.trim()
      )
    },
    onLoginSuccess = onLoginSuccess
  ) {
    OutlinedTextField(
      value = adminName,
      onValueChange = { adminName = it },
      label = { Text(strings.adminNameHint) },
      leadingIcon = {
        Icon(Icons.Filled.Person, contentDescription = strings.adminNameHint, tint = RoyalBlue700)
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = schoolTextFieldColors(),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("admin_name_input")
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLoginScreen(
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onSelectRoleScreen: (PortalRole) -> Unit,
  onLoginSuccess: (PendingLoginMetadata) -> Unit
) {
  var teacherName by remember { mutableStateOf("") }
  var staffCode by remember { mutableStateOf("") }
  var assignedClass by remember { mutableStateOf(strings.classOptions.first()) }
  var classExpanded by remember { mutableStateOf(false) }

  RoleLoginScaffold(
    activeRole = PortalRole.TEACHER,
    title = strings.teacherLoginTitle,
    description = strings.teacherLoginDesc,
    roleIcon = Icons.Filled.Badge,
    strings = strings,
    language = language,
    onLanguageChange = onLanguageChange,
    onSelectRoleScreen = onSelectRoleScreen,
    buildMetadata = {
      PendingLoginMetadata(
        role = PortalRole.TEACHER,
        displayName = teacherName.trim(),
        staffCode = staffCode.trim(),
        assignedClass = assignedClass
      )
    },
    onLoginSuccess = onLoginSuccess
  ) {
    OutlinedTextField(
      value = teacherName,
      onValueChange = { teacherName = it },
      label = { Text(strings.fullName) },
      leadingIcon = {
        Icon(Icons.Filled.Person, contentDescription = strings.fullName, tint = RoyalBlue700)
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = schoolTextFieldColors(),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("teacher_name_input")
    )

    OutlinedTextField(
      value = staffCode,
      onValueChange = { staffCode = it },
      label = { Text(strings.staffCodeHint) },
      leadingIcon = {
        Icon(Icons.Filled.Badge, contentDescription = strings.staffCodeHint, tint = RoyalBlue700)
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = schoolTextFieldColors(),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("teacher_staff_code_input")
    )

    ExposedDropdownMenuBox(
      expanded = classExpanded,
      onExpandedChange = { classExpanded = it }
    ) {
      OutlinedTextField(
        value = assignedClass,
        onValueChange = {},
        readOnly = true,
        label = { Text(strings.assignedClassHint) },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
        shape = RoundedCornerShape(14.dp),
        colors = schoolTextFieldColors(),
        modifier = Modifier
          .menuAnchor(MenuAnchorType.PrimaryNotEditable)
          .fillMaxWidth()
          .testTag("teacher_class_dropdown")
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
  }
}

@Composable
fun StudentLoginScreen(
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onSelectRoleScreen: (PortalRole) -> Unit,
  onLoginSuccess: (PendingLoginMetadata) -> Unit
) {
  var studentName by remember { mutableStateOf("") }
  var admissionNo by remember { mutableStateOf("") }

  RoleLoginScaffold(
    activeRole = PortalRole.STUDENT,
    title = strings.studentLoginTitle,
    description = strings.studentLoginDesc,
    roleIcon = Icons.Filled.School,
    strings = strings,
    language = language,
    onLanguageChange = onLanguageChange,
    onSelectRoleScreen = onSelectRoleScreen,
    buildMetadata = {
      PendingLoginMetadata(
        role = PortalRole.STUDENT,
        displayName = studentName.trim(),
        admissionNo = admissionNo.trim()
      )
    },
    onLoginSuccess = onLoginSuccess
  ) {
    OutlinedTextField(
      value = studentName,
      onValueChange = { studentName = it },
      label = { Text(strings.fullName) },
      leadingIcon = {
        Icon(Icons.Filled.Person, contentDescription = strings.fullName, tint = RoyalBlue700)
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = schoolTextFieldColors(),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("student_login_name_input")
    )

    OutlinedTextField(
      value = admissionNo,
      onValueChange = { admissionNo = it },
      label = { Text(strings.admissionNoHint) },
      leadingIcon = {
        Icon(Icons.Filled.School, contentDescription = strings.admissionNoHint, tint = RoyalBlue700)
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = schoolTextFieldColors(),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("student_login_admission_input")
    )
  }
}

@Composable
private fun RoleLoginScaffold(
  activeRole: PortalRole,
  title: String,
  description: String,
  roleIcon: ImageVector,
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onSelectRoleScreen: (PortalRole) -> Unit,
  buildMetadata: () -> PendingLoginMetadata,
  onLoginSuccess: (PendingLoginMetadata) -> Unit,
  formFields: @Composable () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhiteBg)
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Blue Brand Header
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(listOf(RoyalBlue900, AzureBlue600))
        )
        .statusBarsPadding()
        .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp)
          .align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = strings.portalTitle,
            style = MaterialTheme.typography.titleSmall,
            color = SkyBlue100
          )
          LanguageTogglePill(
            language = language,
            onLanguageChange = onLanguageChange,
            onDarkBackground = true
          )
        }

        Surface(
          shape = CircleShape,
          color = PureWhite,
          shadowElevation = 6.dp,
          modifier = Modifier.size(76.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_school_logo),
            contentDescription = strings.schoolName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .padding(4.dp)
              .clip(CircleShape)
          )
        }

        Text(
          text = strings.schoolName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = PureWhite,
          textAlign = TextAlign.Center
        )
        Text(
          text = strings.schoolAddress,
          style = MaterialTheme.typography.bodySmall,
          color = PureWhite.copy(alpha = 0.85f),
          textAlign = TextAlign.Center
        )
      }
    }

    // Content Container
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 560.dp)
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Role Switcher Tabs (Admin Login | Teacher Login | Student Login)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Slate200),
        shadowElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val roles = listOf(
            Triple(PortalRole.ADMIN, strings.adminRole, Icons.Filled.AdminPanelSettings),
            Triple(PortalRole.TEACHER, strings.teacherRole, Icons.Filled.Badge),
            Triple(PortalRole.STUDENT, strings.studentRole, Icons.Filled.School)
          )
          roles.forEach { (role, label, icon) ->
            val selected = role == activeRole
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (selected) RoyalBlue800 else SkyBlue50,
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectRoleScreen(role) }
                .minimumInteractiveComponentSize()
                .testTag("role_tab_${role.name.lowercase()}")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = icon,
                  contentDescription = label,
                  tint = if (selected) PureWhite else RoyalBlue800,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = if (selected) PureWhite else RoyalBlue800,
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // Main Role Login Card
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Slate200),
        shadowElevation = 4.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SkyBlue50),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = roleIcon,
                contentDescription = title,
                tint = RoyalBlue800,
                modifier = Modifier.size(26.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = strings.portalSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
          }

          Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500
          )

          formFields()

          if (errorMessage != null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = RoseDueBg,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = RoseDue,
                modifier = Modifier.padding(12.dp)
              )
            }
          }

          Button(
            onClick = {
              errorMessage = null
              isLoading = true
              val metadata = buildMetadata()
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  onLoginSuccess(metadata)
                },
                onAuthError = { msg ->
                  isLoading = false
                  errorMessage = msg
                },
                scope = scope,
                onAuthCancelled = {
                  isLoading = false
                }
              )
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("google_sign_in_${activeRole.name.lowercase()}_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = PureWhite,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(strings.signingIn, fontWeight = FontWeight.Bold)
            } else {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = strings.signInWithGoogle,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = strings.signInWithGoogle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = strings.googleSignInNote,
            style = MaterialTheme.typography.labelSmall,
            color = Slate500,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

@Composable
fun schoolTextFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedBorderColor = RoyalBlue800,
  unfocusedBorderColor = Slate200,
  focusedLabelColor = RoyalBlue800,
  cursorColor = RoyalBlue800
)
