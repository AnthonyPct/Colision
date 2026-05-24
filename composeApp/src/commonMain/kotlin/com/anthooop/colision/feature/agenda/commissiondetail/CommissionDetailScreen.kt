package com.anthooop.colision.feature.agenda.commissiondetail

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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.commission_detail_members
import colision.composeapp.generated.resources.commission_detail_no_meetings
import colision.composeapp.generated.resources.commission_detail_subtitle
import colision.composeapp.generated.resources.commission_detail_upcoming_meetings
import colision.composeapp.generated.resources.agenda_create_meeting
import com.anthooop.colision.core.database.entity.MeetingEntity
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.core.design.rememberOfflineGate
import colision.composeapp.generated.resources.write_offline_message
import com.anthooop.colision.feature.agenda.agenda.durationMinutes
import com.anthooop.colision.feature.agenda.agenda.extractTime
import com.anthooop.colision.feature.agenda.agenda.parseIsoDate
import com.anthooop.colision.feature.agenda.agenda.rememberMonthNames
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommissionDetailScreen(
    state: CommissionDetailState,
    onIntent: (CommissionDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = safe.calculateTopPadding(), bottom = safe.calculateBottomPadding()),
        ) {
            TopBar(onBack = { onIntent(CommissionDetailIntent.BackTapped) })

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.SP5,
                    end = Spacing.SP5,
                    top = Spacing.SP4,
                    bottom = if (state.currentMemberIsMember) 96.dp else Spacing.SP4,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.SP4),
            ) {
                item { Header(state = state) }

                item {
                    Text(
                        text = stringResource(Res.string.commission_detail_upcoming_meetings),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (state.meetings.isEmpty() && !state.isLoading) {
                    item {
                        Text(
                            text = stringResource(Res.string.commission_detail_no_meetings),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    items(state.meetings, key = { it.id }) { meeting ->
                        MeetingRow(meeting = meeting, onTap = {
                            onIntent(CommissionDetailIntent.MeetingTapped(meeting.id))
                        })
                    }
                }

                item {
                    Text(
                        text = stringResource(Res.string.commission_detail_members, state.members.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(state.members, key = { it.id }) { member ->
                    MemberRow(member = member)
                }
            }
        }

        if (state.currentMemberIsMember) {
            val offlineGate = rememberOfflineGate(stringResource(Res.string.write_offline_message))
            ExtendedFloatingActionButton(
                onClick = { offlineGate.run { onIntent(CommissionDetailIntent.CreateMeetingTapped) } },
                containerColor = if (offlineGate.isOnline) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                text = { Text(stringResource(Res.string.agenda_create_meeting)) },
                icon = {
                    Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Spacing.SP5, bottom = Spacing.SP5 + safe.calculateBottomPadding()),
            )
        }
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
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun Header(state: CommissionDetailState) {
    Column {
        Text(
            text = state.commission?.name.orEmpty(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(Spacing.SP1))
        Text(
            text = stringResource(
                Res.string.commission_detail_subtitle,
                state.members.size,
                state.meetings.size,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MeetingRow(meeting: MeetingEntity, onTap: () -> Unit) {
    val months = rememberMonthNames()
    val title = meeting.title?.takeIf { it.isNotBlank() } ?: parseIsoDate(meeting.startsAt, months)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onTap)
            .padding(Spacing.SP4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(56.dp)) {
            Text(
                text = extractTime(meeting.startsAt),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${durationMinutes(meeting.startsAt, meeting.endsAt)}min",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(Spacing.SP3))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = parseIsoDate(meeting.startsAt, months),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MemberRow(member: MemberEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = Spacing.SP3, vertical = Spacing.SP3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = member.displayName)
        Spacer(Modifier.width(Spacing.SP3))
        Text(
            text = member.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
