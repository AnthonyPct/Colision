package com.anthooop.colision.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.anthooop.colision.core.design.colisionShapes
import com.anthooop.colision.core.design.colisionTypography
import com.anthooop.colision.core.design.darkForestColorScheme
import com.anthooop.colision.core.design.lightForestColorScheme

@Composable
fun ColisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkForestColorScheme else lightForestColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = colisionTypography,
        shapes = colisionShapes,
        content = content,
    )
}
