package com.anthooop.colision.core.common

interface CrashReporter {
    fun captureException(throwable: Throwable, tag: String? = null)
    fun captureMessage(message: String, tag: String? = null)
    fun addBreadcrumb(message: String, category: String? = null)
    fun setUserContext(deviceId: String?)
}

class NoopCrashReporter : CrashReporter {
    override fun captureException(throwable: Throwable, tag: String?) = Unit
    override fun captureMessage(message: String, tag: String?) = Unit
    override fun addBreadcrumb(message: String, category: String?) = Unit
    override fun setUserContext(deviceId: String?) = Unit
}
