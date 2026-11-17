package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLanguage

private val SchoolLightColorScheme = lightColorScheme(
  primary = RoyalBlue800,
  onPrimary = PureWhite,
  primaryContainer = SkyBlue100,
  onPrimaryContainer = RoyalBlue900,
  secondary = AzureBlue600,
  onSecondary = PureWhite,
  secondaryContainer = SkyBlue50,
  onSecondaryContainer = RoyalBlue800,
  tertiary = CrestGold,
  onTertiary = PureWhite,
  tertiaryContainer = CrestGoldLight,
  onTertiaryContainer = Slate900,
  background = OffWhiteBg,
  onBackground = Slate900,
  surface = PureWhite,
  onSurface = Slate900,
  surfaceVariant = SurfaceTintBlue,
  onSurfaceVariant = Slate700,
  outline = Slate300,
  outlineVariant = Slate200,
  error = RoseDue,
  onError = PureWhite,
  errorContainer = RoseDueBg,
  onErrorContainer = RoseDue
)

val SchoolShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SchoolTheme(
  language: AppLanguage = AppLanguage.EN,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = SchoolLightColorScheme,
    typography = buildSchoolTypography(isHindi = language == AppLanguage.HI),
    shapes = SchoolShapes,
    content = content
  )
}
