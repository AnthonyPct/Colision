package com.anthooop.colision.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.onboarding.data.OnboardingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    onboardingRepository: OnboardingRepository,
) : ViewModel() {

    val startState: StateFlow<AppStartState> =
        onboardingRepository.observeHasJoinedProject()
            .map<Boolean, AppStartState> { hasProject ->
                AppStartState.Ready(if (hasProject) RootGraph.Home else RootGraph.Onboarding)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AppStartState.Loading,
            )
}
