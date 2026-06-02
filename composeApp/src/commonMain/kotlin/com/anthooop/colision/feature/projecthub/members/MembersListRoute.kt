package com.anthooop.colision.feature.projecthub.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MembersListRoute(
    onNavigateBack: () -> Unit,
    onNavigateToCommissions: (memberId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MembersListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                MembersListEvent.NavigateBack -> onNavigateBack()
                is MembersListEvent.NavigateToCommissions -> onNavigateToCommissions(event.memberId)
            }
        }
    }
    MembersListScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
