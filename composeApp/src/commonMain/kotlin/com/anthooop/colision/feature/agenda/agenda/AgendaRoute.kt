package com.anthooop.colision.feature.agenda.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AgendaRoute(
    onNavigateToMeetingDetail: (String) -> Unit,
    onNavigateToCreateMeeting: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AgendaEvent.NavigateToMeetingDetail -> onNavigateToMeetingDetail(event.meetingId)
                AgendaEvent.NavigateToCreateMeeting -> onNavigateToCreateMeeting()
            }
        }
    }
    AgendaScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
