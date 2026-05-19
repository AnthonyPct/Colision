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
import androidx.compose.ui.tooling.preview.Preview
import com.anthooop.colision.app.ColisionTheme
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.action_cancel
import colision.composeapp.generated.resources.action_continue
import colision.composeapp.generated.resources.action_ok
import colision.composeapp.generated.resources.dialog_error_title
import colision.composeapp.generated.resources.error_reason_fallback
import colision.composeapp.generated.resources.join_identity_action_add_self
import colision.composeapp.generated.resources.join_identity_action_confirm
import colision.composeapp.generated.resources.join_identity_avatar_fallback
import colision.composeapp.generated.resources.join_identity_dialog_placeholder
import colision.composeapp.generated.resources.join_identity_dialog_title
import colision.composeapp.generated.resources.join_identity_error_add
import colision.composeapp.generated.resources.join_identity_error_claim
import colision.composeapp.generated.resources.join_identity_error_session_missing
import colision.composeapp.generated.resources.join_identity_search_placeholder
import colision.composeapp.generated.resources.join_identity_subtitle
import colision.composeapp.generated.resources.join_identity_title
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

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
                    text = stringResource(Res.string.action_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            text = stringResource(Res.string.join_identity_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP2))
        Text(
            text = stringResource(Res.string.join_identity_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP5))

        OutlinedTextField(
            value = state.query,
            onValueChange = { onIntent(JoinIdentityIntent.QueryChanged(it)) },
            placeholder = { Text(stringResource(Res.string.join_identity_search_placeholder)) },
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
                text = stringResource(Res.string.join_identity_action_confirm),
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
                ) { Text(stringResource(Res.string.action_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(JoinIdentityIntent.AddCancelled) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
            title = { Text(stringResource(Res.string.join_identity_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = adding.name,
                    onValueChange = { onIntent(JoinIdentityIntent.AddNameChanged(it)) },
                    placeholder = {
                        Text(stringResource(Res.string.join_identity_dialog_placeholder))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
    state.pendingError?.let { error ->
        AlertDialog(
            onDismissRequest = { onIntent(JoinIdentityIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(JoinIdentityIntent.ErrorDismissed) }) {
                    Text(stringResource(Res.string.action_ok))
                }
            },
            title = { Text(stringResource(Res.string.dialog_error_title)) },
            text = { Text(joinIdentityErrorMessage(error)) },
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
            text = stringResource(Res.string.join_identity_action_add_self),
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

@Composable
private fun initialsFor(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> stringResource(Res.string.join_identity_avatar_fallback)
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

@Composable
private fun joinIdentityErrorMessage(error: JoinIdentityError): String = when (error) {
    JoinIdentityError.SessionMissing ->
        stringResource(Res.string.join_identity_error_session_missing)
    is JoinIdentityError.Claim -> stringResource(
        Res.string.join_identity_error_claim,
        error.reason.ifEmpty { stringResource(Res.string.error_reason_fallback) },
    )
    is JoinIdentityError.Add -> stringResource(
        Res.string.join_identity_error_add,
        error.reason.ifEmpty { stringResource(Res.string.error_reason_fallback) },
    )
}

@Preview
@Composable
private fun JoinIdentityScreenPreview() {
    ColisionTheme {
        JoinIdentityScreen(
            state = JoinIdentityState(
                isLoading = false,
                members = listOf(
                    MemberEntity("m1", "p1", null, "Antoine Durand", "", ""),
                    MemberEntity("m2", "p1", null, "Sophie Picquet", "", ""),
                    MemberEntity("m3", "p1", null, "Camille Roux", "", ""),
                ),
                selectedMemberId = "m2",
            ),
            onIntent = {},
        )
    }
}
