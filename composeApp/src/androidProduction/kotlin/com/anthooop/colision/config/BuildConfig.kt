package com.anthooop.colision.config

// TODO ops: fill these values once the Supabase production project + Sentry + PostHog
// production projects are provisioned (see PR description for the ops checklist).
actual object BuildConfig {
    actual val supabaseUrl: String = ""
    actual val supabaseAnonKey: String = ""
    actual val sentryDsn: String = ""
    actual val posthogApiKey: String = ""
    actual val posthogHost: String = "https://eu.i.posthog.com"
    actual val isDevelopmentFlavor: Boolean = false
}
