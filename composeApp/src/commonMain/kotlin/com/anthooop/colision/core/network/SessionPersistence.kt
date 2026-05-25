package com.anthooop.colision.core.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

/**
 * Persists the Supabase anonymous session in a Jetpack DataStore Preferences
 * file (`colision-session.preferences_pb`), so cold-starting the app
 * restores the same `auth.users.id` instead of provisioning a fresh device
 * row each time.
 *
 * Without this, `member.device_id` (frozen at claim time) drifts away from
 * the device row of the current session after every restart — breaking
 * "who am I" lookups (badge "toi", agenda filter by member, ownership of
 * meetings, FCM token attribution, etc.).
 *
 * The file lives in `filesDir` on Android and the iOS Documents directory.
 * MVP-OK: the OS sandbox already isolates it per-app. If we need encryption
 * later, swap the path-backed DataStore for an EncryptedSharedPreferences /
 * Keychain-backed one without touching anything else.
 */
class DataStoreSessionManager(
    private val store: DataStore<Preferences>,
    private val json: Json = DefaultJson,
) : SessionManager {

    override suspend fun saveSession(session: UserSession) {
        store.edit { it[SESSION_KEY] = json.encodeToString(UserSession.serializer(), session) }
    }

    override suspend fun loadSession(): UserSession {
        val raw = store.data.first()[SESSION_KEY]
            ?: error("No session stored")
        return runCatching { json.decodeFromString(UserSession.serializer(), raw) }
            .getOrElse { cause ->
                // Stored blob is unreadable (schema drift, corruption) — wipe
                // it so we don't loop and let supabase-kt fall back to a
                // fresh anonymous sign-in.
                deleteSession()
                error("Persisted session is unreadable: ${cause.message}")
            }
    }

    override suspend fun deleteSession() {
        store.edit { it.remove(SESSION_KEY) }
    }

    companion object {
        private val SESSION_KEY = stringPreferencesKey("supabase_session")
        private val DefaultJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}

internal const val SESSION_PREFERENCES_FILE = "colision-session.preferences_pb"

/**
 * Builds the singleton [DataStore] backing the session manager. Called from
 * the platform Koin modules with a directory path that exists at app boot.
 */
fun createSessionDataStore(directory: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$directory/$SESSION_PREFERENCES_FILE".toPath() },
    )
