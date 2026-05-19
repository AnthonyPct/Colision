package com.anthooop.colision.feature.onboarding.joincommissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.design.Spacing

@Composable
fun JoinCommissionsScreen(
    state: JoinCommissionsState,
    onIntent: (JoinCommissionsIntent) -> Unit,
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
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onIntent(JoinCommissionsIntent.BackTapped) }) {
                Text(
                    text = "Retour",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            text = "Tes commissions",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP2))
        Text(
            text = "Coche celles dont tu es membre. Tu pourras les modifier plus tard.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP4))

        InfoBanner(
            text = "Tu peux ajuster cette sélection à tout moment depuis les réglages.",
        )

        Spacer(Modifier.height(Spacing.SP4))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
        ) {
            items(state.commissions, key = { it.id }) { commission ->
                CommissionRow(
                    commission = commission,
                    checked = commission.id in state.checkedIds,
                    onToggle = {
                        onIntent(JoinCommissionsIntent.CommissionToggled(commission.id))
                    },
                )
            }
        }

        Spacer(Modifier.height(Spacing.SP4))

        Button(
            onClick = { onIntent(JoinCommissionsIntent.ContinueTapped) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.canSubmit,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(horizontal = Spacing.SP6),
        ) {
            val n = state.checkedIds.size
            val plural = if (n > 1) "cochées" else "cochée"
            Text(
                text = "Continuer ($n $plural)",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }

    state.pendingError?.let { msg ->
        AlertDialog(
            onDismissRequest = { onIntent(JoinCommissionsIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(JoinCommissionsIntent.ErrorDismissed) }) { Text("OK") }
            },
            title = { Text("Erreur") },
            text = { Text(msg) },
        )
    }
}

@Composable
private fun InfoBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp))
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun CommissionRow(commission: CommissionEntity, checked: Boolean, onToggle: () -> Unit) {
    val (border, background) = if (checked) {
        1.5.dp to MaterialTheme.colorScheme.surface
    } else {
        1.dp to MaterialTheme.colorScheme.background
    }
    val borderColor = if (checked) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(18.dp))
            .border(border, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = commission.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (checked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(6.dp),
                )
                .border(
                    width = 1.dp,
                    color = if (checked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(6.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
