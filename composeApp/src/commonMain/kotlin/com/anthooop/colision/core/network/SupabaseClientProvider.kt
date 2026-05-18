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
// Session storage: supabase-kt 3.x exposes a SessionManager you can swap to
// route token persistence through our SecureStorage wrapper. That swap is
// deferred to a follow-up — at MVP the default (in-memory + platform
// equivalent) is fine since `signInAnonymously()` is idempotent enough for
// cold starts.
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
