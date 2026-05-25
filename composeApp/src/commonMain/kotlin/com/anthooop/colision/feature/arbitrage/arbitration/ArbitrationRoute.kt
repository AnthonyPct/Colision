package com.anthooop.colision.feature.arbitrage.arbitration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArbitrationRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArbitrationViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ArbitrationEvent.NavigateBack -> onNavigateBack()
                ArbitrationEvent.Submitted -> onNavigateBack()
            }
        }
    }
    ArbitrationScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
