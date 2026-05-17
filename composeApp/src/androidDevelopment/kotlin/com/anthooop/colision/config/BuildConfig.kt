package com.anthooop.colision.config

// Development flavor — points at the "Collision" Supabase project
// (ref: uxmzeqlnrpydiiephfem, region: eu-west-1).
// The anon key is intentionally public (cf. docs/architecture.md
// § Infrastructure & Deployment) — RLS enforces project isolation.
//
// Sentry DSN and PostHog API key remain blank until ops provisions those
// projects; the SDKs no-op cleanly when their secret is blank.
actual object BuildConfig {
    actual val supabaseUrl: String = "https://uxmzeqlnrpydiiephfem.supabase.co"
    actual val supabaseAnonKey: String =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV4bXplcWxucnB5ZGlpZXBoZmVtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkwMzg1NDgsImV4cCI6MjA5NDYxNDU0OH0." +
            "0XrWNO1qSMfVK3E9FflmtrACSTbLjRdRap-N26O-k_A"
    actual val sentryDsn: String = ""
    actual val posthogApiKey: String = ""
    actual val posthogHost: String = "https://eu.i.posthog.com"
    actual val isDevelopmentFlavor: Boolean = true
}
