package com.anthooop.colision.feature.onboarding.projectcreate

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.design.Spacing

@Composable
fun CreateProjectScreen(
    state: CreateProjectState,
    onIntent: (CreateProjectIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = Spacing.SP6,
                end = Spacing.SP6,
                top = Spacing.SP4 + safe.calculateTopPadding(),
                bottom = Spacing.SP8 + safe.calculateBottomPadding(),
            ),
    ) {
        // Back button row.
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.SP6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onIntent(CreateProjectIntent.BackTapped) },
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
            text = "Crée ton projet",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP3))
        Text(
            text = "Donne-lui un nom pour que tes collègues le reconnaissent au premier coup d'œil.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP8))

        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(CreateProjectIntent.NameChanged(it)) },
            placeholder = {
                Text(
                    text = "Ex. Conseil municipal de Saint-Machin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            label = {
                Text(text = "Nom du projet", style = MaterialTheme.typography.labelMedium)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting,
            shape = RoundedCornerShape(14.dp),
        )

        if (state.error != null) {
            Spacer(Modifier.height(Spacing.SP3))
            Text(
                text = errorMessage(state.error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onIntent(CreateProjectIntent.SubmitTapped) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.canSubmit,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(horizontal = Spacing.SP6),
        ) {
            if (state.isSubmitting) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                Text(
                    text = "Créer",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun errorMessage(error: AppError): String = when (error) {
    AppError.NetworkUnavailable, AppError.ServerUnreachable ->
        "Impossible de créer le projet — vérifie ta connexion."
    is AppError.Unknown -> "Impossible de créer le projet — réessaie dans un instant."
    else -> "Impossible de créer le projet — réessaie dans un instant."
}

@Preview
@Composable
private fun CreateProjectScreenPreview() {
    ColisionTheme {
        CreateProjectScreen(
            state = CreateProjectState(name = "Conseil municipal de Saint-Machin"),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun CreateProjectScreenSubmittingPreview() {
    ColisionTheme {
        CreateProjectScreen(
            state = CreateProjectState(
                name = "Conseil municipal de Saint-Machin",
                isSubmitting = true,
            ),
            onIntent = {},
        )
    }
}
