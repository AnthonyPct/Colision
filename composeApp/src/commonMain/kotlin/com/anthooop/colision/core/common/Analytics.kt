package com.anthooop.colision.core.common

interface Analytics {
    fun track(event: String, properties: Map<String, Any?> = emptyMap())
    fun reset()
}

class NoopAnalytics : Analytics {
    override fun track(event: String, properties: Map<String, Any?>) = Unit
    override fun reset() = Unit
}
