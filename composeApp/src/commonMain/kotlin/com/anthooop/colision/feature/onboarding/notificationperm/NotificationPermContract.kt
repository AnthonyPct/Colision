package com.anthooop.colision.feature.onboarding.notificationperm

data class NotificationPermState(
    val isRequesting: Boolean = false,
)

sealed interface NotificationPermIntent {
    data object ActivateTapped : NotificationPermIntent
    data object LaterTapped : NotificationPermIntent
}

sealed interface NotificationPermEvent {
    data object NavigateToHome : NotificationPermEvent
}
