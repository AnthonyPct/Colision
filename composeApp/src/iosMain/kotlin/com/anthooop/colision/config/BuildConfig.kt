package com.anthooop.colision.config

// iOS does not use Gradle product flavors — environment switching happens
// via Xcode build configurations (Config.dev.xcconfig / Config.prod.xcconfig,
// declared in docs/architecture.md). Story 1.9 wires the real values
// through Info.plist + a Swift-side bridge; until then the framework ships
// with empty-string placeholders and assumes the development environment.
actual object BuildConfig {
    actual val supabaseUrl: String = ""
    actual val supabaseAnonKey: String = ""
    actual val sentryDsn: String = ""
    actual val posthogApiKey: String = ""
    actual val posthogHost: String = "https://eu.i.posthog.com"
    actual val isDevelopmentFlavor: Boolean = true
}
