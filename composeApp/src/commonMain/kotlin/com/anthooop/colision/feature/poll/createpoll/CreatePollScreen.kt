@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    kotlin.time.ExperimentalTime::class,
)

package com.anthooop.colision.feature.poll.createpoll

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import colision.composeapp.generated.resources.action_cancel
import colision.composeapp.generated.resources.action_ok
import colision.composeapp.generated.resources.dialog_error_title
import colision.composeapp.generated.resources.create_poll_add_option
import colision.composeapp.generated.resources.create_poll_date_hint
import colision.composeapp.generated.resources.create_poll_date_label
import colision.composeapp.generated.resources.create_poll_date_picker_title
import colision.composeapp.generated.resources.create_poll_eligible
import colision.composeapp.generated.resources.create_poll_offline
import colision.composeapp.generated.resources.create_poll_option_placeholder
import colision.composeapp.generated.resources.create_poll_options_label
import colision.composeapp.generated.resources.create_poll_public
import colision.composeapp.generated.resources.create_poll_question_label
import colision.composeapp.generated.resources.create_poll_question_placeholder
import colision.composeapp.generated.resources.create_poll_scope_label
import colision.composeapp.generated.resources.create_poll_submit
import colision.composeapp.generated.resources.create_poll_title
import colision.composeapp.generated.resources.poll_generic_error
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.feature.poll.pollslist.pollFullDate
import com.anthooop.colision.feature.poll.pollslist.rememberPollMonthNames
import com.anthooop.colision.feature.poll.pollslist.rememberPollWeekdayNames
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreatePollScreen(
    state: CreatePollState,
    onIntent: (CreatePollIntent) -> Unit,
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
            title = stringResource(Res.string.create_poll_title),
            onClose = { onIntent(CreatePollIntent.BackTapped) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.SP5, vertical = Spacing.SP3),
            verticalArrangement = Arrangement.spacedBy(Spacing.SP5),
        ) {
            QuestionField(value = state.question, onChange = { onIntent(CreatePollIntent.QuestionChanged(it)) })
            OptionsField(
                options = state.options,
                onChange = { i, v -> onIntent(CreatePollIntent.OptionChanged(i, v)) },
                onAdd = { onIntent(CreatePollIntent.OptionAdded) },
                onRemove = { onIntent(CreatePollIntent.OptionRemoved(it)) },
            )
            ScopeField(
                isPublic = state.isPublic,
                commissions = state.commissions,
                selectedIds = state.selectedCommissionIds,
                eligibleCount = state.eligibleCount,
                onPublicToggle = { onIntent(CreatePollIntent.PublicToggled(it)) },
                onCommissionToggle = { onIntent(CreatePollIntent.CommissionToggled(it)) },
            )
            DateField(
                selected = state.closesDate,
                onSelect = { onIntent(CreatePollIntent.DateSelected(it)) },
            )
        }

        BottomBar(
            canCreate = state.canCreate,
            isOnline = state.isOnline,
            onSubmit = { onIntent(CreatePollIntent.SubmitTapped) },
        )
    }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { onIntent(CreatePollIntent.ErrorDismissed) },
            confirmButton = {
                TextButton(onClick = { onIntent(CreatePollIntent.ErrorDismissed) }) {
                    Text(stringResource(Res.string.action_ok))
                }
            },
            title = { Text(stringResource(Res.string.dialog_error_title)) },
            text = { Text(stringResource(Res.string.poll_generic_error)) },
        )
    }
}

@Composable
private fun TopBar(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.SP3, vertical = Spacing.SP2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onClose) {
            Text(
                text = "✕",
                style = MaterialTheme.typography.titleMedium,
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
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun QuestionField(value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
        SectionLabel(stringResource(Res.string.create_poll_question_label))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = {
                Text(
                    text = stringResource(Res.string.create_poll_question_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OptionsField(
    options: List<String>,
    onChange: (Int, String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
        SectionLabel(stringResource(Res.string.create_poll_options_label))
        options.forEachIndexed { index, value ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { onChange(index, it) },
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.create_poll_option_placeholder, index + 1),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                if (options.size > 2) {
                    TextButton(onClick = { onRemove(index) }) {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        TextButton(onClick = onAdd, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text(
                text = "+ " + stringResource(Res.string.create_poll_add_option),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ScopeField(
    isPublic: Boolean,
    commissions: List<CommissionEntity>,
    selectedIds: Set<String>,
    eligibleCount: Int,
    onPublicToggle: (Boolean) -> Unit,
    onCommissionToggle: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
        SectionLabel(stringResource(Res.string.create_poll_scope_label))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ChoicePill(
                text = stringResource(Res.string.create_poll_public),
                selected = isPublic,
                enabled = true,
                onClick = { onPublicToggle(!isPublic) },
            )
            commissions.forEach { c ->
                ChoicePill(
                    text = c.name,
                    selected = !isPublic && c.id in selectedIds,
                    enabled = !isPublic,
                    onClick = { if (!isPublic) onCommissionToggle(c.id) },
                )
            }
        }
        Text(
            text = pluralStringResource(Res.plurals.create_poll_eligible, eligibleCount, eligibleCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChoicePill(text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val fg = when {
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(
                width = 1.5.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 1f else 0.4f),
                shape = RoundedCornerShape(999.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DateField(selected: String, onSelect: (String) -> Unit) {
    val months = rememberPollMonthNames()
    val weekdays = rememberPollWeekdayNames()
    var showPicker by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SP2)) {
        SectionLabel(stringResource(Res.string.create_poll_date_label))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .clickable { showPicker = true }
                .padding(horizontal = Spacing.SP4, vertical = Spacing.SP4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pollFullDate(selected, months, weekdays),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "📅",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text = stringResource(Res.string.create_poll_date_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = isoToMillis(selected))
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onSelect(millisToIso(it)) }
                    showPicker = false
                }) { Text(stringResource(Res.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(Res.string.action_cancel)) }
            },
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text(
                        text = stringResource(Res.string.create_poll_date_picker_title),
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
}

@Composable
private fun BottomBar(canCreate: Boolean, isOnline: Boolean, onSubmit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.SP5, vertical = Spacing.SP3),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP2),
    ) {
        if (!isOnline) {
            Text(
                text = stringResource(Res.string.create_poll_offline),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = onSubmit,
            enabled = canCreate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.create_poll_submit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ISO yyyy-MM-dd <-> epoch millis at UTC midnight, for the M3 DatePicker.
private fun isoToMillis(iso: String): Long? {
    val parts = iso.split('-')
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    return runCatching {
        kotlinx.datetime.LocalDate(y, m, d)
            .atStartOfDayIn(kotlinx.datetime.TimeZone.UTC)
            .toEpochMilliseconds()
    }.getOrNull()
}

private fun millisToIso(millis: Long): String =
    kotlin.time.Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
        .date
        .toString()
