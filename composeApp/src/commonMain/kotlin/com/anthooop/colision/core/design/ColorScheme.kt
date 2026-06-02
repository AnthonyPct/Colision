package com.anthooop.colision.core.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Mapping of docs/design/project/tokens.jsx into Material 3 ColorScheme.
// Palette = "forest" (default at MVP, cf. cross-cutting concern #5 in
// docs/architecture.md). The 3 other palettes (coral, indigo, plum) are
// V1.1 tweaks-panel work and intentionally not mapped here.

// --- Forest palette
private val ForestPrimary = Color(0xFF0E7C66)
private val ForestPrimaryContainer = Color(0xFFC7E8DC)
private val ForestOnPrimary = Color(0xFFFFFFFF)
private val ForestOnPrimaryContainer = Color(0xFF032E25)

// --- Light tokens
private val LightBackground = Color(0xFFFAF7F2)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceContainer = Color(0xFFF4EFE5)
private val LightSurfaceContainerHigh = Color(0xFFEBE5D6)
private val LightSurfaceDim = Color(0xFFE8E2D3)

private val LightOutline = Color(0xFFDDD5C3)
private val LightOutlineVariant = Color(0xFFEBE5D6)

private val LightOnSurface = Color(0xFF1A1F1C)
private val LightOnSurfaceVariant = Color(0xFF5A615C)

private val LightError = Color(0xFFC8553D)
private val LightErrorContainer = Color(0xFFF9DDD3)
private val LightOnError = Color(0xFFFFFFFF)
private val LightOnErrorContainer = Color(0xFF421006)

private val LightScrim = Color(0xFF0F1310)

// --- Dark tokens
private val DarkBackground = Color(0xFF121512)
private val DarkSurface = Color(0xFF1A1F1C)
private val DarkSurfaceContainer = Color(0xFF222825)
private val DarkSurfaceContainerHigh = Color(0xFF2A312D)
private val DarkSurfaceDim = Color(0xFF0E1310)

private val DarkOutline = Color(0xFF3A413D)
private val DarkOutlineVariant = Color(0xFF2A312D)

private val DarkOnSurface = Color(0xFFF0EBE0)
private val DarkOnSurfaceVariant = Color(0xFFB4B8B1)

private val DarkError = Color(0xFFE78870)
private val DarkErrorContainer = Color(0xFF5C2615)
private val DarkOnError = Color(0xFFFFFFFF)
private val DarkOnErrorContainer = Color(0xFFF9DDD3)

private val DarkScrim = Color(0xFF000000)

val lightForestColorScheme: ColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = ForestPrimary,
    onSecondary = ForestOnPrimary,
    secondaryContainer = ForestPrimaryContainer,
    onSecondaryContainer = ForestOnPrimaryContainer,
    tertiary = ForestPrimary,
    onTertiary = ForestOnPrimary,
    tertiaryContainer = ForestPrimaryContainer,
    onTertiaryContainer = ForestOnPrimaryContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceContainerHigh,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerLow = LightSurface,
    surfaceContainerLowest = LightSurface,
    surfaceContainerHighest = LightSurfaceContainerHigh,
    surfaceDim = LightSurfaceDim,
    surfaceBright = LightSurface,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    scrim = LightScrim,
)

val darkForestColorScheme: ColorScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = Color(0xFF1F4A40),
    onPrimaryContainer = ForestPrimaryContainer,
    secondary = ForestPrimary,
    onSecondary = ForestOnPrimary,
    secondaryContainer = Color(0xFF1F4A40),
    onSecondaryContainer = ForestPrimaryContainer,
    tertiary = ForestPrimary,
    onTertiary = ForestOnPrimary,
    tertiaryContainer = Color(0xFF1F4A40),
    onTertiaryContainer = ForestPrimaryContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerLow = DarkSurface,
    surfaceContainerLowest = DarkSurfaceDim,
    surfaceContainerHighest = DarkSurfaceContainerHigh,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceContainerHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    scrim = DarkScrim,
)
