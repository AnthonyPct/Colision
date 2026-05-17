package com.anthooop.colision.config

expect object BuildConfig {
    val supabaseUrl: String
    val supabaseAnonKey: String
    val sentryDsn: String
    val posthogApiKey: String
    val posthogHost: String
    val isDevelopmentFlavor: Boolean
}
