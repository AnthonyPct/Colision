package com.anthooop.colision.feature.onboarding.notificationperm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.DeviceRepository
import com.anthooop.colision.core.common.NotificationPermissionManager
import com.anthooop.colision.core.common.NotificationPermissionStatus
import com.anthooop.colision.core.common.PushTokenProvider
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
    private val pushTokenProvider: PushTokenProvider,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(NotificationPermState())
    val state: StateFlow<NotificationPermState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<NotificationPermEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<NotificationPermEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: NotificationPermIntent) {
        when (intent) {
            NotificationPermIntent.ActivateTapped -> requestPermission()
            NotificationPermIntent.LaterTapped -> emit(NotificationPermEvent.NavigateToHome)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun requestPermission() {
        if (_state.value.isRequesting) return
        _state.update { it.copy(isRequesting = true) }
        viewModelScope.launch {
            // The platform manager triggers the native dialog and waits for the
            // outcome. Granted or denied, the user always lands on Home — story
            // 2.7 AC: the app remains functional without the permission.
            val status = runCatching { permissionManager.request() }.getOrNull()
            if (status == NotificationPermissionStatus.Granted) {
                registerPushToken()
            }
            _state.update { it.copy(isRequesting = false) }
            emit(NotificationPermEvent.NavigateToHome)
        }
    }

    // Fetches the FCM/APNs token from the platform and writes it onto the
    // Supabase `device` row (RLS-scoped to the current anonymous session).
    // Best-effort: a null token or a failed upsert is logged but never
    // blocks navigation — the next foreground sweep (or the next time the
    // user lands here) gets another shot.
    private suspend fun registerPushToken() {
        val token = runCatching { pushTokenProvider.fetchToken() }.getOrNull() ?: return
        deviceRepository.upsertToken(pushTokenProvider.platform, token)
    }

    private fun emit(event: NotificationPermEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
