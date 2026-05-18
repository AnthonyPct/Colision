package com.anthooop.colision.core.network

import com.anthooop.colision.config.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest

// Single source of the Supabase client. Stories 2+ pull this through Koin
// (registered in CoreModule by story 1.8 wiring).
//
// Session storage uses supabase-kt's built-in SessionManager — in-memory on
// JVM/Android, platform-equivalent on iOS. Re-signing anonymously on cold
// start is idempotent enough at MVP and avoids us shipping a custom secure
// storage layer. If we later need to persist sessions across cold starts
// (e.g. to avoid creating a new auth.users row when localStorage is wiped),
// plug a SessionManager backed by a real keychain wrapper at that point.
object SupabaseClientProvider {
    fun create(builder: SupabaseClientBuilder.() -> Unit = {}): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
            install(Functions)
            builder()
        }
}
