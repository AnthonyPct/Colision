package com.anthooop.colision.feature.arbitrage.arbitration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.arbitration_submit_error
import com.anthooop.colision.core.design.LocalSnackbar
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArbitrationRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArbitrationViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ArbitrationEvent.NavigateBack -> onNavigateBack()
                ArbitrationEvent.Submitted -> onNavigateBack()
            }
        }
    }
    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbar.showSnackbar(getString(Res.string.arbitration_submit_error))
            viewModel.onIntent(ArbitrationIntent.ErrorDismissed)
        }
    }
    ArbitrationScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
