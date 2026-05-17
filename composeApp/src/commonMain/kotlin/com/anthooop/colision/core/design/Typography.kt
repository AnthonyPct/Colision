package com.anthooop.colision.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Type scale mapped from docs/design/project/tokens.jsx (the `T` object).
//
// Font family: DM Sans is the design intent (chat1.md). The TTF/OTF files are
// not yet bundled in commonMain/composeResources/font/ — when ops drops them
// in (e.g. dm_sans_regular.ttf, dm_sans_medium.ttf, dm_sans_semibold.ttf,
// dm_sans_bold.ttf), swap `colisionFontFamily` for a real FontFamily built
// via `Font(Res.font.dm_sans_regular, FontWeight.Normal)` etc. Until then we
// fall back to FontFamily.Default so the type scale is still correct.
private val colisionFontFamily: FontFamily = FontFamily.Default

private fun colisionTextStyle(
    fontSize: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacingEm: Float = 0f,
): TextStyle = TextStyle(
    fontFamily = colisionFontFamily,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacingEm.em,
)

val colisionTypography: Typography = Typography(
    // display
    displayLarge = colisionTextStyle(32, 40, FontWeight.Bold, -0.015f),
    displayMedium = colisionTextStyle(32, 40, FontWeight.Bold, -0.015f),
    displaySmall = colisionTextStyle(26, 32, FontWeight.Bold, -0.011f),
    // headline
    headlineLarge = colisionTextStyle(26, 32, FontWeight.Bold, -0.011f),
    headlineMedium = colisionTextStyle(26, 32, FontWeight.Bold, -0.011f),
    headlineSmall = colisionTextStyle(20, 26, FontWeight.SemiBold, -0.010f),
    // title
    titleLarge = colisionTextStyle(20, 26, FontWeight.SemiBold, -0.010f),
    titleMedium = colisionTextStyle(17, 22, FontWeight.SemiBold, -0.006f),
    titleSmall = colisionTextStyle(15, 20, FontWeight.SemiBold),
    // body
    bodyLarge = colisionTextStyle(17, 24, FontWeight.Normal),
    bodyMedium = colisionTextStyle(15, 22, FontWeight.Normal),
    bodySmall = colisionTextStyle(13, 18, FontWeight.Normal),
    // label
    labelLarge = colisionTextStyle(16, 20, FontWeight.SemiBold, -0.006f),
    labelMedium = colisionTextStyle(13, 18, FontWeight.Medium, 0.008f),
    labelSmall = colisionTextStyle(11, 14, FontWeight.Medium, 0.036f),
)
