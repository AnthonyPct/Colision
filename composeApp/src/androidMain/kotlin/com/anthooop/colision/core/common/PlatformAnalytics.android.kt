package com.anthooop.colision.core.common

import com.posthog.PostHog

// Wraps the PostHog Android SDK. PostHog must already be initialised via
// PostHogAndroid.setup(...) in ColisionApplication.onCreate before any
// track() / reset() call (Koin resolves the singleton lazily, so the order
// is: Application.onCreate → setup PostHog → Activity creates → first
// resolution of Analytics).
actual class PlatformAnalytics actual constructor() : Analytics {

    override fun track(event: String, properties: Map<String, Any?>) {
        // PostHog Android's properties Map<String, Any> rejects nulls — strip them.
        val nonNull: Map<String, Any> = properties.mapNotNull { (k, v) -> v?.let { k to it } }.toMap()
        PostHog.capture(event = event, properties = nonNull)
    }

    override fun reset() {
        PostHog.reset()
    }
}
