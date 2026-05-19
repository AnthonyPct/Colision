package com.anthooop.colision.feature.projecthub.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.share_code_copied_snackbar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectSettingsRoute(
    onNavigateToCommissions: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onProjectReleased: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val copiedMessage = stringResource(Res.string.share_code_copied_snackbar)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProjectSettingsEvent.NavigateToCommissions -> onNavigateToCommissions()
                ProjectSettingsEvent.NavigateToMembers -> onNavigateToMembers()
                ProjectSettingsEvent.NavigateToWelcome -> onProjectReleased()
                is ProjectSettingsEvent.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(event.text))
                    snackbarHostState.showSnackbar(copiedMessage)
                }
            }
        }
    }

    ProjectSettingsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
