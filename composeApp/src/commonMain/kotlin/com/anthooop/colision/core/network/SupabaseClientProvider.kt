package com.anthooop.colision.core.network

import com.anthooop.colision.config.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest

// Single source of the Supabase client. Stories 2+ pull this through Koin
// (registered in CoreModule by story 1.8 wiring).
//
// Session storage uses a Jetpack DataStore-backed [SessionManager] so the
// anonymous auth session survives cold starts. Without persistence, each
// restart provisioned a fresh `auth.users` row → fresh `device` row → and
// `member.device_id` (frozen at the time the user claimed their identity)
// no longer matched the current device, breaking every "who am I" lookup.
object SupabaseClientProvider {
    fun create(
        sessionManager: SessionManager? = null,
        builder: SupabaseClientBuilder.() -> Unit = {},
    ): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth) {
                if (sessionManager != null) {
                    this.sessionManager = sessionManager
                }
            }
            install(Postgrest)
            install(Functions)
            builder()
        }
}
