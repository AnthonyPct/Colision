package com.anthooop.colision.feature.meeting.conflicts

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.conflicts_action_create_anyway_sub
import colision.composeapp.generated.resources.conflicts_action_create_anyway_title
import colision.composeapp.generated.resources.conflicts_action_postpone_sub
import colision.composeapp.generated.resources.conflicts_action_postpone_title
import colision.composeapp.generated.resources.conflicts_action_suggestions_sub
import colision.composeapp.generated.resources.conflicts_action_suggestions_title
import colision.composeapp.generated.resources.conflicts_actions_label
import colision.composeapp.generated.resources.conflicts_count
import colision.composeapp.generated.resources.conflicts_member_count
import colision.composeapp.generated.resources.conflicts_section_label
import colision.composeapp.generated.resources.conflicts_subtitle
import colision.composeapp.generated.resources.conflicts_time_format
import colision.composeapp.generated.resources.conflicts_title
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.feature.agenda.agenda.extractTime
import com.anthooop.colision.feature.meeting.data.ConflictRow
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConflictsScreen(
    state: ConflictsState,
    onIntent: (ConflictsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safe.calculateTopPadding(), bottom = safe.calculateBottomPadding()),
    ) {
        TopBar(onBack = { onIntent(ConflictsIntent.BackTapped) })

        val byMember = state.conflicts.groupBy { it.memberId }
        val memberCount = byMember.size

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Spacing.SP5, vertical = Spacing.SP3),
            verticalArrangement = Arrangement.spacedBy(Spacing.SP3),
        ) {
            item { ConflictsHeader(memberCount = memberCount) }
            item {
                Text(
                    text = pluralStringResource(
                        Res.plurals.conflicts_member_count,
                        memberCount,
                        memberCount,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(state.conflicts, key = { it.memberId + ":" + it.meetingId }) { row ->
                ConflictRowItem(row = row)
            }
            item {
                Spacer(Modifier.height(Spacing.SP3))
                Text(
                    text = stringResource(Res.string.conflicts_actions_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(Spacing.SP2))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionRow(
                        title = stringResource(Res.string.conflicts_action_suggestions_title),
                        sub = stringResource(Res.string.conflicts_action_suggestions_sub),
                        emphasis = ActionEmphasis.Primary,
                        onClick = { onIntent(ConflictsIntent.SuggestionsTapped) },
                    )
                    ActionRow(
                        title = stringResource(Res.string.conflicts_action_postpone_title),
                        sub = stringResource(Res.string.conflicts_action_postpone_sub),
                        emphasis = ActionEmphasis.Neutral,
                        onClick = { onIntent(ConflictsIntent.PostponeTapped) },
                    )
                    ActionRow(
                        title = stringResource(Res.string.conflicts_action_create_anyway_title),
                        sub = stringResource(Res.string.conflicts_action_create_anyway_sub),
                        emphasis = ActionEmphasis.Danger,
                        enabled = !state.isCreatingAnyway,
                        onClick = { onIntent(ConflictsIntent.CreateAnywayTapped) },
                    )
                }
            }
        }
    }
}

private enum class ActionEmphasis { Primary, Neutral, Danger }

@Composable
private fun ActionRow(
    title: String,
    sub: String,
    emphasis: ActionEmphasis,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val borderColor = when (emphasis) {
        ActionEmphasis.Primary -> MaterialTheme.colorScheme.primary
        ActionEmphasis.Neutral -> MaterialTheme.colorScheme.outline
        ActionEmphasis.Danger -> MaterialTheme.colorScheme.outline
    }
    val titleColor = when (emphasis) {
        ActionEmphasis.Danger -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.SP3, vertical = Spacing.SP2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = stringResource(Res.string.action_back),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.conflicts_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(48.dp))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ConflictsHeader(memberCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(Spacing.SP4),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
    ) {
        Text(
            text = stringResource(Res.string.conflicts_count, memberCount),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(Res.string.conflicts_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
    Spacer(Modifier.height(Spacing.SP2))
    Text(
        text = stringResource(Res.string.conflicts_section_label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ConflictRowItem(row: ConflictRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = Spacing.SP3, vertical = Spacing.SP3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = row.memberDisplayName)
        Spacer(Modifier.width(Spacing.SP3))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = row.memberDisplayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(
                    Res.string.conflicts_time_format,
                    row.commissionName,
                    extractTime(row.meetingStartsAt),
                    extractTime(row.meetingEndsAt),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Avatar(name: String) {
    val initials = name.split(' ').take(2).joinToString("") { it.take(1) }.uppercase()
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials.ifBlank { "?" },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
