package com.anthooop.colision.feature.projecthub.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MemberCommissionsRoute(
    memberId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemberCommissionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(memberId) { viewModel.load(memberId) }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                MemberCommissionsEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    MemberCommissionsScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
