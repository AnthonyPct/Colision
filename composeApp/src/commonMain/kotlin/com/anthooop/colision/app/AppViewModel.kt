package com.anthooop.colision.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.config.BuildKonfig
import com.anthooop.colision.core.common.AnonymousAuthManager
import com.anthooop.colision.core.common.AppConfig
import com.anthooop.colision.core.common.AppConfigRepository
import com.anthooop.colision.core.common.MobilePlatform
import com.anthooop.colision.core.common.UrlLauncher
import com.anthooop.colision.core.common.VersionCompare
import com.anthooop.colision.core.common.currentPlatform
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.onboarding.data.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val authManager: AnonymousAuthManager,
    private val onboardingRepository: OnboardingRepository,
    private val appConfigRepository: AppConfigRepository,
    private val urlLauncher: UrlLauncher,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _startState = MutableStateFlow<AppStartState>(AppStartState.Loading)
    val startState: StateFlow<AppStartState> = _startState.asStateFlow()

    private val _updateState = MutableStateFlow<AppUpdateState>(AppUpdateState.None)
    val updateState: StateFlow<AppUpdateState> = _updateState.asStateFlow()

    // Store URL resolved for the current platform once the config is loaded.
    private var pendingStoreUrl: String = ""

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onUpdateTapped() {
        urlLauncher.open(pendingStoreUrl)
    }

    /** Dismiss only allowed for optional updates; forced ones stay on screen. */
    fun onUpdateDismissed() {
        _updateState.update { current ->
            if (current is AppUpdateState.Available && !current.forced) AppUpdateState.None else current
        }
    }

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
        // Best-effort, non-blocking update check. Never gates app start: a
        // failed/absent config simply shows no prompt.
        viewModelScope.launch { checkForUpdate() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private suspend fun checkForUpdate() {
        val config = appConfigRepository.fetch().getOrNull() ?: return
        val installed = BuildKonfig.APP_VERSION
        pendingStoreUrl = when (currentPlatform) {
            MobilePlatform.Android -> config.androidStoreUrl
            MobilePlatform.Ios -> config.iosStoreUrl
        }
        _updateState.value = resolveUpdateState(installed, config)
    }
}

/**
 * Pure decision: forced when below the minimum supported version, optional when
 * merely behind the latest, none otherwise. Extracted for unit testing.
 */
internal fun resolveUpdateState(installed: String, config: AppConfig): AppUpdateState = when {
    VersionCompare.isOutdated(installed, config.minSupportedVersion) ->
        AppUpdateState.Available(forced = true, message = config.updateMessage)
    VersionCompare.isOutdated(installed, config.latestVersion) ->
        AppUpdateState.Available(forced = false, message = config.updateMessage)
    else -> AppUpdateState.None
}

sealed interface AppUpdateState {
    data object None : AppUpdateState
    data class Available(val forced: Boolean, val message: String?) : AppUpdateState
}
