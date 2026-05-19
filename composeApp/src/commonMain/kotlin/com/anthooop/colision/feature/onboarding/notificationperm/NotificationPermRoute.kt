package com.anthooop.colision.feature.onboarding.notificationperm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationPermRoute(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationPermViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                NotificationPermEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }
    NotificationPermScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
