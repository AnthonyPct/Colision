package com.anthooop.colision.feature.onboarding.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeRoute(
    onNavigateToCreateProject: () -> Unit,
    onNavigateToJoinCode: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                WelcomeEvent.NavigateToCreateProject -> onNavigateToCreateProject()
                WelcomeEvent.NavigateToJoinCode -> onNavigateToJoinCode()
            }
        }
    }
    WelcomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
