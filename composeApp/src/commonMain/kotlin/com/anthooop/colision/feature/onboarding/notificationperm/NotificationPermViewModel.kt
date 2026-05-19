package com.anthooop.colision.feature.onboarding.notificationperm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.NotificationPermissionManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationPermViewModel(
    private val permissionManager: NotificationPermissionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationPermState())
    val state: StateFlow<NotificationPermState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NotificationPermEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<NotificationPermEvent> = _events.asSharedFlow()

    fun onIntent(intent: NotificationPermIntent) {
        when (intent) {
            NotificationPermIntent.ActivateTapped -> requestPermission()
            NotificationPermIntent.LaterTapped -> emit(NotificationPermEvent.NavigateToHome)
        }
    }

    private fun requestPermission() {
        if (_state.value.isRequesting) return
        _state.update { it.copy(isRequesting = true) }
        viewModelScope.launch {
            // The platform manager triggers the native dialog and waits for the
            // outcome. Granted or denied, the user always lands on Home — story
            // 2.7 AC: the app remains functional without the permission.
            runCatching { permissionManager.request() }
            // TODO(story 2.7 follow-up): if granted, register FCM / APNs token
            // on the device row. Track the work in a separate ticket once a
            // platform device-id is wired up.
            _state.update { it.copy(isRequesting = false) }
            emit(NotificationPermEvent.NavigateToHome)
        }
    }

    private fun emit(event: NotificationPermEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
