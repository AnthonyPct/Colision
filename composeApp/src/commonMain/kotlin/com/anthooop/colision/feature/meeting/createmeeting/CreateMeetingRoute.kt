package com.anthooop.colision.feature.meeting.createmeeting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateMeetingRoute(
    onNavigateBack: () -> Unit,
    onNavigateToConflicts: () -> Unit,
    onMeetingCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateMeetingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CreateMeetingEvent.NavigateBack -> onNavigateBack()
                is CreateMeetingEvent.MeetingCreated -> onMeetingCreated()
                CreateMeetingEvent.NavigateToConflicts -> onNavigateToConflicts()
            }
        }
    }
    CreateMeetingScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
