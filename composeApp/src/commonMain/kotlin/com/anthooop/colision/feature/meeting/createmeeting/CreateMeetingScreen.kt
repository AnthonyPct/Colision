@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.anthooop.colision.feature.meeting.createmeeting

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.create_meeting_commissions_label
import colision.composeapp.generated.resources.create_meeting_date_label
import colision.composeapp.generated.resources.create_meeting_duration_label
import colision.composeapp.generated.resources.create_meeting_duration_min
import colision.composeapp.generated.resources.create_meeting_no_conflict_banner
import colision.composeapp.generated.resources.create_meeting_offline_message
import colision.composeapp.generated.resources.create_meeting_start_label
import colision.composeapp.generated.resources.create_meeting_submit
import colision.composeapp.generated.resources.create_meeting_time_placeholder
import colision.composeapp.generated.resources.create_meeting_title
import colision.composeapp.generated.resources.create_meeting_title_label
import colision.composeapp.generated.resources.create_meeting_title_placeholder
import colision.composeapp.generated.resources.month_april
import colision.composeapp.generated.resources.month_august
import colision.composeapp.generated.resources.month_december
import colision.composeapp.generated.resources.month_february
import colision.composeapp.generated.resources.month_january
import colision.composeapp.generated.resources.month_july
import colision.composeapp.generated.resources.month_june
import colision.composeapp.generated.resources.month_march
import colision.composeapp.generated.resources.month_may
import colision.composeapp.generated.resources.month_november
import colision.composeapp.generated.resources.month_october
import colision.composeapp.generated.resources.month_september
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateMeetingScreen(
    state: CreateMeetingState,
    onIntent: (CreateMeetingIntent) -> Unit,
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
            title = stringResource(Res.string.create_meeting_title),
            onBack = { onIntent(CreateMeetingIntent.BackTapped) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.SP5, vertical = Spacing.SP3),
            verticalArrangement = Arrangement.spacedBy(Spacing.SP5),
        ) {
            TitleField(value = state.title, onChange = { onIntent(CreateMeetingIntent.TitleChanged(it)) })
            DateField(
                dates = state.availableDates,
                selected = state.date,
                onSelect = { onIntent(CreateMeetingIntent.DateSelected(it)) },
            )
            TimeAndDuration(
                time = state.time,
                duration = state.duration,
                onTimeChange = { onIntent(CreateMeetingIntent.TimeChanged(it)) },
                onDurationSelect = { onIntent(CreateMeetingIntent.DurationSelected(it)) },
            )
            CommissionsField(
                commissions = state.commissions,
                selectedIds = state.selectedCommissionIds,
                onToggle = { onIntent(CreateMeetingIntent.CommissionToggled(it)) },
            )
            NoConflictBanner()
        }

        BottomBar(
            canSubmit = state.canSubmit,
            isSubmitting = state.isSubmitting,
            isOnline = state.isOnline,
            onSubmit = { onIntent(CreateMeetingIntent.SubmitTapped) },
        )
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
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
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.4.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun TitleField(value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
        SectionLabel(stringResource(Res.string.create_meeting_title_label))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = {
                Text(
                    text = stringResource(Res.string.create_meeting_title_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DateField(dates: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
        SectionLabel(stringResource(Res.string.create_meeting_date_label))
        val months = rememberMonthAbbrev()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.SP2),
            contentPadding = PaddingValues(vertical = 2.dp),
        ) {
            items(dates, key = { it }) { iso ->
                DateCard(iso = iso, monthAbbrev = months, selected = iso == selected, onTap = { onSelect(iso) })
            }
        }
    }
}

@Composable
private fun DateCard(iso: String, monthAbbrev: List<String>, selected: Boolean, onTap: () -> Unit) {
    val parsed = parseIso(iso, monthAbbrev)
    val container = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface
    val labelColor = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
    val bigColor = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = Modifier
            .size(width = 56.dp, height = 72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .border(
                width = 1.5.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onTap)
            .padding(vertical = Spacing.SP2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = parsed.dayOfWeekShort.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = labelColor,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = parsed.day,
            style = MaterialTheme.typography.titleLarge,
            color = bigColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TimeAndDuration(
    time: String,
    duration: DurationOption,
    onTimeChange: (String) -> Unit,
    onDurationSelect: (DurationOption) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.SP3)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
            SectionLabel(stringResource(Res.string.create_meeting_start_label))
            OutlinedTextField(
                value = time,
                onValueChange = { raw -> onTimeChange(formatTimeInput(raw)) },
                placeholder = {
                    Text(
                        text = stringResource(Res.string.create_meeting_time_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
            SectionLabel(stringResource(Res.string.create_meeting_duration_label))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                DurationOption.entries.forEach { option ->
                    DurationChip(
                        option = option,
                        selected = option == duration,
                        onTap = { onDurationSelect(option) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationChip(
    option: DurationOption,
    selected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = 1.5.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.create_meeting_duration_min, option.minutes),
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CommissionsField(
    commissions: List<CommissionEntity>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
        SectionLabel(stringResource(Res.string.create_meeting_commissions_label))
        FlowChips(
            items = commissions,
            isSelected = { it.id in selectedIds },
            onToggle = { onToggle(it.id) },
        )
    }
}

@Composable
private fun FlowChips(
    items: List<CommissionEntity>,
    isSelected: (CommissionEntity) -> Boolean,
    onToggle: (CommissionEntity) -> Unit,
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items.forEach { c ->
            val selected = isSelected(c)
            val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(bg)
                    .border(
                        width = 1.5.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(999.dp),
                    )
                    .clickable(onClick = { onToggle(c) })
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = c.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = fg,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun NoConflictBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.create_meeting_no_conflict_banner),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BottomBar(
    canSubmit: Boolean,
    isSubmitting: Boolean,
    isOnline: Boolean,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.SP5, vertical = Spacing.SP3),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
    ) {
        if (!isOnline) {
            Text(
                text = stringResource(Res.string.create_meeting_offline_message),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.create_meeting_submit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

internal data class ParsedDate(val day: String, val dayOfWeekShort: String)

internal fun parseIso(iso: String, monthAbbrev: List<String>): ParsedDate {
    val parts = iso.split('-')
    if (parts.size != 3) return ParsedDate(iso, "")
    val year = parts[0].toIntOrNull() ?: 0
    val month = parts[1].toIntOrNull() ?: 1
    val day = parts[2].toIntOrNull() ?: 1
    val dow = zellersDayOfWeekShort(year, month, day)
    return ParsedDate(day = day.toString(), dayOfWeekShort = dow)
}

private val daysShortFr = listOf("lun", "mar", "mer", "jeu", "ven", "sam", "dim")

internal fun zellersDayOfWeekShort(year: Int, month: Int, day: Int): String {
    // Zeller's congruence — 0=Saturday, 1=Sunday, … 6=Friday. Convert into
    // Monday-first index to match the French week labels in the design.
    val y = if (month < 3) year - 1 else year
    val m = if (month < 3) month + 12 else month
    val k = y % 100
    val j = y / 100
    val h = (day + 13 * (m + 1) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
    // h: 0=Sat, 1=Sun, 2=Mon, ..., 6=Fri → mondayIdx
    val mondayIdx = (h + 5) % 7
    return daysShortFr[mondayIdx]
}

@Composable
private fun rememberMonthAbbrev(): List<String> = listOf(
    stringResource(Res.string.month_january),
    stringResource(Res.string.month_february),
    stringResource(Res.string.month_march),
    stringResource(Res.string.month_april),
    stringResource(Res.string.month_may),
    stringResource(Res.string.month_june),
    stringResource(Res.string.month_july),
    stringResource(Res.string.month_august),
    stringResource(Res.string.month_september),
    stringResource(Res.string.month_october),
    stringResource(Res.string.month_november),
    stringResource(Res.string.month_december),
)

internal fun formatTimeInput(raw: String): String {
    // Strip non-digits, cap to 4 digits, then format as HH:mm.
    val digits = raw.filter { it.isDigit() }.take(4)
    return when {
        digits.length <= 2 -> digits
        else -> digits.substring(0, 2) + ":" + digits.substring(2)
    }
}
