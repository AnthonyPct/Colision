package com.anthooop.colision.feature.meeting.suggestions

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import colision.composeapp.generated.resources.suggestions_empty
import colision.composeapp.generated.resources.suggestions_loading
import colision.composeapp.generated.resources.suggestions_slot_everyone_free
import colision.composeapp.generated.resources.suggestions_submit
import colision.composeapp.generated.resources.suggestions_subtitle
import colision.composeapp.generated.resources.suggestions_title
import com.anthooop.colision.core.design.Spacing
import com.anthooop.colision.feature.agenda.agenda.extractTime
import com.anthooop.colision.feature.agenda.agenda.parseIsoDate
import com.anthooop.colision.feature.agenda.agenda.rememberMonthNames
import com.anthooop.colision.feature.meeting.data.SuggestedSlot
import org.jetbrains.compose.resources.stringResource

@Composable
fun SuggestionsScreen(
    state: SuggestionsState,
    onIntent: (SuggestionsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = safe.calculateTopPadding(), bottom = safe.calculateBottomPadding()),
    ) {
        TopBar(onBack = { onIntent(SuggestionsIntent.BackTapped) })

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> Centered(stringResource(Res.string.suggestions_loading))
                state.slots.isEmpty() -> Centered(stringResource(Res.string.suggestions_empty))
                else -> SlotsList(state = state, onIntent = onIntent)
            }
        }

        BottomBar(
            canSubmit = state.canSubmit,
            onSubmit = { onIntent(SuggestionsIntent.SubmitTapped) },
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
            text = stringResource(Res.string.suggestions_title),
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
private fun SlotsList(state: SuggestionsState, onIntent: (SuggestionsIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.SP5, vertical = Spacing.SP3),
        verticalArrangement = Arrangement.spacedBy(Spacing.SP3),
    ) {
        item {
            Text(
                text = stringResource(Res.string.suggestions_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        itemsIndexed(state.slots, key = { _, slot -> slot.startsAt }) { index, slot ->
            SlotRow(
                slot = slot,
                selected = state.selectedIndex == index,
                onTap = { onIntent(SuggestionsIntent.SlotSelected(index)) },
            )
        }
    }
}

@Composable
private fun SlotRow(slot: SuggestedSlot, selected: Boolean, onTap: () -> Unit) {
    val months = rememberMonthNames()
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerBg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .border(1.75.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onTap)
            .padding(Spacing.SP4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateBadge(iso = slot.startsAt, months = months, selected = selected)
        Spacer(Modifier.width(Spacing.SP4))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${extractTime(slot.startsAt)} – ${extractTime(slot.endsAt)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.suggestions_slot_everyone_free),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DateBadge(iso: String, months: List<String>, selected: Boolean) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = parseIsoDate(iso, months),
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BottomBar(canSubmit: Boolean, onSubmit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.SP5, vertical = Spacing.SP3),
    ) {
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.suggestions_submit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
