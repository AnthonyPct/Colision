@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.anthooop.colision.feature.poll.polldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.action_cancel
import colision.composeapp.generated.resources.action_delete
import colision.composeapp.generated.resources.action_ok
import colision.composeapp.generated.resources.dialog_error_title
import colision.composeapp.generated.resources.poll_generic_error
import colision.composeapp.generated.resources.poll_detail_banner_closed
import colision.composeapp.generated.resources.poll_detail_banner_readonly
import colision.composeapp.generated.resources.poll_detail_banner_vote
import colision.composeapp.generated.resources.poll_detail_banner_voted
import colision.composeapp.generated.resources.poll_detail_change_vote
import colision.composeapp.generated.resources.poll_detail_created_by
import colision.composeapp.generated.resources.poll_detail_delete_confirm_body
import colision.composeapp.generated.resources.poll_detail_delete_confirm_title
import colision.composeapp.generated.resources.poll_detail_deleted_body
import colision.composeapp.generated.resources.poll_detail_deleted_title
import colision.composeapp.generated.resources.poll_detail_loading
import colision.composeapp.generated.resources.poll_detail_offline
import colision.composeapp.generated.resources.poll_detail_submit_vote
import colision.composeapp.generated.resources.poll_detail_total
import colision.composeapp.generated.resources.polls_public_pill
import colision.composeapp.generated.resources.polls_vote_count
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.feature.poll.pollslist.DeadlineChip
import com.anthooop.colision.feature.poll.pollslist.pollFullDate
import com.anthooop.colision.feature.poll.pollslist.rememberPollMonthNames
import com.anthooop.colision.feature.poll.pollslist.rememberPollWeekdayNames
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PollDetailScreen(
    state: PollDetailState,
    onIntent: (PollDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safe.calculateTopPadding(), bottom = safe.calculateBottomPadding()),
    ) {
        TopBar(
            showMenu = state.isCreator,
            onBack = { onIntent(PollDetailIntent.BackTapped) },
            onDelete = { onIntent(PollDetailIntent.DeleteTapped) },
        )

        when {
            state.isLoading -> Centered(stringResource(Res.string.poll_detail_loading))
            state.isDeleted -> DeletedState()
            else -> {
                Box(modifier = Modifier.weight(1f)) {
                    PollBody(state = state, onIntent = onIntent)
                }
                if (!state.isClosed && state.eligible) {
                    VoteBar(state = state, onIntent = onIntent)
                }
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onIntent(PollDetailIntent.DeleteDismissed) },
            title = { Text(stringResource(Res.string.poll_detail_delete_confirm_title)) },
            text = { Text(stringResource(Res.string.poll_detail_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { onIntent(PollDetailIntent.DeleteConfirmed) }) {
                    Text(
                        text = stringResource(Res.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(PollDetailIntent.DeleteDismissed) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { onIntent(PollDetailIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(PollDetailIntent.ErrorDismissed) }) {
                    Text(stringResource(Res.string.action_ok))
                }
            },
            title = { Text(stringResource(Res.string.dialog_error_title)) },
            text = { Text(stringResource(Res.string.poll_generic_error)) },
        )
    }
}

@Composable
private fun TopBar(showMenu: Boolean, onBack: () -> Unit, onDelete: () -> Unit) {
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
        if (showMenu) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        text = "⋯",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.action_delete),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            expanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun PollBody(state: PollDetailState, onIntent: (PollDetailIntent) -> Unit) {
    val months = rememberPollMonthNames()
    val weekdays = rememberPollWeekdayNames()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.SP5, vertical = Spacing.SP4),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP3),
    ) {
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (state.targetIsPublic) {
                    Pill(stringResource(Res.string.polls_public_pill))
                } else {
                    state.commissionNames.forEach { Pill(it) }
                }
                DeadlineChip(isClosed = state.isClosed, daysLeft = state.daysLeft)
            }
        }
        item {
            Text(
                text = state.question,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            state.creatorName?.let { creator ->
                Spacer(Modifier.height(Spacing.SP2))
                Text(
                    text = stringResource(
                        Res.string.poll_detail_created_by,
                        creator,
                        pollFullDate(state.closesAtIso, months, weekdays),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { EligibilityBanner(state = state) }
        items(state.options, key = { it.id }) { option ->
            OptionRow(
                option = option,
                showResults = state.showResults,
                selected = if (state.showResults) {
                    state.myVoteOptionId == option.id
                } else {
                    state.pendingOptionId == option.id
                },
                onTap = { onIntent(PollDetailIntent.OptionSelected(option.id)) },
            )
        }
        if (state.showResults) {
            item {
                Text(
                    text = stringResource(
                        Res.string.poll_detail_total,
                        pluralStringResource(Res.plurals.polls_vote_count, state.totalVotes, state.totalVotes),
                        state.eligibleCount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.SP2),
                )
            }
        }
    }
}

@Composable
private fun EligibilityBanner(state: PollDetailState) {
    val (bg, fg, text) = when {
        state.isClosed -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(Res.string.poll_detail_banner_closed, state.totalVotes),
        )
        !state.eligible -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(Res.string.poll_detail_banner_readonly, state.commissionNames.joinToString(", ")),
        )
        state.hasVoted && !state.isEditing -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            stringResource(Res.string.poll_detail_banner_voted),
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(Res.string.poll_detail_banner_vote),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = fg)
    }
}

@Composable
private fun OptionRow(
    option: PollOptionUi,
    showResults: Boolean,
    selected: Boolean,
    onTap: () -> Unit,
) {
    if (showResults) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.5.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(14.dp),
                ),
        ) {
            // Proportional fill behind the label.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(14.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(option.percent / 100f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${option.percent}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                )
                .border(
                    width = 1.75.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(14.dp),
                )
                .clickable(onClick = onTap)
                .padding(horizontal = Spacing.SP4, vertical = Spacing.SP4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    )
                    .border(
                        width = 2.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.size(Spacing.SP3))
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun VoteBar(state: PollDetailState, onIntent: (PollDetailIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.SP5, vertical = Spacing.SP3),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
    ) {
        if (!state.isOnline) {
            Text(
                text = stringResource(Res.string.poll_detail_offline),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.hasVoted && !state.isEditing) {
            OutlinedButton(
                onClick = { onIntent(PollDetailIntent.EditVote) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.poll_detail_change_vote),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Button(
                onClick = { onIntent(PollDetailIntent.SubmitVote) },
                enabled = state.canSubmitVote,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.poll_detail_submit_vote),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun Centered(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DeletedState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.SP6),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.poll_detail_deleted_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.SP3))
        Text(
            text = stringResource(Res.string.poll_detail_deleted_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
