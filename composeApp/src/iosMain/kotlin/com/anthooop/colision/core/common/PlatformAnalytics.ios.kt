package com.anthooop.colision.core.common

// iOS PostHog integration is added via SPM in iosApp/ (PostHog iOS SDK) and
// bridged through Swift. The KMP framework does not depend on a PostHog
// Kotlin/Native library — there isn't one. Until the Swift bridge lands,
// fall back to no-op so the Koin graph resolves on iOS without crashing.
actual class PlatformAnalytics actual constructor() : Analytics {
    override fun track(event: String, properties: Map<String, Any?>) = Unit
    override fun reset() = Unit
}
