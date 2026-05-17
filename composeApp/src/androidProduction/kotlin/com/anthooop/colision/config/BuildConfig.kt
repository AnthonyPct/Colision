package com.anthooop.colision.config

// Production flavor.
// - supabaseUrl / supabaseAnonKey: still empty — a separate production
//   Supabase project will be provisioned later.
// - sentryDsn: shared with the development flavor. Sentry differentiates
//   events via the `environment: "production"` tag set by
//   ColisionApplication.initSentry() (driven by isDevelopmentFlavor). Swap
//   to a dedicated DSN here the day the architecture's two-project intent
//   is honoured.
// - posthogApiKey: pending PostHog project provisioning.
actual object BuildConfig {
    actual val supabaseUrl: String = ""
    actual val supabaseAnonKey: String = ""
    actual val sentryDsn: String =
        "https://6ea8ec9466dd998073c6d372d39885f4@o4511406962966528.ingest.de.sentry.io/4511406968406096"
    actual val posthogApiKey: String = ""
    actual val posthogHost: String = "https://eu.i.posthog.com"
    actual val isDevelopmentFlavor: Boolean = false
}
