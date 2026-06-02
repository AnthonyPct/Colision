package com.anthooop.colision.feature.meeting.conflicts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConflictsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSuggestions: () -> Unit,
    onMeetingCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConflictsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ConflictsEvent.NavigateBack -> onNavigateBack()
                ConflictsEvent.NavigateToSuggestions -> onNavigateToSuggestions()
                is ConflictsEvent.MeetingCreated -> onMeetingCreated()
            }
        }
    }
    ConflictsScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
