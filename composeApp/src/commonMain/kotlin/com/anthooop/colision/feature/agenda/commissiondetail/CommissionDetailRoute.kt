package com.anthooop.colision.feature.agenda.commissiondetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CommissionDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToMeetingDetail: (String) -> Unit,
    onNavigateToCreateMeeting: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommissionDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CommissionDetailEvent.NavigateBack -> onNavigateBack()
                is CommissionDetailEvent.NavigateToMeetingDetail -> onNavigateToMeetingDetail(event.meetingId)
                CommissionDetailEvent.NavigateToCreateMeeting -> onNavigateToCreateMeeting()
            }
        }
    }
    CommissionDetailScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
