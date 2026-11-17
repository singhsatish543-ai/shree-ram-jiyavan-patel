package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val DisplayFontFamily = FontFamily(
  Font(R.font.outfit, FontWeight.Normal)
)

val BodyFontFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal)
)

val DevanagariFontFamily = FontFamily(
  Font(R.font.noto_sans_devanagari, FontWeight.Normal)
)

fun buildSchoolTypography(isHindi: Boolean): Typography {
  val headingFamily = if (isHindi) DevanagariFontFamily else DisplayFontFamily
  val bodyFamily = if (isHindi) DevanagariFontFamily else BodyFontFamily

  return Typography(
    displaySmall = TextStyle(
      fontFamily = headingFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp,
      lineHeight = 36.sp,
      letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
      fontFamily = headingFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
      lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
      fontFamily = headingFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 19.sp,
      lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
      fontFamily = headingFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
      lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
      fontFamily = headingFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 16.sp,
      lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp,
      lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 15.sp,
      lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp,
      lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 14.sp
    )
  )
}
