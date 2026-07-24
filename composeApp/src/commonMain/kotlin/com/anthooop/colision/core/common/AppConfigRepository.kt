package com.anthooop.colision.core.common

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Remote app configuration (singleton row `app_config`), read at launch. */
data class AppConfig(
    val minSupportedVersion: String,
    val latestVersion: String,
    val androidStoreUrl: String,
    val iosStoreUrl: String,
    val updateMessage: String?,
)

@Serializable
data class AppConfigDto(
    @SerialName("min_supported_version") val minSupportedVersion: String,
    @SerialName("latest_version") val latestVersion: String,
    @SerialName("android_store_url") val androidStoreUrl: String,
    @SerialName("ios_store_url") val iosStoreUrl: String,
    @SerialName("update_message") val updateMessage: String? = null,
)

fun AppConfigDto.toModel(): AppConfig = AppConfig(
    minSupportedVersion = minSupportedVersion,
    latestVersion = latestVersion,
    androidStoreUrl = androidStoreUrl,
    iosStoreUrl = iosStoreUrl,
    updateMessage = updateMessage,
)

interface AppConfigRepository {
    /** Fetches the remote config; null when unavailable (fail-open: no prompt). */
    suspend fun fetch(): Result<AppConfig?>
}

class DefaultAppConfigRepository(
    private val supabase: SupabaseClient,
) : AppConfigRepository {
    override suspend fun fetch(): Result<AppConfig?> = runCatching {
        supabase.from("app_config")
            .select()
            .decodeList<AppConfigDto>()
            .firstOrNull()
            ?.toModel()
    }
}
