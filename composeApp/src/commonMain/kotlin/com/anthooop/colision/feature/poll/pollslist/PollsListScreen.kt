@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.anthooop.colision.feature.poll.pollslist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.poll_deadline_closed
import colision.composeapp.generated.resources.poll_deadline_days
import colision.composeapp.generated.resources.poll_deadline_last_day
import colision.composeapp.generated.resources.polls_empty_closed
import colision.composeapp.generated.resources.polls_empty_open
import colision.composeapp.generated.resources.polls_filter_closed
import colision.composeapp.generated.resources.polls_filter_open
import colision.composeapp.generated.resources.polls_footer
import colision.composeapp.generated.resources.polls_members_only
import colision.composeapp.generated.resources.polls_new
import colision.composeapp.generated.resources.polls_not_voted
import colision.composeapp.generated.resources.polls_public_pill
import colision.composeapp.generated.resources.polls_result
import colision.composeapp.generated.resources.polls_title
import colision.composeapp.generated.resources.polls_vote_count
import colision.composeapp.generated.resources.polls_voted
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PollsListScreen(
    state: PollsListState,
    onIntent: (PollsListIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    val months = rememberPollMonthNames()
    val weekdays = rememberPollWeekdayNames()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safe.calculateTopPadding()),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(Res.string.polls_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    start = Spacing.SP5,
                    end = Spacing.SP5,
                    top = Spacing.SP4,
                    bottom = Spacing.SP3,
                ),
            )

            Row(
                modifier = Modifier.padding(horizontal = Spacing.SP5),
                horizontalArrangement = Arrangement.spacedBy(Spacing.SP2),
            ) {
                FilterChip(
                    selected = state.filter == PollFilter.Open,
                    onClick = { onIntent(PollsListIntent.FilterSelected(PollFilter.Open)) },
                    label = { Text(stringResource(Res.string.polls_filter_open, state.openPolls.size)) },
                    colors = filterChipColors(),
                )
                FilterChip(
                    selected = state.filter == PollFilter.Closed,
                    onClick = { onIntent(PollsListIntent.FilterSelected(PollFilter.Closed)) },
                    label = { Text(stringResource(Res.string.polls_filter_closed, state.closedPolls.size)) },
                    colors = filterChipColors(),
                )
            }

            val polls = state.visiblePolls
            if (polls.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(Spacing.SP6),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(
                            if (state.filter == PollFilter.Open) {
                                Res.string.polls_empty_open
                            } else {
                                Res.string.polls_empty_closed
                            },
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.SP5,
                        end = Spacing.SP5,
                        top = Spacing.SP3,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SP3),
                ) {
                    items(polls, key = { it.id }) { poll ->
                        PollCard(
                            poll = poll,
                            months = months,
                            weekdays = weekdays,
                            onTap = { onIntent(PollsListIntent.PollTapped(poll.id)) },
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onIntent(PollsListIntent.CreateTapped) },
            text = { Text(stringResource(Res.string.polls_new)) },
            icon = {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.SP5),
        )
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

@Composable
private fun PollCard(
    poll: PollListItem,
    months: List<String>,
    weekdays: List<String>,
    onTap: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onTap)
            .padding(Spacing.SP4),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP3),
    ) {
        Text(
            text = poll.question,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )

        ScopePills(poll = poll)

        // Per-user status line.
        when {
            poll.isClosed -> poll.winnerLabel?.let { winner ->
                Text(
                    text = stringResource(Res.string.polls_result, winner),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
            poll.hasVoted -> Text(
                text = stringResource(Res.string.polls_voted),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            poll.eligible -> Text(
                text = stringResource(Res.string.polls_not_voted),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> Text(
                text = stringResource(Res.string.polls_members_only),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    Res.string.polls_footer,
                    pluralStringResource(Res.plurals.polls_vote_count, poll.voters, poll.voters),
                    pollFullDate(poll.closesAtIso, months, weekdays),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            DeadlineChip(isClosed = poll.isClosed, daysLeft = poll.daysLeft)
        }
    }
}

@Composable
private fun ScopePills(poll: PollListItem) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (poll.targetIsPublic) {
            Pill(text = stringResource(Res.string.polls_public_pill))
        } else {
            poll.commissionNames.forEach { name -> Pill(text = name) }
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
internal fun DeadlineChip(isClosed: Boolean, daysLeft: Int) {
    val urgent = !isClosed && daysLeft <= 2
    val bg = when {
        isClosed -> MaterialTheme.colorScheme.surfaceVariant
        urgent -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        isClosed -> MaterialTheme.colorScheme.onSurfaceVariant
        urgent -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when {
        isClosed -> stringResource(Res.string.poll_deadline_closed)
        daysLeft == 0 -> stringResource(Res.string.poll_deadline_last_day)
        else -> stringResource(Res.string.poll_deadline_days, daysLeft)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
