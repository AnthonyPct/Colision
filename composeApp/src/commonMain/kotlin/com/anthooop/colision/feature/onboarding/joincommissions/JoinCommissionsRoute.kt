package com.anthooop.colision.feature.onboarding.joincommissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JoinCommissionsRoute(
    projectId: String,
    memberId: String,
    onNavigateToNotificationPermission: (projectId: String, memberId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JoinCommissionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(projectId, memberId) { viewModel.load(projectId, memberId) }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is JoinCommissionsEvent.NavigateToNotificationPermission ->
                    onNavigateToNotificationPermission(event.projectId, event.memberId)
                JoinCommissionsEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    JoinCommissionsScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
