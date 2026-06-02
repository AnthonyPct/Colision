package com.anthooop.colision.feature.onboarding.joinidentity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JoinIdentityRoute(
    projectId: String,
    onNavigateToCommissions: (projectId: String, memberId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JoinIdentityViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is JoinIdentityEvent.NavigateToCommissions ->
                    onNavigateToCommissions(event.projectId, event.memberId)
                JoinIdentityEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    JoinIdentityScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
