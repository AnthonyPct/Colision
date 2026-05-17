package com.anthooop.colision.core.common

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import io.sentry.kotlin.multiplatform.protocol.User

actual class PlatformCrashReporter actual constructor() : CrashReporter {

    override fun captureException(throwable: Throwable, tag: String?) {
        Sentry.captureException(throwable) { scope ->
            if (tag != null) scope.setTag("source", tag)
        }
    }

    override fun captureMessage(message: String, tag: String?) {
        Sentry.captureMessage(message) { scope ->
            if (tag != null) scope.setTag("source", tag)
        }
    }

    override fun addBreadcrumb(message: String, category: String?) {
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                this.message = message
                if (category != null) this.category = category
            },
        )
    }

    override fun setUserContext(deviceId: String?) {
        if (deviceId == null) {
            Sentry.setUser(null)
        } else {
            Sentry.setUser(User().apply { id = deviceId })
        }
    }
}
