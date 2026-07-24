package com.anthooop.colision.feature.poll.createpoll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreatePollRoute(
    onNavigateBack: () -> Unit,
    onPollCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatePollViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CreatePollEvent.NavigateBack -> onNavigateBack()
                CreatePollEvent.PollCreated -> onPollCreated()
            }
        }
    }
    CreatePollScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
