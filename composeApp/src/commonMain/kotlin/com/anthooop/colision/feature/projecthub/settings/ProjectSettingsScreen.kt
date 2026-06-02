package com.anthooop.colision.feature.projecthub.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_cancel
import colision.composeapp.generated.resources.action_copy
import colision.composeapp.generated.resources.action_delete
import colision.composeapp.generated.resources.action_leave
import colision.composeapp.generated.resources.action_ok
import colision.composeapp.generated.resources.dialog_error_title
import colision.composeapp.generated.resources.error_reason_fallback
import colision.composeapp.generated.resources.project_settings_delete_action_in_progress
import colision.composeapp.generated.resources.project_settings_delete_dialog_body
import colision.composeapp.generated.resources.project_settings_delete_dialog_title
import colision.composeapp.generated.resources.project_settings_delete_dialog_typed_error
import colision.composeapp.generated.resources.project_settings_delete_dialog_typed_hint
import colision.composeapp.generated.resources.project_settings_error
import colision.composeapp.generated.resources.project_settings_leave_dialog_body
import colision.composeapp.generated.resources.project_settings_leave_dialog_title
import colision.composeapp.generated.resources.project_settings_row_commissions
import colision.composeapp.generated.resources.project_settings_row_commissions_supporting
import colision.composeapp.generated.resources.project_settings_row_delete
import colision.composeapp.generated.resources.project_settings_row_delete_supporting
import colision.composeapp.generated.resources.project_settings_row_leave
import colision.composeapp.generated.resources.project_settings_row_leave_supporting
import colision.composeapp.generated.resources.project_settings_row_members
import colision.composeapp.generated.resources.project_settings_row_members_supporting
import colision.composeapp.generated.resources.project_settings_section_danger
import colision.composeapp.generated.resources.project_settings_section_management
import colision.composeapp.generated.resources.project_settings_share_code_label
import colision.composeapp.generated.resources.project_settings_share_code_placeholder
import colision.composeapp.generated.resources.project_settings_title_fallback
import colision.composeapp.generated.resources.write_offline_message
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.core.design.rememberOfflineGate
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProjectSettingsScreen(
    state: ProjectSettingsState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ProjectSettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Spacing.SP6,
                    end = Spacing.SP6,
                    top = Spacing.SP6 + safe.calculateTopPadding(),
                    bottom = Spacing.SP8 + safe.calculateBottomPadding(),
                ),
        ) {
            Text(
                text = state.projectName.ifEmpty {
                    stringResource(Res.string.project_settings_title_fallback)
                },
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(Spacing.SP4))

            ShareCodeCard(
                code = state.shareCode,
                onCopy = { onIntent(ProjectSettingsIntent.CopyShareCode) },
            )

            Spacer(Modifier.height(Spacing.SP8))

            SectionLabel(stringResource(Res.string.project_settings_section_management))
            Spacer(Modifier.height(Spacing.SP3))
            SettingsRow(
                title = stringResource(Res.string.project_settings_row_commissions),
                subtitle = stringResource(Res.string.project_settings_row_commissions_supporting),
                onClick = { onIntent(ProjectSettingsIntent.OpenCommissions) },
            )
            Spacer(Modifier.height(Spacing.SP2))
            SettingsRow(
                title = stringResource(Res.string.project_settings_row_members),
                subtitle = stringResource(Res.string.project_settings_row_members_supporting),
                onClick = { onIntent(ProjectSettingsIntent.OpenMembers) },
            )

            Spacer(Modifier.height(Spacing.SP8))

            SectionLabel(stringResource(Res.string.project_settings_section_danger))
            Spacer(Modifier.height(Spacing.SP3))
            val offlineGate = rememberOfflineGate(stringResource(Res.string.write_offline_message))
            SettingsRow(
                title = stringResource(Res.string.project_settings_row_leave),
                subtitle = stringResource(Res.string.project_settings_row_leave_supporting),
                onClick = { offlineGate.run { onIntent(ProjectSettingsIntent.LeaveTapped) } },
                emphasised = false,
                enabled = offlineGate.isOnline,
            )
            Spacer(Modifier.height(Spacing.SP2))
            SettingsRow(
                title = stringResource(Res.string.project_settings_row_delete),
                subtitle = stringResource(Res.string.project_settings_row_delete_supporting),
                onClick = { offlineGate.run { onIntent(ProjectSettingsIntent.DeleteTapped) } },
                emphasised = true,
                enabled = offlineGate.isOnline,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp + safe.calculateBottomPadding()),
        )
    }

    state.confirming?.let { ConfirmingDialog(it, state.isProcessing, onIntent) }
    state.pendingError?.let { error ->
        AlertDialog(
            onDismissRequest = { onIntent(ProjectSettingsIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(ProjectSettingsIntent.ErrorDismissed) }) {
                    Text(stringResource(Res.string.action_ok))
                }
            },
            title = { Text(stringResource(Res.string.dialog_error_title)) },
            text = { Text(projectSettingsErrorMessage(error)) },
        )
    }
}

