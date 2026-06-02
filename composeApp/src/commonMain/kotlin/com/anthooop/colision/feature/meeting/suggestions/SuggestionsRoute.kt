package com.anthooop.colision.feature.meeting.suggestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SuggestionsRoute(
    onNavigateBack: () -> Unit,
    onMeetingCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SuggestionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SuggestionsEvent.NavigateBack -> onNavigateBack()
                is SuggestionsEvent.MeetingCreated -> onMeetingCreated()
            }
        }
    }
    SuggestionsScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
