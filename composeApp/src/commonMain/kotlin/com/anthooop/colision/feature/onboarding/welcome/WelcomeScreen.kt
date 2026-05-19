package com.anthooop.colision.feature.onboarding.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.welcome_action_create
import colision.composeapp.generated.resources.welcome_action_join
import colision.composeapp.generated.resources.welcome_subtitle
import colision.composeapp.generated.resources.welcome_title
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = Spacing.SP6,
                end = Spacing.SP6,
                top = Spacing.SP8 + safeDrawing.calculateTopPadding(),
                bottom = Spacing.SP8 + safeDrawing.calculateBottomPadding(),
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.Bottom,
        ) {
            HeroIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(bottom = Spacing.SP10),
            )

            Text(
                text = stringResource(Res.string.welcome_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Spacing.SP3))
            Text(
                text = stringResource(Res.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.SP8),
            verticalArrangement = Arrangement.spacedBy(Spacing.SP3),
        ) {
            Button(
                onClick = { onIntent(WelcomeIntent.CreateProjectTapped) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = Spacing.SP6),
                enabled = !state.isLoading,
            ) {
                Text(
                    text = stringResource(Res.string.welcome_action_create),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
            OutlinedButton(
                onClick = { onIntent(WelcomeIntent.JoinProjectTapped) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = Spacing.SP6),
                enabled = !state.isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(
                    text = stringResource(Res.string.welcome_action_join),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Two overlapping circles representing commissions, with a coral-highlighted
 * dot at the intersection symbolising a conflicting member. Mirrors the SVG
 * in `docs/design/project/screens-onboarding.jsx:15`.
 */
@Composable
private fun HeroIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val coral = MaterialTheme.colorScheme.error
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Both circles are radius ≈ 86 in a 320×240 viewBox. Scale to fit.
            val scaleX = size.width / 320f
            val scaleY = size.height / 240f
            val radius = 86f * minOf(scaleX, scaleY)
            val leftCenter = Offset(120f * scaleX, 120f * scaleY)
            val rightCenter = Offset(210f * scaleX, 120f * scaleY)

            // Left circle with gradient fill.
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryContainer,
                        primaryContainer.copy(alpha = 0.4f),
                    ),
                    start = Offset(leftCenter.x - radius, leftCenter.y - radius),
                    end = Offset(leftCenter.x + radius, leftCenter.y + radius),
                ),
                radius = radius,
                center = leftCenter,
            )
            // Right circle, translucent primary.
            drawCircle(
                color = primary.copy(alpha = 0.18f),
                radius = radius,
                center = rightCenter,
            )
            // Outlines.
            drawCircle(
                color = primary,
                radius = radius,
                center = leftCenter,
                style = Stroke(width = 2f),
            )
            drawCircle(
                color = primary,
                radius = radius,
                center = rightCenter,
                style = Stroke(width = 2f),
            )

            // Member dots inside each commission.
            val leftDots = listOf(
                Offset(88f, 86f), Offset(100f, 140f), Offset(140f, 76f),
                Offset(78f, 116f), Offset(152f, 156f),
            )
            val rightDots = listOf(
                Offset(238f, 86f), Offset(248f, 140f), Offset(228f, 76f),
                Offset(252f, 116f), Offset(200f, 168f),
            )
            (leftDots + rightDots).forEach { p ->
                drawCircle(
                    color = primary,
                    radius = 5f * minOf(scaleX, scaleY),
                    center = Offset(p.x * scaleX, p.y * scaleY),
                )
            }
            // Member in conflict at intersection.
            val conflictCenter = Offset(165f * scaleX, 120f * scaleY)
            drawCircle(
                color = coral,
                radius = 9f * minOf(scaleX, scaleY),
                center = conflictCenter,
            )
            drawCircle(
                color = coral.copy(alpha = 0.4f),
                radius = 14f * minOf(scaleX, scaleY),
                center = conflictCenter,
                style = Stroke(width = 1.5f),
            )
            drawCircle(
                color = coral.copy(alpha = 0.2f),
                radius = 20f * minOf(scaleX, scaleY),
                center = conflictCenter,
                style = Stroke(width = 1.2f),
            )
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    ColisionTheme {
        WelcomeScreen(state = WelcomeState(), onIntent = {})
    }
}
