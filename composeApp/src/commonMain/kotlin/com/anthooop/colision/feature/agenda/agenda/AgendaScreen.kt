package com.anthooop.colision.feature.agenda.agenda

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.agenda_create_meeting
import colision.composeapp.generated.resources.agenda_empty
import colision.composeapp.generated.resources.agenda_greeting
import colision.composeapp.generated.resources.agenda_offline_banner
import colision.composeapp.generated.resources.agenda_offline_banner_no_sync
import colision.composeapp.generated.resources.agenda_toggle_month
import colision.composeapp.generated.resources.agenda_toggle_week
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun AgendaScreen(
    state: AgendaState,
    onIntent: (AgendaIntent) -> Unit,
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
                .padding(top = safe.calculateTopPadding()),
        ) {
            if (!state.isOnline) {
                OfflineBanner(lastSyncTime = state.lastSyncTime)
            }
            Header(firstName = state.firstName)
            ViewToggle(view = state.view, onSelect = { onIntent(AgendaIntent.ViewSelected(it)) })
            Spacer(Modifier.height(Spacing.SP2))
            when {
                state.meetings.isEmpty() && !state.isLoading -> EmptyState()
                state.view == AgendaView.Week -> WeekView(
                    meetings = state.meetings,
                    onMeetingTap = { onIntent(AgendaIntent.MeetingTapped(it)) },
                )
                else -> MonthView(
                    meetings = state.meetings,
                    onMeetingTap = { onIntent(AgendaIntent.MeetingTapped(it)) },
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onIntent(AgendaIntent.CreateMeetingTapped) },
            containerColor = MaterialTheme.colorScheme.primary,
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

@Composable
private fun OfflineBanner(lastSyncTime: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = Spacing.SP5, vertical = Spacing.SP2),
    ) {
        Text(
            text = if (lastSyncTime != null) {
                stringResource(Res.string.agenda_offline_banner, lastSyncTime)
            } else {
                stringResource(Res.string.agenda_offline_banner_no_sync)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Header(firstName: String) {
    Column(modifier = Modifier.padding(horizontal = Spacing.SP5, vertical = Spacing.SP3)) {
        val name = firstName.ifBlank { "" }
        Text(
            text = stringResource(Res.string.agenda_greeting, name).trim(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ViewToggle(view: AgendaView, onSelect: (AgendaView) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.SP5, vertical = Spacing.SP2),
        horizontalArrangement = Arrangement.spacedBy(Spacing.SP2),
    ) {
        TogglePill(
            label = stringResource(Res.string.agenda_toggle_week),
            selected = view == AgendaView.Week,
            onClick = { onSelect(AgendaView.Week) },
        )
        TogglePill(
            label = stringResource(Res.string.agenda_toggle_month),
            selected = view == AgendaView.Month,
            onClick = { onSelect(AgendaView.Month) },
        )
    }
}

@Composable
private fun TogglePill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(Spacing.SP6),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.agenda_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WeekView(
    meetings: List<AgendaMeeting>,
    onMeetingTap: (String) -> Unit,
) {
    val grouped = meetings.groupBy { it.meeting.startsAt.substring(0, 10) }
    val dates = grouped.keys.sorted()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.SP5,
            end = Spacing.SP5,
            top = Spacing.SP2,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP5),
    ) {
        items(dates, key = { it }) { date ->
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
                DayHeader(date)
                grouped[date].orEmpty().forEach { item ->
                    MeetingCard(item = item, onTap = { onMeetingTap(item.meeting.id) })
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: String) {
    val parsed = parseIsoDate(date)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = parsed.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MeetingCard(item: AgendaMeeting, onTap: () -> Unit) {
    val borderColor = if (item.conflicted) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = Spacing.SP4, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = extractTime(item.meeting.startsAt),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = extractTime(item.meeting.endsAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "${durationMinutes(item.meeting.startsAt, item.meeting.endsAt)}min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(Spacing.SP3))
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent),
        )
        Spacer(Modifier.width(Spacing.SP3))
        Column(modifier = Modifier.fillMaxWidth()) {
            val title = item.meeting.title?.takeIf { it.isNotBlank() }
                ?: item.commissions.firstOrNull()?.name.orEmpty()
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (item.commissions.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.SP2))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.commissions.take(3).forEach { c ->
                        CommissionChip(name = c.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommissionChip(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun MonthView(
    meetings: List<AgendaMeeting>,
    onMeetingTap: (String) -> Unit,
) {
    val byDay = meetings.groupBy { it.meeting.startsAt.substring(0, 10) }
    val days = byDay.keys.sorted()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.SP5,
            end = Spacing.SP5,
            top = Spacing.SP2,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
    ) {
        items(days, key = { it }) { date ->
            val items = byDay[date].orEmpty()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(Spacing.SP3),
                verticalArrangement = Arrangement.spacedBy(Spacing.SP1),
            ) {
                Text(
                    text = parseIsoDate(date).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { onMeetingTap(item.meeting.id) })
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (item.conflicted) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary,
                                ),
                        )
                        Spacer(Modifier.width(Spacing.SP2))
                        Text(
                            text = extractTime(item.meeting.startsAt),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(Spacing.SP3))
                        Text(
                            text = item.meeting.title?.takeIf { it.isNotBlank() }
                                ?: item.commissions.firstOrNull()?.name.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

internal fun extractTime(isoDate: String): String {
    val tIdx = isoDate.indexOf('T').takeIf { it >= 0 } ?: return ""
    return isoDate.substring(tIdx + 1, minOf(tIdx + 6, isoDate.length))
}

internal fun parseIsoDate(isoDateOrDate: String): String {
    val tIdx = isoDateOrDate.indexOf('T')
    val datePart = if (tIdx >= 0) isoDateOrDate.substring(0, tIdx) else isoDateOrDate
    val parts = datePart.split('-')
    if (parts.size != 3) return datePart
    val day = parts[2].trimStart('0').ifBlank { "0" }
    val months = listOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre",
    )
    val monthIdx = parts[1].toIntOrNull()?.let { it - 1 } ?: 0
    val month = months.getOrNull(monthIdx) ?: parts[1]
    return "$day $month"
}

internal fun durationMinutes(start: String, end: String): Int {
    fun toMin(s: String): Int {
        val t = s.indexOf('T').takeIf { it >= 0 } ?: return 0
        val hh = s.substring(t + 1, t + 3).toIntOrNull() ?: 0
        val mm = s.substring(t + 4, t + 6).toIntOrNull() ?: 0
        return hh * 60 + mm
    }
    val diff = toMin(end) - toMin(start)
    return if (diff < 0) diff + 24 * 60 else diff
}
