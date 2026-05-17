package com.anthooop.colision.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Placeholder theme. Story 1.3 swaps this for the real Colision design system
// (tokens.jsx → ColorScheme + Typography + Shapes + Spacing).
@Composable
fun ColisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(content = content)
}
