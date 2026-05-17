package com.anthooop.colision.core.common

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus

class SupabaseAnonymousAuthManager(
    private val client: SupabaseClient,
    private val logger: Logger,
    private val crashReporter: CrashReporter,
) : AnonymousAuthManager {

    override suspend fun ensureSession(): Result<Unit> = runCatching {
        when (client.auth.sessionStatus.value) {
            is SessionStatus.Authenticated -> Unit
            else -> client.auth.signInAnonymously()
        }
    }.onFailure { t ->
        crashReporter.captureException(t, tag = TAG)
        logger.warn(TAG, "ensureSession failed", t)
    }

    // Refresh strategy = "option b" from docs/architecture.md gap #4: refresh
    // on every foreground so the refresh token never silently ages out. This
    // costs one auth round-trip per cold-start/foreground; cheap and avoids
    // a dedicated expiry-tracking codepath that would drift with supabase-kt
    // API changes.
    override suspend fun refreshIfNeeded(): Result<Unit> = runCatching {
        val status = client.auth.sessionStatus.value
        if (status !is SessionStatus.Authenticated) {
            client.auth.signInAnonymously()
            return@runCatching
        }
        client.auth.refreshCurrentSession()
    }.onFailure { t ->
        // Refresh token expired or otherwise broken — start over.
        crashReporter.captureException(t, tag = TAG)
        logger.warn(TAG, "refreshIfNeeded failed, re-signing anonymously", t)
        runCatching { client.auth.signOut() }
        runCatching { client.auth.signInAnonymously() }
    }

    private companion object {
        const val TAG = "AnonymousAuthManager"
    }
}
