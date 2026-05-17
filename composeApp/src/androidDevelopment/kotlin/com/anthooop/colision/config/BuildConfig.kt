package com.anthooop.colision.config

// TODO ops: fill these values once the Supabase development project + Sentry + PostHog
// projects are provisioned (see PR description for the ops checklist). Leaving them as
// empty strings is intentional — the app boots, but Supabase/Sentry/PostHog calls will
// no-op or fail loudly with a clear error.
actual object BuildConfig {
    actual val supabaseUrl: String = ""
    actual val supabaseAnonKey: String = ""
    actual val sentryDsn: String = ""
    actual val posthogApiKey: String = ""
    actual val posthogHost: String = "https://eu.i.posthog.com"
    actual val isDevelopmentFlavor: Boolean = true
}
