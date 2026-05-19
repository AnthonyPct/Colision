package com.anthooop.colision.feature.onboarding.joinidentity

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.core.design.Spacing

@Composable
fun JoinIdentityScreen(
    state: JoinIdentityState,
    onIntent: (JoinIdentityIntent) -> Unit,
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
            TextButton(onClick = { onIntent(JoinIdentityIntent.BackTapped) }) {
                Text(
                    text = "Retour",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            text = "Qui es-tu ?",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP2))
        Text(
            text = "Choisis ton nom dans la liste. Tu n'y es pas ? Ajoute-toi en bas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP5))

        OutlinedTextField(
            value = state.query,
            onValueChange = { onIntent(JoinIdentityIntent.QueryChanged(it)) },
            placeholder = { Text("Rechercher ton nom") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(percent = 50),
        )

        Spacer(Modifier.height(Spacing.SP4))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
        ) {
            items(state.filteredMembers, key = { it.id }) { member ->
                MemberRow(
                    member = member,
                    selected = state.selectedMemberId == member.id,
                    onClick = { onIntent(JoinIdentityIntent.MemberSelected(member.id)) },
                )
            }
            item {
                AddMyselfRow(onClick = { onIntent(JoinIdentityIntent.AddTapped) })
            }
        }

        Spacer(Modifier.height(Spacing.SP4))

        Button(
            onClick = { onIntent(JoinIdentityIntent.ConfirmTapped) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.canConfirm,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(horizontal = Spacing.SP6),
        ) {
            Text(
                text = "C'est moi",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }

    state.addNewIdentity?.let { adding ->
        AlertDialog(
            onDismissRequest = { onIntent(JoinIdentityIntent.AddCancelled) },
            confirmButton = {
                Button(
                    onClick = { onIntent(JoinIdentityIntent.AddConfirmed) },
                    enabled = adding.canSubmit && !state.isSubmitting,
                ) { Text("Continuer") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(JoinIdentityIntent.AddCancelled) }) { Text("Annuler") }
            },
            title = { Text("Ton nom") },
            text = {
                OutlinedTextField(
                    value = adding.name,
                    onValueChange = { onIntent(JoinIdentityIntent.AddNameChanged(it)) },
                    placeholder = { Text("Prénom (et nom optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
    state.pendingError?.let { msg ->
        AlertDialog(
            onDismissRequest = { onIntent(JoinIdentityIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(JoinIdentityIntent.ErrorDismissed) }) { Text("OK") }
            },
            title = { Text("Erreur") },
            text = { Text(msg) },
        )
    }
}

@Composable
private fun MemberRow(member: MemberEntity, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarBubble(initials = initialsFor(member.displayName))
        Spacer(Modifier.size(Spacing.SP3))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(percent = 50)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun AddMyselfRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+ Je m'ajoute moi-même",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AvatarBubble(initials: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(percent = 50)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun initialsFor(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}
