package com.anthooop.colision.core.common

/**
 * Platform-specific source of the native push token.
 *
 * - Android: returns the current FCM registration token, refreshed by the
 *   [com.google.firebase.messaging.FirebaseMessagingService] callback.
 * - iOS: returns the APNs device token captured by the AppDelegate after
 *   `registerForRemoteNotifications` resolves.
 *
 * Returns `null` when the token isn't yet available (permission not granted,
 * Firebase init pending, APNs registration in-flight). Callers should treat
 * that as "try again on the next foreground" rather than an error.
 */
interface PushTokenProvider {
    val platform: PushPlatform
    suspend fun fetchToken(): String?
}
