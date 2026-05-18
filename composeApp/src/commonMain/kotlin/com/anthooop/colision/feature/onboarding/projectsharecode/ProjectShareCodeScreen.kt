package com.anthooop.colision.feature.onboarding.projectsharecode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anthooop.colision.core.design.Spacing

@Composable
fun ProjectShareCodeScreen(
    state: ProjectShareCodeState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ProjectShareCodeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = Spacing.SP6,
                    end = Spacing.SP6,
                    top = Spacing.SP4 + safe.calculateTopPadding(),
                    bottom = Spacing.SP8 + safe.calculateBottomPadding(),
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.SP6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { onIntent(ProjectShareCodeIntent.BackTapped) },
                    contentPadding = PaddingValues(horizontal = Spacing.SP3),
                ) {
                    Text(
                        text = "Retour",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Text(
                text = "Voici ton code\nde partage",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Spacing.SP3))
            Text(
                text = "Partage ce code aux autres membres de ton conseil pour qu'ils rejoignent ${state.projectName.ifEmpty { "le projet" }}.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.SP10))

            ShareCodeDisplay(code = state.shareCode)

            Spacer(Modifier.height(Spacing.SP6))

            OutlinedButton(
                onClick = { onIntent(ProjectShareCodeIntent.CopyTapped) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(percent = 50),
                enabled = state.shareCode.isNotEmpty(),
            ) {
                Text(
                    text = "Copier",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onIntent(ProjectShareCodeIntent.ContinueTapped) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = Spacing.SP6),
                enabled = !state.isLoading,
            ) {
                Text(
                    text = "Continuer",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp + safe.calculateBottomPadding()),
        )
    }
}

@Composable
private fun ShareCodeDisplay(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(vertical = Spacing.SP8),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.SP3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val padded = code.padEnd(6, '·')
            padded.take(6).forEach { c ->
                Text(
                    text = c.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = if (c == '·') MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
