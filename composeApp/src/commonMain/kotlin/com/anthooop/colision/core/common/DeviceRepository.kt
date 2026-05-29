package com.anthooop.colision.core.common

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Writes the device's push token to the Supabase `device` row associated
 * with the current anonymous session. RLS limits the UPDATE to the
 * caller's own row (`auth_user_id = auth.uid()`), so the client doesn't
 * need to supply a filter — Postgrest applies it implicitly.
 */
interface DeviceRepository {
    suspend fun upsertToken(platform: PushPlatform, token: String): Result<Unit>
}

class SupabaseDeviceRepository(
    private val supabase: SupabaseClient,
    private val logger: Logger,
    private val crashReporter: CrashReporter,
) : DeviceRepository {

    @Serializable
    private data class DeviceTokenUpdate(
        @SerialName("fcm_token") val fcmToken: String? = null,
        @SerialName("apns_token") val apnsToken: String? = null,
        @SerialName("platform") val platform: String,
    )

    override suspend fun upsertToken(platform: PushPlatform, token: String): Result<Unit> = runCatching {
        val payload = when (platform) {
            PushPlatform.Android -> DeviceTokenUpdate(fcmToken = token, platform = platform.wire)
            PushPlatform.Ios -> DeviceTokenUpdate(apnsToken = token, platform = platform.wire)
        }
        // RLS restricts the update to the row matching auth.uid(), so an
        // empty filter is safe and avoids round-tripping the user id.
        supabase.from("device").update(payload)
        Unit
    }.onFailure { t ->
        crashReporter.captureException(t, tag = TAG)
        logger.warn(TAG, "upsertToken failed for $platform", t)
    }

    private companion object {
        const val TAG = "DeviceRepository"
    }
}
