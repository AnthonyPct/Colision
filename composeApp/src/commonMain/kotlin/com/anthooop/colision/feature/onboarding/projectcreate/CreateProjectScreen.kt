package com.anthooop.colision.feature.onboarding.projectcreate

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.create_project_action_submit
import colision.composeapp.generated.resources.create_project_display_name_label
import colision.composeapp.generated.resources.create_project_display_name_placeholder
import colision.composeapp.generated.resources.create_project_error_generic
import colision.composeapp.generated.resources.create_project_error_network
import colision.composeapp.generated.resources.create_project_name_label
import colision.composeapp.generated.resources.create_project_name_placeholder
import colision.composeapp.generated.resources.create_project_subtitle
import colision.composeapp.generated.resources.create_project_title
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

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
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.SP6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onIntent(CreateProjectIntent.BackTapped) },
                contentPadding = PaddingValues(horizontal = Spacing.SP3),
            ) {
                Text(
                    text = stringResource(Res.string.action_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            text = stringResource(Res.string.create_project_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP3))
        Text(
            text = stringResource(Res.string.create_project_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP8))

        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(CreateProjectIntent.NameChanged(it)) },
            placeholder = {
                Text(
                    text = stringResource(Res.string.create_project_name_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            label = {
                Text(
                    text = stringResource(Res.string.create_project_name_label),
                    style = MaterialTheme.typography.labelMedium,
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting,
            shape = RoundedCornerShape(14.dp),
        )

        Spacer(Modifier.height(Spacing.SP4))

        OutlinedTextField(
            value = state.displayName,
            onValueChange = { onIntent(CreateProjectIntent.DisplayNameChanged(it)) },
            placeholder = {
                Text(
                    text = stringResource(Res.string.create_project_display_name_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            label = {
                Text(
                    text = stringResource(Res.string.create_project_display_name_label),
                    style = MaterialTheme.typography.labelMedium,
                )
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
                    text = stringResource(Res.string.create_project_action_submit),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun errorMessage(error: AppError): String = when (error) {
    AppError.NetworkUnavailable, AppError.ServerUnreachable ->
        stringResource(Res.string.create_project_error_network)
    else -> stringResource(Res.string.create_project_error_generic)
}

@Preview
@Composable
private fun CreateProjectScreenPreview() {
    ColisionTheme {
        CreateProjectScreen(
            state = CreateProjectState(
                name = "Conseil municipal de Saint-Machin",
                displayName = "Antoine",
            ),
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
                displayName = "Antoine",
                isSubmitting = true,
            ),
            onIntent = {},
        )
    }
}
