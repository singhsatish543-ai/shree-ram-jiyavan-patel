package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.PaymentMode
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.CrestGold
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoyalBlue700
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue100
import com.example.ui.theme.SkyBlue50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import java.text.NumberFormat
import java.util.Locale

fun formatInr(amount: Long): String {
  val formatter = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
  return "₹${formatter.format(amount)}"
}

fun paymentModeLabel(modeRaw: String, strings: AppStrings): String =
  when (PaymentMode.fromRaw(modeRaw)) {
    PaymentMode.CASH -> strings.modeCash
    PaymentMode.UPI -> strings.modeUpi
    PaymentMode.BANK_TRANSFER -> strings.modeBank
    PaymentMode.CHEQUE -> strings.modeCheque
  }

@Composable
fun LanguageTogglePill(
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  modifier: Modifier = Modifier,
  onDarkBackground: Boolean = true
) {
  val containerColor = if (onDarkBackground) PureWhite.copy(alpha = 0.16f) else SkyBlue50
  val borderColor = if (onDarkBackground) PureWhite.copy(alpha = 0.35f) else SkyBlue100

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(containerColor)
      .border(1.dp, borderColor, RoundedCornerShape(50))
      .padding(3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    AppLanguage.entries.forEach { option ->
      val selected = option == language
      val pillBg = when {
        selected && onDarkBackground -> PureWhite
        selected && !onDarkBackground -> RoyalBlue800
        else -> Color.Transparent
      }
      val textColor = when {
        selected && onDarkBackground -> RoyalBlue900
        selected && !onDarkBackground -> PureWhite
        onDarkBackground -> PureWhite
        else -> RoyalBlue800
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(50))
          .background(pillBg)
          .clickable { onLanguageChange(option) }
          .minimumInteractiveComponentSize()
          .padding(horizontal = 10.dp, vertical = 4.dp)
          .testTag("lang_toggle_${option.code.lowercase()}"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (option == AppLanguage.EN) "EN" else "हिन्दी",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
          color = textColor
        )
      }
    }
  }
}

@Composable
fun SchoolTopBar(
  title: String,
  subtitle: String,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onSwitchRole: () -> Unit,
  onSignOut: () -> Unit,
  strings: AppStrings,
  modifier: Modifier = Modifier,
  showBackButton: Boolean = false,
  onBackClick: () -> Unit = {},
  showAdminActions: Boolean = false,
  adminDisplayName: String = "",
  sessionLabel: String = strings.academicSessionLabel,
  noticeCount: Int = 0,
  onNotificationsClick: () -> Unit = {},
  onProfileClick: () -> Unit = {}
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          colors = listOf(RoyalBlue900, RoyalBlue700)
        )
      )
      .statusBarsPadding()
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (showBackButton) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("top_bar_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = strings.backToDashboard,
            tint = PureWhite
          )
        }
      }

      Surface(
        shape = CircleShape,
        color = PureWhite,
        shadowElevation = 4.dp,
        modifier = Modifier.size(44.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_school_logo),
          contentDescription = strings.schoolName,
          contentScale = ContentScale.Crop,
          modifier = Modifier.padding(3.dp).clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = PureWhite,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = PureWhite.copy(alpha = 0.85f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      LanguageTogglePill(
        language = language,
        onLanguageChange = onLanguageChange,
        onDarkBackground = true
      )

      if (!showAdminActions) {
        IconButton(
          onClick = onSwitchRole,
          modifier = Modifier.testTag("switch_role_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.SwapHoriz,
            contentDescription = strings.switchPortal,
            tint = PureWhite
          )
        }

        IconButton(
          onClick = onSignOut,
          modifier = Modifier.testTag("sign_out_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = strings.signOut,
            tint = PureWhite
          )
        }
      }
    }

    if (showAdminActions) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(PureWhite.copy(alpha = 0.13f))
          .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Surface(
          shape = RoundedCornerShape(50),
          color = PureWhite.copy(alpha = 0.18f),
          modifier = Modifier
            .weight(1f, fill = false)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onProfileClick)
            .minimumInteractiveComponentSize()
            .testTag("header_admin_profile")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.AccountCircle,
              contentDescription = strings.navProfile,
              tint = PureWhite,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${adminDisplayName.ifBlank { strings.adminRole }} • $sessionLabel",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = PureWhite,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.testTag("header_notifications_button")
          ) {
            BadgedBox(
              badge = {
                if (noticeCount > 0) {
                  Badge(
                    containerColor = CrestGold,
                    contentColor = PureWhite
                  ) {
                    Text(if (noticeCount > 9) "9+" else noticeCount.toString())
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = strings.notificationsLabel,
                tint = PureWhite
              )
            }
          }

          IconButton(
            onClick = onSwitchRole,
            modifier = Modifier.testTag("switch_role_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.SwapHoriz,
              contentDescription = strings.switchPortal,
              tint = PureWhite
            )
          }

          IconButton(
            onClick = onSignOut,
            modifier = Modifier.testTag("sign_out_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.Logout,
              contentDescription = strings.signOut,
              tint = PureWhite
            )
          }
        }
      }
    }
  }
}

@Composable
fun SummaryKpiCard(
  label: String,
  value: String,
  icon: ImageVector,
  accentColor: Color,
  iconBg: Color,
  modifier: Modifier = Modifier,
  supportingText: String? = null
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    color = PureWhite,
    border = BorderStroke(1.dp, Slate200),
    shadowElevation = 2.dp
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(iconBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
        if (!supportingText.isNullOrBlank()) {
          Text(
            text = supportingText,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Slate900,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = Slate500,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun EmptyStateCard(
  icon: ImageVector,
  title: String,
  description: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = PureWhite,
    border = BorderStroke(1.dp, SkyBlue100)
  ) {
    Column(
      modifier = Modifier.padding(22.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(SkyBlue50),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = RoyalBlue700,
          modifier = Modifier.size(26.dp)
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Slate900
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = Slate500
      )
      if (actionLabel != null && onAction != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Button(
          onClick = onAction,
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
          modifier = Modifier.testTag("empty_state_action")
        ) {
          Text(actionLabel)
        }
      }
    }
  }
}

@Composable
fun ConfirmDeleteDialog(
  title: String,
  message: String,
  confirmLabel: String,
  cancelLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, fontWeight = FontWeight.Bold) },
    text = { Text(message) },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = RoseDue),
        modifier = Modifier.testTag("confirm_delete_button")
      ) {
        Text(confirmLabel)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_delete_button")) {
        Text(cancelLabel)
      }
    }
  )
}
