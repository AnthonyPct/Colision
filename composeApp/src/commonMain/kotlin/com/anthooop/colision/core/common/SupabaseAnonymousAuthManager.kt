package com.anthooop.colision.core.common

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the "keep one stable anonymous identity per install" policy.
 *
 * The cardinal rule: **never create a new anonymous identity while a previous
 * one is persisted on device.** Doing so provisions a fresh `auth.users` +
 * `device` row, so the `member.device_id` frozen at claim time no longer
 * matches the current device — the user becomes a nameless "ghost" and can't
 * re-pick their name (the RLS claim policy needs `device_id IS NULL`). This is
 * what happened on 2026-07-16 (a full council-meeting room on congested signal:
 * refresh failed, and the old code fell straight through to signInAnonymously()).
 */
class SupabaseAnonymousAuthManager(
    private val gateway: AuthSessionGateway,
    private val logger: Logger,
) : AnonymousAuthManager {

    // Serializes the read-then-act critical section. Without it, concurrent
    // callers (AppViewModel.init + ProjectSyncManager foreground sync) can both
    // observe a non-Authenticated status before either completes and each act on
    // it → two auth.users + device rows on a single cold start (bug #66).
    private val sessionMutex = Mutex()

    override suspend fun ensureSession(): Result<Unit> = runCatching {
        // Gate on initialization: until Auth has loaded the persisted session,
        // status() is Initializing and we must not sign in — that alone used to
        // mint a fresh identity on every cold start.
        gateway.awaitInitialization()
        sessionMutex.withLock {
            when (gateway.status()) {
                AuthStatus.Authenticated -> Unit

                // Stored identity exists but its refresh failed (flaky
                // connectivity, or the process was killed mid-refresh under
                // memory pressure). The in-memory session can end up WITHOUT a
                // usable refresh token, so calling refreshCurrentSession() throws
                // "No refresh token found in current session" (Sentry COLISION-A).
                // Re-import the persisted blob instead — it still carries a valid
                // refresh token (saveSession persists the whole session), and
                // autoRefresh renews the access token in place. NEVER sign in.
                AuthStatus.RefreshFailure ->
                    recoverOrRefresh()

                AuthStatus.NoSession ->
                    if (gateway.hasStoredSession()) {
                        // A persisted identity that just isn't active yet —
                        // re-use it rather than provisioning a new one.
                        gateway.recoverStoredSession().getOrThrow()
                    } else {
                        // Genuine first run: no identity has ever been stored.
                        gateway.signInAnonymously().getOrThrow()
                    }

                // Should not occur after awaitInitialization(); never sign in
                // blindly while the persisted session might still be loading.
                AuthStatus.Initializing ->
                    error("Auth still initializing after awaitInitialization()")
            }
        }
    }.onFailure { t ->
        // Recoverable and self-healing (offline, or killed mid-refresh): the
        // caller keeps the cached data and retries on the next foreground. This
        // is NOT a crash, so it is deliberately not reported to Sentry — only
        // logged — to avoid drowning the dashboard in transient auth noise.
        logger.warn(TAG, "ensureSession failed; keeping stored identity", t)
    }

    override suspend fun refreshIfNeeded(): Result<Unit> = runCatching {
        gateway.awaitInitialization()
        sessionMutex.withLock {
            when (gateway.status()) {
                AuthStatus.Authenticated ->
                    gateway.refreshCurrentSession().getOrThrow()

                AuthStatus.RefreshFailure ->
                    recoverOrRefresh()

                AuthStatus.NoSession ->
                    if (gateway.hasStoredSession()) {
                        gateway.recoverStoredSession().getOrThrow()
                    } else {
                        gateway.signInAnonymously().getOrThrow()
                    }

                AuthStatus.Initializing ->
                    error("Auth still initializing after awaitInitialization()")
            }
        }
    }.onFailure { t ->
        // Refresh failed (typically offline). Keep the existing identity and let
        // the next foreground retry — do NOT re-sign anonymously, which is the
        // exact drift that created ghost members. Not reported to Sentry.
        logger.warn(TAG, "refreshIfNeeded failed; keeping stored identity", t)
    }

    /**
     * Recovery for [AuthStatus.RefreshFailure]: prefer re-importing the persisted
     * session (which holds a valid refresh token) over refreshCurrentSession()
     * on a possibly token-less in-memory session. Falls back to a direct refresh
     * only if, unexpectedly, nothing is persisted.
     */
    private suspend fun recoverOrRefresh() {
        if (gateway.hasStoredSession()) {
            gateway.recoverStoredSession().getOrThrow()
        } else {
            gateway.refreshCurrentSession().getOrThrow()
        }
    }

    private companion object {
        const val TAG = "AnonymousAuthManager"
    }
}
