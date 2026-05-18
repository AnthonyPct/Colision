package com.anthooop.colision.core.common

enum class NotificationPermissionStatus {
    NotRequested,
    Granted,
    Denied,
}

interface NotificationPermissionManager {
    suspend fun currentStatus(): NotificationPermissionStatus
    suspend fun request(): NotificationPermissionStatus
}
