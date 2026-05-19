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
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.design.Spacing

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
            title = "Commissions",
            onBack = { onIntent(CommissionsListIntent.BackTapped) },
            actionLabel = "+ Ajouter",
            onAction = { onIntent(CommissionsListIntent.AddTapped) },
        )

        if (state.commissions.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(Spacing.SP6),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Aucune commission pour l'instant.\nAjoute-en une avec + Ajouter.",
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
                    Text("OK")
                }
            },
            title = { Text("Erreur") },
            text = { Text(error) },
        )
    }
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
                text = "Retour",
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
            Text("Modifier", style = MaterialTheme.typography.labelMedium)
        }
        TextButton(onClick = onDelete) {
            Text(
                text = "Supprimer",
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
            val (name, title, confirmLabel) = when (editing) {
                is EditingState.Create -> Triple(editing.name, "Nouvelle commission", "Ajouter")
                is EditingState.Rename -> Triple(editing.name, "Renommer la commission", "Enregistrer")
                else -> Triple("", "", "")
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
                        Text("Annuler")
                    }
                },
                title = { Text(title) },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { onIntent(CommissionsListIntent.EditorNameChanged(it)) },
                        placeholder = { Text("Ex. Jeunesse") },
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
                    ) { Text("Supprimer") }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(CommissionsListIntent.EditorCancelled) }) {
                        Text("Annuler")
                    }
                },
                title = { Text("Supprimer ${editing.name} ?") },
                text = {
                    Text(
                        text = "Toutes les réunions rattachées à cette commission seront également supprimées. Cette action est définitive.",
                    )
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

