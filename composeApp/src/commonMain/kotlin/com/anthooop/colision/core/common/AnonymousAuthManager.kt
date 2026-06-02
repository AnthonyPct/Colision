package com.anthooop.colision.core.common

interface AnonymousAuthManager {
    /**
     * Idempotent: ensures a Supabase anonymous session exists before any
     * RLS-gated call is issued. Safe to call at every cold start.
     */
    suspend fun ensureSession(): Result<Unit>

    /**
     * Called on foreground (onResume / scenePhase = active). Refreshes the
     * current session proactively when the refresh token has less than
     * [REFRESH_THRESHOLD_DAYS] days of life remaining (cf. architecture
     * gap #4, recommendation b).
     */
    suspend fun refreshIfNeeded(): Result<Unit>

    companion object {
        const val REFRESH_THRESHOLD_DAYS: Long = 7
    }
}
