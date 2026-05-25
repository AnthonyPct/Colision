package com.anthooop.colision.feature.meeting.editmeeting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingEvent
import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditMeetingRoute(
    onNavigateBack: () -> Unit,
    onMeetingUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditMeetingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CreateMeetingEvent.NavigateBack -> onNavigateBack()
                is CreateMeetingEvent.MeetingCreated -> onMeetingUpdated()
                CreateMeetingEvent.NavigateToConflicts -> Unit // edit flow doesn't fork to conflicts in 4.5
            }
        }
    }
    CreateMeetingScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
