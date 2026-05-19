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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anthooop.colision.core.design.Spacing

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
                text = state.projectName.ifEmpty { "Mon projet" },
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(Spacing.SP4))

            ShareCodeCard(
                code = state.shareCode,
                onCopy = { onIntent(ProjectSettingsIntent.CopyShareCode) },
            )

            Spacer(Modifier.height(Spacing.SP8))

            SectionLabel("Gestion")
            Spacer(Modifier.height(Spacing.SP3))
            SettingsRow(
                title = "Commissions",
                subtitle = "Ajouter, renommer, supprimer",
                onClick = { onIntent(ProjectSettingsIntent.OpenCommissions) },
            )
            Spacer(Modifier.height(Spacing.SP2))
            SettingsRow(
                title = "Membres",
                subtitle = "Ajouter et assigner aux commissions",
                onClick = { onIntent(ProjectSettingsIntent.OpenMembers) },
            )

            Spacer(Modifier.height(Spacing.SP8))

            SectionLabel("Zone sensible")
            Spacer(Modifier.height(Spacing.SP3))
            SettingsRow(
                title = "Quitter ce projet",
                subtitle = "Je retire mon identité du projet",
                onClick = { onIntent(ProjectSettingsIntent.LeaveTapped) },
                emphasised = false,
            )
            Spacer(Modifier.height(Spacing.SP2))
            SettingsRow(
                title = "Supprimer le projet",
                subtitle = "Action définitive pour tous les membres",
                onClick = { onIntent(ProjectSettingsIntent.DeleteTapped) },
                emphasised = true,
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
    state.pendingError?.let { msg ->
        AlertDialog(
            onDismissRequest = { onIntent(ProjectSettingsIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(ProjectSettingsIntent.ErrorDismissed) }) {
                    Text("OK")
                }
            },
            title = { Text("Erreur") },
            text = { Text(msg) },
        )
    }
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
                text = "Code de partage",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.SP1))
            Text(
                text = code.ifEmpty { "------" },
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        TextButton(onClick = onCopy, enabled = code.isNotEmpty()) {
            Text("Copier", style = MaterialTheme.typography.labelLarge)
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
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (emphasised) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                ) { Text("Quitter") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(ProjectSettingsIntent.CancelCurrentAction) }) {
                    Text("Annuler")
                }
            },
            title = { Text("Quitter le projet ?") },
            text = {
                Text(
                    text = "Tu vas quitter ce projet. Ton historique d'arbitrage sera effacé. Continuer ?",
                )
            },
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
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(ProjectSettingsIntent.CancelCurrentAction) }) {
                    Text("Annuler")
                }
            },
            title = { Text("Supprimer le projet ?") },
            text = {
                Column {
                    Text(
                        text = "Cette action supprime DÉFINITIVEMENT le projet et toutes ses données pour tous ses membres.",
                    )
                    Spacer(Modifier.height(Spacing.SP3))
                    Text(
                        text = "Pour confirmer, tape « supprimer ».",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(Modifier.height(Spacing.SP2))
                    OutlinedTextField(
                        value = action.typed,
                        onValueChange = {
                            onIntent(ProjectSettingsIntent.DeleteConfirmTextChanged(it))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )
    }
}
