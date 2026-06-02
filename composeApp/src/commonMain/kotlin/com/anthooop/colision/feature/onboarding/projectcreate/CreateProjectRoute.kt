package com.anthooop.colision.feature.onboarding.projectcreate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateProjectRoute(
    onNavigateToShareCode: (projectId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateProjectViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateProjectEvent.NavigateToShareCode -> onNavigateToShareCode(event.projectId)
                CreateProjectEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    CreateProjectScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
