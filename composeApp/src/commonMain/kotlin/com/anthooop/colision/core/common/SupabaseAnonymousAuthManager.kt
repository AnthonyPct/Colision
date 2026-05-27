package com.anthooop.colision.core.common

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SupabaseAnonymousAuthManager(
    private val client: SupabaseClient,
    private val logger: Logger,
    private val crashReporter: CrashReporter,
) : AnonymousAuthManager {

    // Serializes the read-then-sign-in critical section. Without it, concurrent
    // callers (AppViewModel.init + ProjectSyncManager foreground sync) both see
    // a non-Authenticated status before either completes, and each calls
    // signInAnonymously() → two auth.users + device rows on a single cold start
    // (bug #66).
    private val sessionMutex = Mutex()

    override suspend fun ensureSession(): Result<Unit> = runCatching {
        // Wait until Auth has finished loading the persisted session from
        // DataStore. Without this gate, `sessionStatus.value` is still
        // `Initializing` and we fall through to signInAnonymously(),
        // creating a fresh auth.users + device on every cold start — which
        // detaches the previously claimed member.device_id.
        client.auth.awaitInitialization()
        sessionMutex.withLock {
            when (client.auth.sessionStatus.value) {
                is SessionStatus.Authenticated -> Unit
                else -> client.auth.signInAnonymously()
            }
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
        client.auth.awaitInitialization()
        sessionMutex.withLock {
            val status = client.auth.sessionStatus.value
            if (status !is SessionStatus.Authenticated) {
                client.auth.signInAnonymously()
                return@withLock
            }
            client.auth.refreshCurrentSession()
        }
    }.onFailure { t ->
        // Refresh token expired or otherwise broken — start over.
        crashReporter.captureException(t, tag = TAG)
        logger.warn(TAG, "refreshIfNeeded failed, re-signing anonymously", t)
        sessionMutex.withLock {
            runCatching { client.auth.signOut() }
            runCatching { client.auth.signInAnonymously() }
        }
    }

    private companion object {
        const val TAG = "AnonymousAuthManager"
    }
}
