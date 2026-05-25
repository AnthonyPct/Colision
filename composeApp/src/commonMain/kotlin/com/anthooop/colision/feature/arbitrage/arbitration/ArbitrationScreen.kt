package com.anthooop.colision.feature.arbitrage.arbitration

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import colision.composeapp.generated.resources.arbitration_card_invited
import colision.composeapp.generated.resources.arbitration_card_organizer
import colision.composeapp.generated.resources.arbitration_choice_a_label
import colision.composeapp.generated.resources.arbitration_choice_b_label
import colision.composeapp.generated.resources.arbitration_choice_default
import colision.composeapp.generated.resources.arbitration_choice_later_button
import colision.composeapp.generated.resources.arbitration_choice_later_label
import colision.composeapp.generated.resources.arbitration_eyebrow
import colision.composeapp.generated.resources.arbitration_note
import colision.composeapp.generated.resources.arbitration_resolved_action_back
import colision.composeapp.generated.resources.arbitration_resolved_body
import colision.composeapp.generated.resources.arbitration_resolved_title
import colision.composeapp.generated.resources.arbitration_separator_or
import colision.composeapp.generated.resources.arbitration_time_range
import colision.composeapp.generated.resources.arbitration_title
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.feature.agenda.agenda.extractTime
import com.anthooop.colision.feature.agenda.agenda.parseIsoDate
import com.anthooop.colision.feature.agenda.agenda.rememberMonthNames
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArbitrationScreen(
    state: ArbitrationState,
    onIntent: (ArbitrationIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safe.calculateTopPadding(), bottom = safe.calculateBottomPadding()),
    ) {
        TopBar(onBack = { onIntent(ArbitrationIntent.BackTapped) })

        when {
            state.isResolved -> ResolvedState(onBack = { onIntent(ArbitrationIntent.BackTapped) })
            state.meetingA != null && state.meetingB != null -> ArbitrationBody(
                meetingA = state.meetingA,
                meetingB = state.meetingB,
                currentChoice = state.currentChoice,
                isSubmitting = state.isSubmitting,
                onIntent = onIntent,
            )
            else -> Box(modifier = Modifier.fillMaxSize())
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
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ResolvedState(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.SP6),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.arbitration_resolved_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(Spacing.SP3))
        Text(
            text = stringResource(Res.string.arbitration_resolved_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.SP5))
        Button(onClick = onBack) {
            Text(stringResource(Res.string.arbitration_resolved_action_back))
        }
    }
}

@Composable
private fun ArbitrationBody(
    meetingA: ArbitrationMeetingUi,
    meetingB: ArbitrationMeetingUi,
    currentChoice: ArbitrationChoice?,
    isSubmitting: Boolean,
    onIntent: (ArbitrationIntent) -> Unit,
) {
    val months = rememberMonthNames()
    val datePart = parseIsoDate(meetingA.startsAt, months)
        .replaceFirstChar { it.uppercase() }
    val timePart = extractTime(meetingA.startsAt)
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    PaddingValues(
                        start = Spacing.SP5,
                        end = Spacing.SP5,
                        bottom = Spacing.SP6,
                    ),
                ),
        ) {
            Text(
                text = stringResource(Res.string.arbitration_eyebrow, datePart, timePart),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Spacing.SP2))
            Text(
                text = stringResource(Res.string.arbitration_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Spacing.SP2))
            Text(
                text = stringResource(Res.string.arbitration_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.SP5))

            MeetingChoiceCard(
                meeting = meetingA,
                selected = currentChoice == ArbitrationChoice.GoingToA,
                onClick = { onIntent(ArbitrationIntent.ChoiceTapped(ArbitrationChoice.GoingToA)) },
            )
            Spacer(Modifier.height(Spacing.SP3))
            OrSeparator()
            Spacer(Modifier.height(Spacing.SP3))
            MeetingChoiceCard(
                meeting = meetingB,
                selected = currentChoice == ArbitrationChoice.GoingToB,
                onClick = { onIntent(ArbitrationIntent.ChoiceTapped(ArbitrationChoice.GoingToB)) },
            )

            Spacer(Modifier.height(Spacing.SP3))
            LaterChoice(
                selected = currentChoice == ArbitrationChoice.Later,
                onClick = { onIntent(ArbitrationIntent.ChoiceTapped(ArbitrationChoice.Later)) },
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SubmitBar(
            meetingA = meetingA,
            meetingB = meetingB,
            currentChoice = currentChoice,
            isSubmitting = isSubmitting,
            onSubmit = { onIntent(ArbitrationIntent.SubmitTapped) },
        )
    }
}

@Composable
private fun MeetingChoiceCard(
    meeting: ArbitrationMeetingUi,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(Spacing.SP4),
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column {
            CommissionChip(name = meeting.commissionName)
            Spacer(Modifier.height(Spacing.SP3))
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Spacing.SP2))
            Text(
                text = stringResource(
                    Res.string.arbitration_time_range,
                    extractTime(meeting.startsAt),
                    extractTime(meeting.endsAt),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = pluralStringResource(
                    Res.plurals.arbitration_card_invited,
                    meeting.invitedCount,
                    meeting.invitedCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            meeting.organizerName?.let { organizer ->
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.arbitration_card_organizer, organizer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun OrSeparator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = stringResource(Res.string.arbitration_separator_or),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.SP3),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun LaterChoice(selected: Boolean, onClick: () -> Unit) {
    val (bg, border) = if (selected) {
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.background to MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.arbitration_choice_later_label),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SubmitBar(
    meetingA: ArbitrationMeetingUi,
    meetingB: ArbitrationMeetingUi,
    currentChoice: ArbitrationChoice?,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
) {
    val label = when (currentChoice) {
        ArbitrationChoice.GoingToA -> stringResource(
            Res.string.arbitration_choice_a_label,
            meetingA.commissionName,
        )
        ArbitrationChoice.GoingToB -> stringResource(
            Res.string.arbitration_choice_b_label,
            meetingB.commissionName,
        )
        ArbitrationChoice.Later -> stringResource(Res.string.arbitration_choice_later_button)
        null -> stringResource(Res.string.arbitration_choice_default)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.SP5, vertical = Spacing.SP3),
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSubmit,
            enabled = currentChoice != null && !isSubmitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
