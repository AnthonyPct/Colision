package com.anthooop.colision.core.push

import com.anthooop.colision.core.common.CrashReporter
import com.anthooop.colision.core.common.Logger
import com.anthooop.colision.core.common.PushPlatform
import com.anthooop.colision.core.common.PushTokenProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Reads the current FCM registration token via the Firebase SDK. Returns
 * `null` (rather than throwing) when Firebase isn't initialized — typically
 * because `composeApp/google-services.json` wasn't provided at build time
 * (the google-services plugin auto-applies only when the file is present).
 * Callers treat null as "push disabled on this device" and just skip the
 * upsert.
 */
class FcmPushTokenProvider(
    private val logger: Logger,
    private val crashReporter: CrashReporter,
) : PushTokenProvider {

    override val platform: PushPlatform = PushPlatform.Android

    override suspend fun fetchToken(): String? {
        return try {
            suspendCancellableCoroutine<String?> { cont ->
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token -> cont.resume(token) }
                    .addOnFailureListener { t ->
                        logger.warn(TAG, "FCM getToken failed", t)
                        crashReporter.captureException(t, tag = TAG)
                        cont.resume(null)
                    }
            }
        } catch (t: Throwable) {
            // FirebaseApp not initialized (no google-services.json), or any
            // other init error — degrade gracefully so the rest of the app
            // keeps working.
            logger.warn(TAG, "FCM unavailable", t)
            null
        }
    }

    private companion object {
        const val TAG = "FcmPushTokenProvider"
    }
}
