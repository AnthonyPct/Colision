package com.anthooop.colision.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.AnonymousAuthManager
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.onboarding.data.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val authManager: AnonymousAuthManager,
    onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val sessionReady = MutableStateFlow(false)

    val startState: StateFlow<AppStartState> =
        combine(
            sessionReady,
            onboardingRepository.observeHasJoinedProject(),
        ) { ready, hasProject ->
            if (!ready) {
                AppStartState.Loading
            } else {
                AppStartState.Ready(if (hasProject) RootGraph.Home else RootGraph.Onboarding)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppStartState.Loading,
        )

    init {
        // Sign in anonymously before exposing the app: every server call is
        // RLS-gated and the `create_project` / `try_resolve_code` RPCs are
        // GRANT EXECUTE … TO authenticated only, so without a session every
        // call would 42501. ensureSession() is idempotent — it no-ops on
        // an existing authenticated status.
        viewModelScope.launch {
            authManager.ensureSession()
            sessionReady.value = true
        }
    }
}
