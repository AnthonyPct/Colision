package com.anthooop.colision.core.common

// Cross-platform Analytics surface. Real impls are platform-specific:
// - Android: PostHogAnalyticsAndroid (PostHog SDK).
// - iOS: PostHogAnalyticsIos placeholder until the PostHog iOS SDK is added
//   via SPM in iosApp/.
expect class PlatformAnalytics() : Analytics

interface Analytics {
    fun track(event: String, properties: Map<String, Any?> = emptyMap())
    fun reset()
}

class NoopAnalytics : Analytics {
    override fun track(event: String, properties: Map<String, Any?>) = Unit
    override fun reset() = Unit
}
