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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.design.Spacing

@Composable
fun MemberCommissionsScreen(
    state: MemberCommissionsState,
    onIntent: (MemberCommissionsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = safe.calculateTopPadding(), bottom = safe.calculateBottomPadding()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.SP3, vertical = Spacing.SP2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onIntent(MemberCommissionsIntent.BackTapped) }) {
                Text(
                    text = "Retour",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(modifier = Modifier.padding(horizontal = Spacing.SP6, vertical = Spacing.SP4)) {
            Text(
                text = state.memberName.ifEmpty { "Membre" },
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.padding(top = Spacing.SP2))
            Text(
                text = "Coche les commissions auxquelles cette personne appartient.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (state.commissions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Pas encore de commission. Crée-en d'abord.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.SP5, vertical = Spacing.SP4),
                verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
            ) {
                items(state.commissions, key = { it.id }) { commission ->
                    AssignmentRow(
                        commission = commission,
                        assigned = commission.id in state.assignedIds,
                        onToggle = { onIntent(MemberCommissionsIntent.CommissionToggled(commission.id)) },
                    )
                }
            }
        }
    }

    state.pendingError?.let { msg ->
        AlertDialog(
            onDismissRequest = { onIntent(MemberCommissionsIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(MemberCommissionsIntent.ErrorDismissed) }) { Text("OK") }
            },
            title = { Text("Erreur") },
            text = { Text(msg) },
        )
    }
}

@Composable
private fun AssignmentRow(
    commission: CommissionEntity,
    assigned: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = assigned, onCheckedChange = { onToggle() })
        Spacer(Modifier.padding(start = Spacing.SP1))
        Text(
            text = commission.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
