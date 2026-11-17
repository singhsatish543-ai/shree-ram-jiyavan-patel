package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.LanguageTogglePill
import com.example.ui.theme.AzureBlue500
import com.example.ui.theme.CrestGold
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue700
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue100
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  strings: AppStrings,
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onContinue: () -> Unit
) {
  val scaleAnim = remember { Animatable(0.84f) }
  val alphaAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    scaleAnim.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(Unit) {
    alphaAnim.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 600)
    )
    delay(1600L)
    onContinue()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(RoyalBlue900, RoyalBlue800, AzureBlue500)
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp)
  ) {
    // Top bar with session badge & bilingual switcher
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(50),
        color = PureWhite.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.25f))
      ) {
        Text(
          text = strings.academicSessionLabel,
          style = MaterialTheme.typography.labelMedium,
          color = PureWhite,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }

      LanguageTogglePill(
        language = language,
        onLanguageChange = onLanguageChange,
        onDarkBackground = true
      )
    }

    // Center Emblem, School Name & Address
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .widthIn(max = 520.dp)
        .scale(scaleAnim.value)
        .alpha(alphaAnim.value),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Surface(
        shape = CircleShape,
        color = PureWhite,
        shadowElevation = 14.dp,
        border = BorderStroke(4.dp, CrestGold.copy(alpha = 0.85f)),
        modifier = Modifier.size(144.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_school_logo),
          contentDescription = strings.schoolName,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = strings.schoolName,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = PureWhite,
        textAlign = TextAlign.Center
      )

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.24f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = strings.schoolAddress,
            tint = SkyBlue100,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = strings.schoolAddress,
            style = MaterialTheme.typography.bodyMedium,
            color = PureWhite.copy(alpha = 0.95f),
            textAlign = TextAlign.Center
          )
        }
      }

      Text(
        text = strings.schoolMotto,
        style = MaterialTheme.typography.labelLarge,
        color = SkyBlue100,
        textAlign = TextAlign.Center
      )
    }

    // Bottom Continue action + subtle indicator
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .widthIn(max = 420.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(16.dp),
          color = PureWhite,
          strokeWidth = 2.dp
        )
        Text(
          text = strings.splashLoading,
          style = MaterialTheme.typography.bodySmall,
          color = PureWhite.copy(alpha = 0.85f)
        )
      }

      Button(
        onClick = onContinue,
        colors = ButtonDefaults.buttonColors(
          containerColor = PureWhite,
          contentColor = RoyalBlue900
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("splash_continue_button")
      ) {
        Text(
          text = strings.continueBtn,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = strings.continueBtn,
          tint = RoyalBlue700
        )
      }
    }
  }
}
