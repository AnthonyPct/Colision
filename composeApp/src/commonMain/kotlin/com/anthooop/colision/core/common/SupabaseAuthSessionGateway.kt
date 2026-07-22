package com.anthooop.colision.core.common

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus

/**
 * Real [AuthSessionGateway] backed by supabase-kt. Deliberately dependency-thin
 * and logic-free: it only translates supabase-kt calls/state into the gateway
 * contract. All decision-making lives in [SupabaseAnonymousAuthManager], which
 * is unit-tested against a fake gateway.
 */
class SupabaseAuthSessionGateway(
    private val client: SupabaseClient,
) : AuthSessionGateway {

    override suspend fun awaitInitialization() = client.auth.awaitInitialization()

    override fun status(): AuthStatus = when (client.auth.sessionStatus.value) {
        is SessionStatus.Authenticated -> AuthStatus.Authenticated
        is SessionStatus.RefreshFailure -> AuthStatus.RefreshFailure
        is SessionStatus.NotAuthenticated -> AuthStatus.NoSession
        // Initializing (or any state added by a future supabase-kt) is treated
        // as "not ready" — the manager will not sign in blindly on it.
        else -> AuthStatus.Initializing
    }

    override suspend fun hasStoredSession(): Boolean =
        // loadSessionOrNull() reads the persisted blob without mutating it and
        // works even while status is RefreshFailure/Initializing.
        client.auth.sessionManager.loadSessionOrNull() != null

    override suspend fun recoverStoredSession(): Result<Unit> = runCatching {
        val stored = client.auth.sessionManager.loadSessionOrNull()
            ?: error("No persisted session to recover")
        // importSession re-activates the stored identity; autoRefresh (defaulting
        // to config.alwaysAutoRefresh = true) refreshes an expired access token
        // in place — without ever provisioning a new auth.users row.
        client.auth.importSession(stored)
    }

    override suspend fun signInAnonymously(): Result<Unit> = runCatching {
        client.auth.signInAnonymously()
    }

    override suspend fun refreshCurrentSession(): Result<Unit> = runCatching {
        client.auth.refreshCurrentSession()
    }
}
