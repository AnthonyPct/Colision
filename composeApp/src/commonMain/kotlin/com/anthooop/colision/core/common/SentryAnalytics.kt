package com.anthooop.colision.core.common

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel

// Product events route through Sentry's captureMessage at INFO level. Each
// unique event name groups into a single Sentry Issue, so the Issues
// dashboard doubles as a basic event counter. Properties are attached as
// tags (string-coerced) so they're filterable in Sentry's search.
//
// Not designed for funnels or retention cohorts — at MVP scale with few
// users that's fine. The day we need real product analytics we can swap
// the Koin binding from SentryAnalytics to a dedicated impl without
// touching call sites.
class SentryAnalytics : Analytics {

    override fun track(event: String, properties: Map<String, Any?>) {
        Sentry.captureMessage("event:$event") { scope ->
            scope.level = SentryLevel.INFO
            scope.setTag("event_name", event)
            properties.forEach { (key, value) ->
                if (value != null) scope.setTag(key, value.toString())
            }
        }
    }

    override fun reset() {
        Sentry.setUser(null)
    }
}
