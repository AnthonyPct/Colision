package com.anthooop.colision.feature.projecthub.commissions

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_add
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.action_cancel
import colision.composeapp.generated.resources.action_delete
import colision.composeapp.generated.resources.action_modify
import colision.composeapp.generated.resources.action_ok
import colision.composeapp.generated.resources.action_save
import colision.composeapp.generated.resources.commissions_list_action_add
import colision.composeapp.generated.resources.commissions_list_dialog_create_title
import colision.composeapp.generated.resources.commissions_list_dialog_delete_body
import colision.composeapp.generated.resources.commissions_list_dialog_delete_title
import colision.composeapp.generated.resources.commissions_list_dialog_placeholder
import colision.composeapp.generated.resources.commissions_list_dialog_rename_title
import colision.composeapp.generated.resources.commissions_list_empty
import colision.composeapp.generated.resources.commissions_list_error
import colision.composeapp.generated.resources.commissions_list_title
import colision.composeapp.generated.resources.dialog_error_title
import colision.composeapp.generated.resources.error_reason_fallback
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommissionsListScreen(
    state: CommissionsListState,
    onIntent: (CommissionsListIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = safe.calculateTopPadding(),
                bottom = safe.calculateBottomPadding(),
            ),
    ) {
        TopBar(
            title = stringResource(Res.string.commissions_list_title),
            onBack = { onIntent(CommissionsListIntent.BackTapped) },
            actionLabel = stringResource(Res.string.commissions_list_action_add),
            onAction = { onIntent(CommissionsListIntent.AddTapped) },
        )

        if (state.commissions.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(Spacing.SP6),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.commissions_list_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.SP5, vertical = Spacing.SP4),
                verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
            ) {
                items(state.commissions, key = { it.id }) { commission ->
                    CommissionRow(
                        commission = commission,
                        onRename = { onIntent(CommissionsListIntent.RenameTapped(commission.id, commission.name)) },
                        onDelete = { onIntent(CommissionsListIntent.DeleteTapped(commission.id, commission.name)) },
                    )
                }
            }
        }
    }

    state.editing?.let { editing ->
        EditingDialog(
            editing = editing,
            onIntent = onIntent,
        )
    }

    state.pendingError?.let { error ->
        AlertDialog(
            onDismissRequest = { onIntent(CommissionsListIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(CommissionsListIntent.ErrorDismissed) }) {
                    Text(stringResource(Res.string.action_ok))
                }
            },
            title = { Text(stringResource(Res.string.dialog_error_title)) },
            text = { Text(commissionsListErrorMessage(error)) },
        )
    }
}

@Composable
private fun commissionsListErrorMessage(error: CommissionsListError): String = when (error) {
    is CommissionsListError.Network -> stringResource(
        Res.string.commissions_list_error,
        error.reason.ifEmpty { stringResource(Res.string.error_reason_fallback) },
    )
}

@Composable
private fun TopBar(
    title: String,
    onBack: () -> Unit,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.SP3, vertical = Spacing.SP2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = stringResource(Res.string.action_back),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            Spacer(Modifier.width(80.dp))
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun CommissionRow(
    commission: CommissionEntity,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(Spacing.SP4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = commission.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRename) {
            Text(
                text = stringResource(Res.string.action_modify),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        TextButton(onClick = onDelete) {
            Text(
                text = stringResource(Res.string.action_delete),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun EditingDialog(
    editing: EditingState,
    onIntent: (CommissionsListIntent) -> Unit,
) {
    when (editing) {
        is EditingState.Create, is EditingState.Rename -> {
            val name = when (editing) {
                is EditingState.Create -> editing.name
                is EditingState.Rename -> editing.name
                else -> ""
            }
            val title = when (editing) {
                is EditingState.Create -> stringResource(Res.string.commissions_list_dialog_create_title)
                is EditingState.Rename -> stringResource(Res.string.commissions_list_dialog_rename_title)
                else -> ""
            }
            val confirmLabel = when (editing) {
                is EditingState.Create -> stringResource(Res.string.action_add)
                is EditingState.Rename -> stringResource(Res.string.action_save)
                else -> ""
            }
            AlertDialog(
                onDismissRequest = { onIntent(CommissionsListIntent.EditorCancelled) },
                confirmButton = {
                    Button(
                        onClick = { onIntent(CommissionsListIntent.EditorConfirmed) },
                        enabled = name.trim().length >= 2,
                    ) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(CommissionsListIntent.EditorCancelled) }) {
                        Text(stringResource(Res.string.action_cancel))
                    }
                },
                title = { Text(title) },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { onIntent(CommissionsListIntent.EditorNameChanged(it)) },
                        placeholder = {
                            Text(stringResource(Res.string.commissions_list_dialog_placeholder))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }
        is EditingState.ConfirmDelete -> {
            AlertDialog(
                onDismissRequest = { onIntent(CommissionsListIntent.EditorCancelled) },
                confirmButton = {
                    Button(
                        onClick = { onIntent(CommissionsListIntent.EditorConfirmed) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) { Text(stringResource(Res.string.action_delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(CommissionsListIntent.EditorCancelled) }) {
                        Text(stringResource(Res.string.action_cancel))
                    }
                },
                title = {
                    Text(
                        stringResource(
                            Res.string.commissions_list_dialog_delete_title,
                            editing.name,
                        ),
                    )
                },
                text = {
                    Text(stringResource(Res.string.commissions_list_dialog_delete_body))
                },
            )
        }
    }
}

@Preview
@Composable
private fun CommissionsListScreenPreview() {
    ColisionTheme {
        CommissionsListScreen(
            state = CommissionsListState(
                isLoading = false,
                commissions = listOf(
                    CommissionEntity("c1", "p1", "Jeunesse", "", ""),
                    CommissionEntity("c2", "p1", "École", "", ""),
                    CommissionEntity("c3", "p1", "Urbanisme", "", ""),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun CommissionsListScreenEmptyPreview() {
    ColisionTheme {
        CommissionsListScreen(
            state = CommissionsListState(isLoading = false),
            onIntent = {},
        )
    }
}

