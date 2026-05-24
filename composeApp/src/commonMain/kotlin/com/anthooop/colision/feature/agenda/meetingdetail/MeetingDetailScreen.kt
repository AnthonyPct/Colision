package com.anthooop.colision.feature.agenda.meetingdetail

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
import androidx.compose.material3.OutlinedButton
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
import colision.composeapp.generated.resources.action_delete
import colision.composeapp.generated.resources.action_modify
import colision.composeapp.generated.resources.meeting_detail_attendees
import colision.composeapp.generated.resources.meeting_detail_created_by
import colision.composeapp.generated.resources.meeting_detail_deleted_body
import colision.composeapp.generated.resources.meeting_detail_deleted_title
import colision.composeapp.generated.resources.meeting_detail_duration_minutes
import colision.composeapp.generated.resources.meeting_detail_loading
import colision.composeapp.generated.resources.meeting_detail_no_title_fallback
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.feature.agenda.agenda.durationMinutes
import com.anthooop.colision.feature.agenda.agenda.extractTime
import com.anthooop.colision.feature.agenda.agenda.parseIsoDate
import com.anthooop.colision.feature.agenda.agenda.rememberMonthNames
import org.jetbrains.compose.resources.stringResource

@Composable
fun MeetingDetailScreen(
    state: MeetingDetailState,
    onIntent: (MeetingDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safe.calculateTopPadding(), bottom = safe.calculateBottomPadding()),
    ) {
        TopBar(onBack = { onIntent(MeetingDetailIntent.BackTapped) })

        when {
            state.isLoading -> Centered(stringResource(Res.string.meeting_detail_loading))
            state.isDeleted -> DeletedState()
            state.meeting != null -> MeetingBody(state = state, onIntent = onIntent)
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
            text = stringResource(Res.string.meeting_detail_deleted_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.SP3))
        Text(
            text = stringResource(Res.string.meeting_detail_deleted_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MeetingBody(state: MeetingDetailState, onIntent: (MeetingDetailIntent) -> Unit) {
    val meeting = state.meeting ?: return
    val months = rememberMonthNames()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.SP5, vertical = Spacing.SP4),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP4),
    ) {
        item {
            if (state.commissions.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.commissions.forEach { c ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = c.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.SP3))
            }
            val title = meeting.title?.takeIf { it.isNotBlank() }
                ?: state.commissions.firstOrNull()?.name
                ?: stringResource(Res.string.meeting_detail_no_title_fallback)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                    .padding(Spacing.SP4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = parseIsoDate(meeting.startsAt, months).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val duration = durationMinutes(meeting.startsAt, meeting.endsAt)
                    Text(
                        text = stringResource(
                            Res.string.meeting_detail_duration_minutes,
                            extractTime(meeting.startsAt),
                            extractTime(meeting.endsAt),
                            duration,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (state.isCreator) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
                    OutlinedButton(onClick = { onIntent(MeetingDetailIntent.EditTapped) }) {
                        Text(stringResource(Res.string.action_modify))
                    }
                    OutlinedButton(onClick = { onIntent(MeetingDetailIntent.DeleteTapped) }) {
                        Text(stringResource(Res.string.action_delete))
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(
                    Res.string.meeting_detail_attendees,
                    state.attendees.size,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }
        items(state.attendees, key = { it.id }) { member ->
            AttendeeRow(member = member)
        }

        item {
            state.creator?.let { creator ->
                Spacer(Modifier.height(Spacing.SP3))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(name = creator.displayName)
                    Spacer(Modifier.width(Spacing.SP2))
                    Text(
                        text = stringResource(Res.string.meeting_detail_created_by, creator.displayName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendeeRow(member: MemberEntity) {
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

