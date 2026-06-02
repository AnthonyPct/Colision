package com.anthooop.colision.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.AnonymousAuthManager
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.onboarding.data.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppViewModel(
    private val authManager: AnonymousAuthManager,
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _startState = MutableStateFlow<AppStartState>(AppStartState.Loading)
    val startState: StateFlow<AppStartState> = _startState.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        // Resolve the start graph exactly once per app start, then freeze it.
        // Subsequent transitions (end-of-onboarding → Home, project deletion
        // → Onboarding) are driven by explicit navigation, not by re-evaluating
        // the gate — otherwise the gate flipping mid-onboarding (e.g. as soon
        // as the join code resolves and writes the project to Room) would
        // yank the user out of the flow before they pick an identity.
        viewModelScope.launch {
            // Anonymous sign-in must complete before we touch RLS-gated
            // endpoints. ensureSession() is idempotent.
            authManager.ensureSession()
            val hasProject = onboardingRepository.observeHasJoinedProject().first()
            _startState.value = AppStartState.Ready(
                if (hasProject) RootGraph.Home else RootGraph.Onboarding,
            )
        }
    }
}
