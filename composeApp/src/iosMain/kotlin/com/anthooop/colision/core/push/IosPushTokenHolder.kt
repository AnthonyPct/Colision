package com.anthooop.colision.core.push

import com.anthooop.colision.core.common.PushPlatform
import com.anthooop.colision.core.common.PushTokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Bridge between the Swift AppDelegate and Kotlin. The AppDelegate captures
 * the APNs device token via
 * `application(_:didRegisterForRemoteNotificationsWithDeviceToken:)`, hex-
 * encodes it, and writes it here. The Kotlin side ([ApnsPushTokenProvider])
 * reads it back when [com.anthooop.colision.feature.onboarding.notificationperm.NotificationPermViewModel]
 * registers tokens after a successful permission grant.
 *
 * Exposed as a singleton Kotlin object so Swift can reach it as
 * `IosPushTokenHolder.shared` through the generated ComposeApp framework.
 */
object IosPushTokenHolder {
    private val tokenState = MutableStateFlow<String?>(null)

    /** Called from Swift after `didRegisterForRemoteNotifications`. */
    fun setToken(token: String?) {
        tokenState.value = token
    }

    /**
     * Suspends up to 5s waiting for APNs to deliver the token. Returns null
     * if registration is still pending — callers treat that as "try again
     * next foreground" rather than as a hard failure.
     */
    suspend fun awaitToken(): String? = withTimeoutOrNull(5.seconds) {
        tokenState.filterNotNull().first()
    }
}

/** Bridges [IosPushTokenHolder] into the platform-agnostic [PushTokenProvider]. */
class ApnsPushTokenProvider : PushTokenProvider {
    override val platform: PushPlatform = PushPlatform.Ios
    override suspend fun fetchToken(): String? = IosPushTokenHolder.awaitToken()
}
