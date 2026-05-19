package com.anthooop.colision.feature.projecthub.members

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.action_cancel
import colision.composeapp.generated.resources.action_add
import colision.composeapp.generated.resources.action_ok
import colision.composeapp.generated.resources.dialog_error_title
import colision.composeapp.generated.resources.error_reason_fallback
import colision.composeapp.generated.resources.members_list_action_add
import colision.composeapp.generated.resources.members_list_dialog_placeholder
import colision.composeapp.generated.resources.members_list_dialog_title
import colision.composeapp.generated.resources.members_list_empty
import colision.composeapp.generated.resources.members_list_error_add
import colision.composeapp.generated.resources.members_list_row_no_commission
import colision.composeapp.generated.resources.members_list_title
import colision.composeapp.generated.resources.write_offline_message
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.core.design.rememberOfflineGate
import org.jetbrains.compose.resources.stringResource

@Composable
fun MembersListScreen(
    state: MembersListState,
    onIntent: (MembersListIntent) -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.SP3, vertical = Spacing.SP2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onIntent(MembersListIntent.BackTapped) }) {
                Text(
                    text = stringResource(Res.string.action_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.members_list_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            val offlineGate = rememberOfflineGate(stringResource(Res.string.write_offline_message))
            TextButton(onClick = { offlineGate.run { onIntent(MembersListIntent.AddTapped) } }) {
                Text(
                    text = stringResource(Res.string.members_list_action_add),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (offlineGate.isOnline) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (state.rows.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(Spacing.SP6),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.members_list_empty),
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
                items(state.rows, key = { it.member.id }) { row ->
                    MemberRowItem(row, onClick = { onIntent(MembersListIntent.MemberTapped(row.member.id)) })
                }
            }
        }
    }

    state.addingMember?.let { adding ->
        AlertDialog(
            onDismissRequest = { onIntent(MembersListIntent.AddCancelled) },
            confirmButton = {
                Button(
                    onClick = { onIntent(MembersListIntent.AddConfirmed) },
                    enabled = adding.canConfirm,
                ) { Text(stringResource(Res.string.action_add)) }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(MembersListIntent.AddCancelled) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
            title = { Text(stringResource(Res.string.members_list_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = adding.name,
                    onValueChange = { onIntent(MembersListIntent.AddNameChanged(it)) },
                    placeholder = { Text(stringResource(Res.string.members_list_dialog_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }

    state.pendingError?.let { error ->
        AlertDialog(
            onDismissRequest = { onIntent(MembersListIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(MembersListIntent.ErrorDismissed) }) {
                    Text(stringResource(Res.string.action_ok))
                }
            },
            title = { Text(stringResource(Res.string.dialog_error_title)) },
            text = { Text(membersListErrorMessage(error)) },
        )
    }
}

@Composable
private fun membersListErrorMessage(error: MembersListError): String = when (error) {
    is MembersListError.Add -> stringResource(
        Res.string.members_list_error_add,
        error.reason.ifEmpty { stringResource(Res.string.error_reason_fallback) },
    )
}

@Composable
private fun MemberRowItem(row: MemberRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(Spacing.SP4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.member.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (row.commissionLabels.isEmpty()) {
                    stringResource(Res.string.members_list_row_no_commission)
                } else {
                    row.commissionLabels.joinToString(" · ")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun MembersListScreenPreview() {
    ColisionTheme {
        MembersListScreen(
            state = MembersListState(
                isLoading = false,
                rows = listOf(
                    MemberRow(
                        MemberEntity("m1", "p1", null, "Antoine Durand", "", ""),
                        listOf("Urbanisme", "Voirie"),
                    ),
                    MemberRow(
                        MemberEntity("m2", "p1", "d1", "Sophie Picquet", "", ""),
                        listOf("Jeunesse", "École"),
                    ),
                    MemberRow(
                        MemberEntity("m3", "p1", null, "Camille Roux", "", ""),
                        emptyList(),
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
