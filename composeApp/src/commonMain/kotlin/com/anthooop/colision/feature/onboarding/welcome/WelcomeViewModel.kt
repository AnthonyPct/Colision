package com.anthooop.colision.feature.onboarding.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(WelcomeState())
    val state: StateFlow<WelcomeState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<WelcomeEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<WelcomeEvent> = _events.asSharedFlow()

    fun onIntent(intent: WelcomeIntent) {
        when (intent) {
            WelcomeIntent.CreateProjectTapped -> emit(WelcomeEvent.NavigateToCreateProject)
            WelcomeIntent.JoinProjectTapped -> emit(WelcomeEvent.NavigateToJoinCode)
        }
    }

    private fun emit(event: WelcomeEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
