package com.anthooop.colision.feature.onboarding.projectcommissions

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_add
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.action_continue
import colision.composeapp.generated.resources.action_delete
import colision.composeapp.generated.resources.create_commissions_empty
import colision.composeapp.generated.resources.create_commissions_error
import colision.composeapp.generated.resources.create_commissions_input_placeholder
import colision.composeapp.generated.resources.create_commissions_remove_cd
import colision.composeapp.generated.resources.create_commissions_subtitle
import colision.composeapp.generated.resources.create_commissions_title
import colision.composeapp.generated.resources.error_reason_fallback
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateProjectCommissionsScreen(
    state: CreateProjectCommissionsState,
    onIntent: (CreateProjectCommissionsIntent) -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.SP6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onIntent(CreateProjectCommissionsIntent.BackTapped) },
                contentPadding = PaddingValues(horizontal = Spacing.SP3),
            ) {
                Text(
                    text = stringResource(Res.string.action_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            text = stringResource(Res.string.create_commissions_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP3))
        Text(
            text = stringResource(Res.string.create_commissions_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.SP3),
        ) {
            OutlinedTextField(
                value = state.draftName,
                onValueChange = { onIntent(CreateProjectCommissionsIntent.DraftNameChanged(it)) },
                placeholder = {
                    Text(
                        text = stringResource(Res.string.create_commissions_input_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onIntent(CreateProjectCommissionsIntent.AddTapped) },
                ),
            )
            FilledTonalButton(
                onClick = { onIntent(CreateProjectCommissionsIntent.AddTapped) },
                enabled = state.canAdd,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = stringResource(Res.string.action_add),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (state.pendingError != null) {
            Spacer(Modifier.height(Spacing.SP3))
            Text(
                text = errorMessage(state.pendingError),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(Spacing.SP4))

        if (state.commissions.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(Spacing.SP4),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.create_commissions_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
            ) {
                items(state.commissions, key = { it.id }) { commission ->
                    CommissionRow(
                        commission = commission,
                        onRemove = {
                            onIntent(CreateProjectCommissionsIntent.RemoveTapped(commission.id))
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.SP4))

        Button(
            onClick = { onIntent(CreateProjectCommissionsIntent.ContinueTapped) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(horizontal = Spacing.SP6),
        ) {
            Text(
                text = stringResource(Res.string.action_continue),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CommissionRow(
    commission: CommissionEntity,
    onRemove: () -> Unit,
) {
    val removeLabel = stringResource(Res.string.create_commissions_remove_cd, commission.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(start = Spacing.SP4, top = Spacing.SP2, bottom = Spacing.SP2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = commission.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onRemove,
            modifier = Modifier.semantics { contentDescription = removeLabel },
        ) {
            Text(
                text = stringResource(Res.string.action_delete),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun errorMessage(error: CreateProjectCommissionsError): String = when (error) {
    is CreateProjectCommissionsError.Network -> stringResource(
        Res.string.create_commissions_error,
        error.reason.ifEmpty { stringResource(Res.string.error_reason_fallback) },
    )
}

@Preview
@Composable
private fun CreateProjectCommissionsScreenPreview() {
    ColisionTheme {
        CreateProjectCommissionsScreen(
            state = CreateProjectCommissionsState(
                draftName = "Travaux",
                commissions = listOf(
                    CommissionEntity("c1", "p1", "Jeunesse", "", ""),
                    CommissionEntity("c2", "p1", "Sport", "", ""),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun CreateProjectCommissionsScreenEmptyPreview() {
    ColisionTheme {
        CreateProjectCommissionsScreen(
            state = CreateProjectCommissionsState(),
            onIntent = {},
        )
    }
}
