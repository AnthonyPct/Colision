package com.anthooop.colision.core.common

actual class PlatformCrashReporter actual constructor() : CrashReporter {
    override fun captureException(throwable: Throwable, tag: String?) = Unit
    override fun captureMessage(message: String, tag: String?) = Unit
    override fun addBreadcrumb(message: String, category: String?) = Unit
    override fun setUserContext(deviceId: String?) = Unit
}
