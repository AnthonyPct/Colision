package com.anthooop.colision.core.design

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.dm_sans_bold
import colision.composeapp.generated.resources.dm_sans_medium
import colision.composeapp.generated.resources.dm_sans_regular
import colision.composeapp.generated.resources.dm_sans_semibold
import org.jetbrains.compose.resources.Font

// Type scale mapped from docs/design/project/tokens.jsx (the `T` object).
// DM Sans is the design intent (chat1.md). The TTF files live in
// commonMain/composeResources/font/ — Compose generates a `Res.font.*`
// accessor per file at build time.
@Composable
fun colisionFontFamily(): FontFamily = FontFamily(
    Font(Res.font.dm_sans_regular, weight = FontWeight.Normal),
    Font(Res.font.dm_sans_medium, weight = FontWeight.Medium),
    Font(Res.font.dm_sans_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.dm_sans_bold, weight = FontWeight.Bold),
)

@Composable
private fun colisionTextStyle(
    family: FontFamily,
    fontSize: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacingEm: Float = 0f,
): TextStyle = TextStyle(
    fontFamily = family,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacingEm.em,
)

@Composable
fun colisionTypography(): Typography {
    val family = colisionFontFamily()
    return Typography(
        // display
        displayLarge = colisionTextStyle(family, 32, 40, FontWeight.Bold, -0.015f),
        displayMedium = colisionTextStyle(family, 32, 40, FontWeight.Bold, -0.015f),
        displaySmall = colisionTextStyle(family, 26, 32, FontWeight.Bold, -0.011f),
        // headline
        headlineLarge = colisionTextStyle(family, 26, 32, FontWeight.Bold, -0.011f),
        headlineMedium = colisionTextStyle(family, 26, 32, FontWeight.Bold, -0.011f),
        headlineSmall = colisionTextStyle(family, 20, 26, FontWeight.SemiBold, -0.010f),
        // title
        titleLarge = colisionTextStyle(family, 20, 26, FontWeight.SemiBold, -0.010f),
        titleMedium = colisionTextStyle(family, 17, 22, FontWeight.SemiBold, -0.006f),
        titleSmall = colisionTextStyle(family, 15, 20, FontWeight.SemiBold),
        // body
        bodyLarge = colisionTextStyle(family, 17, 24, FontWeight.Normal),
        bodyMedium = colisionTextStyle(family, 15, 22, FontWeight.Normal),
        bodySmall = colisionTextStyle(family, 13, 18, FontWeight.Normal),
        // label
        labelLarge = colisionTextStyle(family, 16, 20, FontWeight.SemiBold, -0.006f),
        labelMedium = colisionTextStyle(family, 13, 18, FontWeight.Medium, 0.008f),
        labelSmall = colisionTextStyle(family, 11, 14, FontWeight.Medium, 0.036f),
    )
}
