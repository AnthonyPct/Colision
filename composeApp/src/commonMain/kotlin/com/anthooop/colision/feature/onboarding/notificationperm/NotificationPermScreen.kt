package com.anthooop.colision.feature.onboarding.notificationperm

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.notif_perm_action_activate
import colision.composeapp.generated.resources.notif_perm_action_later
import colision.composeapp.generated.resources.notif_perm_card_arbitration_supporting
import colision.composeapp.generated.resources.notif_perm_card_arbitration_title
import colision.composeapp.generated.resources.notif_perm_card_conflict_supporting
import colision.composeapp.generated.resources.notif_perm_card_conflict_title
import colision.composeapp.generated.resources.notif_perm_card_meeting_supporting
import colision.composeapp.generated.resources.notif_perm_card_meeting_title
import colision.composeapp.generated.resources.notif_perm_subtitle
import colision.composeapp.generated.resources.notif_perm_title
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotificationPermScreen(
    state: NotificationPermState,
    onIntent: (NotificationPermIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = Spacing.SP6,
                end = Spacing.SP6,
                top = Spacing.SP10 + safe.calculateTopPadding(),
                bottom = Spacing.SP8 + safe.calculateBottomPadding(),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(22.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "🔔",
                style = MaterialTheme.typography.displayMedium,
            )
        }

        Spacer(Modifier.height(Spacing.SP6))

        Text(
            text = stringResource(Res.string.notif_perm_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP3))
        Text(
            text = stringResource(Res.string.notif_perm_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP6))

        InfoCard(
            title = stringResource(Res.string.notif_perm_card_conflict_title),
            supporting = stringResource(Res.string.notif_perm_card_conflict_supporting),
        )
        Spacer(Modifier.height(Spacing.SP3))
        InfoCard(
            title = stringResource(Res.string.notif_perm_card_meeting_title),
            supporting = stringResource(Res.string.notif_perm_card_meeting_supporting),
        )
        Spacer(Modifier.height(Spacing.SP3))
        InfoCard(
            title = stringResource(Res.string.notif_perm_card_arbitration_title),
            supporting = stringResource(Res.string.notif_perm_card_arbitration_supporting),
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onIntent(NotificationPermIntent.ActivateTapped) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(horizontal = Spacing.SP6),
            enabled = !state.isRequesting,
        ) {
            Text(
                text = stringResource(Res.string.notif_perm_action_activate),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(Spacing.SP2))
        TextButton(
            onClick = { onIntent(NotificationPermIntent.LaterTapped) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isRequesting,
        ) {
            Text(
                text = stringResource(Res.string.notif_perm_action_later),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, supporting: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(Spacing.SP4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "●", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.size(Spacing.SP3))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun NotificationPermScreenPreview() {
    ColisionTheme {
        NotificationPermScreen(state = NotificationPermState(), onIntent = {})
    }
}
