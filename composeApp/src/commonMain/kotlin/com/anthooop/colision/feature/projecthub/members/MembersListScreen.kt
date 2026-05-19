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
import com.anthooop.colision.core.design.Spacing

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
                Text("Retour", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.weight(1f))
            Text("Membres", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onIntent(MembersListIntent.AddTapped) }) {
                Text("+ Ajouter", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (state.rows.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(Spacing.SP6),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Aucun membre pour l'instant.\nAjoute-en avec + Ajouter.",
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
                ) { Text("Ajouter") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(MembersListIntent.AddCancelled) }) { Text("Annuler") }
            },
            title = { Text("Nouveau membre") },
            text = {
                OutlinedTextField(
                    value = adding.name,
                    onValueChange = { onIntent(MembersListIntent.AddNameChanged(it)) },
                    placeholder = { Text("Prénom (et nom optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }

    state.pendingError?.let { msg ->
        AlertDialog(
            onDismissRequest = { onIntent(MembersListIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(MembersListIntent.ErrorDismissed) }) { Text("OK") }
            },
            title = { Text("Erreur") },
            text = { Text(msg) },
        )
    }
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
                text = if (row.commissionLabels.isEmpty()) "Aucune commission"
                else row.commissionLabels.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
