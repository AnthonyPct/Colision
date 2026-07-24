package com.anthooop.colision.feature.poll.pollslist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PollsListRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PollsListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PollsListEvent.NavigateToDetail -> onNavigateToDetail(event.pollId)
                PollsListEvent.NavigateToCreate -> onNavigateToCreate()
            }
        }
    }
    PollsListScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
