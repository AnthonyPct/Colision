package com.anthooop.colision.core.common

/**
 * Narrow seam over the Supabase auth surface that [SupabaseAnonymousAuthManager]
 * needs. It exists so the manager's identity-preservation logic is unit-testable
 * without a live [io.github.jan.supabase.SupabaseClient], and so every bit of
 * supabase-kt coupling lives in one thin adapter ([SupabaseAuthSessionGateway]).
 */
interface AuthSessionGateway {

    /** Suspends until Auth has finished loading the persisted session. */
    suspend fun awaitInitialization()

    /** Current auth status, collapsed to the four cases the manager reacts to. */
    fun status(): AuthStatus

    /**
     * True when a session blob is persisted on device (any previous identity),
     * regardless of whether it is currently active or refreshable. Reads storage
     * without mutating it.
     */
    suspend fun hasStoredSession(): Boolean

    /**
     * Re-activates the persisted identity (import + refresh if the access token
     * expired) instead of creating a new one. Fails if there is nothing to
     * recover.
     */
    suspend fun recoverStoredSession(): Result<Unit>

    /** Creates a brand-new anonymous identity. Only valid on a genuine first run. */
    suspend fun signInAnonymously(): Result<Unit>

    /** Refreshes the currently active session. */
    suspend fun refreshCurrentSession(): Result<Unit>
}

/**
 * The subset of supabase-kt `SessionStatus` the manager branches on. Keeping
 * [AuthStatus.NoSession] and [AuthStatus.RefreshFailure] distinct is the whole
 * point of the fix: a failed refresh (flaky connectivity) must never be mistaken
 * for "no identity", or we mint a new anonymous user and orphan the claimed
 * `member.device_id` — the "ghost" lockout (incidents 2026-07-16 / 2026-07-22).
 */
enum class AuthStatus {
    /** A valid session is loaded. */
    Authenticated,

    /** No session is stored or active — genuine first run, or after sign-out. */
    NoSession,

    /**
     * A stored session exists but its last refresh failed (typically flaky
     * connectivity). Recoverable — must NOT be treated as "no identity".
     */
    RefreshFailure,

    /** Auth has not finished initializing yet. */
    Initializing,
}
