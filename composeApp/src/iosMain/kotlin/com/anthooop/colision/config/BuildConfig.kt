package com.anthooop.colision.config

// iOS does not use Gradle product flavors — environment switching happens
// via Xcode build configurations (Config.dev.xcconfig / Config.prod.xcconfig,
// declared in docs/architecture.md). For MVP we ship dev values; story 1.9's
// Swift bridge will eventually route through Info.plist so prod builds can
// override at archive time.
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
