package com.anthooop.colision.feature.onboarding.projectcommissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateProjectCommissionsRoute(
    projectId: String,
    onNavigateToShareCode: (projectId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateProjectCommissionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.load(projectId)
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateProjectCommissionsEvent.NavigateToShareCode ->
                    onNavigateToShareCode(event.projectId)
                CreateProjectCommissionsEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    CreateProjectCommissionsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
