package com.anthooop.colision.feature.onboarding.joinconfirm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JoinConfirmRoute(
    projectId: String,
    onNavigateToIdentity: (projectId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JoinConfirmViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) {
        viewModel.load(projectId)
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is JoinConfirmEvent.NavigateToIdentity -> onNavigateToIdentity(event.projectId)
                JoinConfirmEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    JoinConfirmScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