@Composable
private fun projectSettingsErrorMessage(error: ProjectSettingsError): String = when (error) {
    is ProjectSettingsError.Network -> stringResource(
        Res.string.project_settings_error,
        error.reason.ifEmpty { stringResource(Res.string.error_reason_fallback) },
    )
}

@Composable
private fun ShareCodeCard(code: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(18.dp))
            .padding(Spacing.SP5),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.project_settings_share_code_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.SP1))
            Text(
                text = code.ifEmpty {
                    stringResource(Res.string.project_settings_share_code_placeholder)
                },
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        TextButton(onClick = onCopy, enabled = code.isNotEmpty()) {
            Text(
                text = stringResource(Res.string.action_copy),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    emphasised: Boolean = false,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(Spacing.SP4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val alpha = if (enabled) 1f else 0.45f
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = (
                    if (emphasised) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                ).copy(alpha = alpha),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            )
        }
    }
}

@Composable
private fun ConfirmingDialog(
    action: ConfirmingAction,
    isProcessing: Boolean,
    onIntent: (ProjectSettingsIntent) -> Unit,
) {
    when (action) {
        ConfirmingAction.Leave -> AlertDialog(
            onDismissRequest = { onIntent(ProjectSettingsIntent.CancelCurrentAction) },
            confirmButton = {
                Button(
                    onClick = { onIntent(ProjectSettingsIntent.ConfirmCurrentAction) },
                    enabled = !isProcessing,
                ) { Text(stringResource(Res.string.action_leave)) }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(ProjectSettingsIntent.CancelCurrentAction) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
            title = { Text(stringResource(Res.string.project_settings_leave_dialog_title)) },
            text = { Text(stringResource(Res.string.project_settings_leave_dialog_body)) },
        )
        is ConfirmingAction.Delete -> AlertDialog(
            onDismissRequest = { onIntent(ProjectSettingsIntent.CancelCurrentAction) },
            confirmButton = {
                Button(
                    onClick = { onIntent(ProjectSettingsIntent.ConfirmCurrentAction) },
                    enabled = action.canConfirm && !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(
                        if (isProcessing) stringResource(Res.string.project_settings_delete_action_in_progress)
                        else stringResource(Res.string.action_delete),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onIntent(ProjectSettingsIntent.CancelCurrentAction) },
                    enabled = !isProcessing,
                ) { Text(stringResource(Res.string.action_cancel)) }
            },
            title = { Text(stringResource(Res.string.project_settings_delete_dialog_title)) },
            text = {
                Column {
                    Text(text = stringResource(Res.string.project_settings_delete_dialog_body))
                    Spacer(Modifier.height(Spacing.SP3))
                    Text(
                        text = stringResource(Res.string.project_settings_delete_dialog_typed_hint),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(Modifier.height(Spacing.SP2))
                    OutlinedTextField(
                        value = action.typed,
                        onValueChange = {
                            onIntent(ProjectSettingsIntent.DeleteConfirmTextChanged(it))
                        },
                        singleLine = true,
                        isError = action.typed.isNotEmpty() && !action.canConfirm,
                        supportingText = {
                            if (action.typed.isNotEmpty() && !action.canConfirm) {
                                Text(
                                    text = stringResource(Res.string.project_settings_delete_dialog_typed_error),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )
    }
}

@Preview
@Composable
private fun ProjectSettingsScreenPreview() {
    ColisionTheme {
        ProjectSettingsScreen(
            state = ProjectSettingsState(
                projectName = "Conseil municipal de Saint-Machin",
                shareCode = "KQ7H2P",
            ),
            snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() },
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun ProjectSettingsScreenDeleteDialogPreview() {
    ColisionTheme {
        ProjectSettingsScreen(
            state = ProjectSettingsState(
                projectName = "Conseil municipal de Saint-Machin",
                shareCode = "KQ7H2P",
                confirming = ConfirmingAction.Delete(typed = "suppri"),
            ),
            snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() },
            onIntent = {},
        )
    }
}
