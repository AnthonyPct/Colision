package com.anthooop.colision.core.design

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Shared composition state used by every screen that needs to gate write
 * actions behind connectivity (FR39). The App-level Scaffold provides both
 * values; screens read them via [rememberOfflineGate].
 */
val LocalIsOnline = compositionLocalOf { true }
val LocalSnackbar = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalSnackbar not provided — wrap your composable in the App-level Scaffold.")
}
val LocalSnackbarScope = staticCompositionLocalOf<CoroutineScope> {
    error("LocalSnackbarScope not provided — wrap your composable in the App-level Scaffold.")
}

/** Connectivity-aware action handle returned by [rememberOfflineGate]. */
class OfflineGate(
    val isOnline: Boolean,
    private val onOfflineAttempt: () -> Unit,
) {
    /**
     * Runs [block] when online; otherwise surfaces a Snackbar with the
     * "Connexion requise pour cette action" message. Use this to wrap every
     * tap callback that mutates server state.
     */
    fun run(block: () -> Unit) {
        if (isOnline) block() else onOfflineAttempt()
    }
}

@Composable
fun rememberOfflineGate(offlineMessage: String): OfflineGate {
    val isOnline = LocalIsOnline.current
    val snackbar = LocalSnackbar.current
    val scope = LocalSnackbarScope.current
    return remember(isOnline, snackbar, scope, offlineMessage) {
        OfflineGate(
            isOnline = isOnline,
            onOfflineAttempt = {
                scope.launch {
                    snackbar.showSnackbar(offlineMessage)
                }
            },
        )
    }
}
