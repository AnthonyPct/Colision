package com.anthooop.colision.feature.onboarding.joincode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JoinCodeRoute(
    onNavigateToConfirm: (projectId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JoinCodeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is JoinCodeEvent.NavigateToConfirm -> onNavigateToConfirm(event.projectId)
                JoinCodeEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    JoinCodeScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
