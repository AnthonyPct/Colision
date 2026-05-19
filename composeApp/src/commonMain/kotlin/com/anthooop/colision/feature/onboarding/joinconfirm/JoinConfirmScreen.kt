package com.anthooop.colision.feature.onboarding.joinconfirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.join_confirm_action_confirm
import colision.composeapp.generated.resources.join_confirm_action_wrong
import colision.composeapp.generated.resources.join_confirm_commissions_count
import colision.composeapp.generated.resources.join_confirm_eyebrow
import colision.composeapp.generated.resources.join_confirm_loading_placeholder
import colision.composeapp.generated.resources.join_confirm_section_commissions
import colision.composeapp.generated.resources.join_confirm_section_commissions_empty
import colision.composeapp.generated.resources.join_confirm_section_members
import colision.composeapp.generated.resources.join_confirm_section_members_supporting
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun JoinConfirmScreen(
    state: JoinConfirmState,
    onIntent: (JoinConfirmIntent) -> Unit,
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
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.SP2),
        ) {
            TextButton(onClick = { onIntent(JoinConfirmIntent.BackTapped) }) {
                Text(
                    text = stringResource(Res.string.action_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            text = stringResource(Res.string.join_confirm_eyebrow),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.SP2))
        Text(
            text = state.projectName.ifEmpty {
                stringResource(Res.string.join_confirm_loading_placeholder)
            },
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP2))
        Text(
            text = pluralStringResource(
                Res.plurals.join_confirm_commissions_count,
                state.commissions.size,
                state.commissions.size,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP6))

        ProjectPreviewCard(commissions = state.commissions)

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onIntent(JoinConfirmIntent.ConfirmTapped) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(horizontal = Spacing.SP6),
            enabled = state.projectName.isNotEmpty(),
        ) {
            Text(
                text = stringResource(Res.string.join_confirm_action_confirm),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(Spacing.SP2))
        TextButton(
            onClick = { onIntent(JoinConfirmIntent.WrongProjectTapped) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(Res.string.join_confirm_action_wrong),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProjectPreviewCard(commissions: List<CommissionEntity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(18.dp)),
    ) {
        Column(modifier = Modifier.padding(Spacing.SP4)) {
            Text(
                text = stringResource(Res.string.join_confirm_section_commissions),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.SP2))
            if (commissions.isEmpty()) {
                Text(
                    text = stringResource(Res.string.join_confirm_section_commissions_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SP2),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
                ) {
                    commissions.forEach { c ->
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(percent = 50),
                                )
                                .padding(horizontal = Spacing.SP3, vertical = Spacing.SP1),
                        ) {
                            Text(
                                text = c.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(modifier = Modifier.padding(Spacing.SP4)) {
            Text(
                text = stringResource(Res.string.join_confirm_section_members),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.SP2))
            Text(
                text = stringResource(Res.string.join_confirm_section_members_supporting),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun JoinConfirmScreenPreview() {
    ColisionTheme {
        JoinConfirmScreen(
            state = JoinConfirmState(
                projectName = "Conseil municipal de Saint-Machin",
                commissions = listOf(
                    CommissionEntity("c1", "p1", "Jeunesse", "", ""),
                    CommissionEntity("c2", "p1", "École", "", ""),
                    CommissionEntity("c3", "p1", "Urbanisme", "", ""),
                ),
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}
