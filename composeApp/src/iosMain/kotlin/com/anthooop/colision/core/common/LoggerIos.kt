package com.anthooop.colision.core.common

import com.anthooop.colision.config.BuildKonfig
import platform.Foundation.NSLog

class LoggerIos(
    private val isDevelopmentFlavor: Boolean = BuildKonfig.IS_DEVELOPMENT_FLAVOR,
) : Logger {
    override fun debug(tag: String, message: String, throwable: Throwable?) {
        if (isDevelopmentFlavor) {
            NSLog("[%s] DEBUG: %s%s", tag, message, throwable?.let { " — ${it.message}" }.orEmpty())
        }
    }

    override fun info(tag: String, message: String) {
        if (isDevelopmentFlavor) NSLog("[%s] INFO: %s", tag, message)
    }

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        NSLog("[%s] WARN: %s%s", tag, message, throwable?.let { " — ${it.message}" }.orEmpty())
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        NSLog("[%s] ERROR: %s%s", tag, message, throwable?.let { " — ${it.message}" }.orEmpty())
    }
}
