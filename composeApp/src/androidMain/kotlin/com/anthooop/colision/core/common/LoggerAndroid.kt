package com.anthooop.colision.core.common

import android.util.Log
import com.anthooop.colision.config.BuildConfig

class LoggerAndroid(
    private val isDevelopmentFlavor: Boolean = BuildConfig.isDevelopmentFlavor,
) : Logger {
    override fun debug(tag: String, message: String, throwable: Throwable?) {
        if (isDevelopmentFlavor) Log.d(tag, message, throwable)
    }

    override fun info(tag: String, message: String) {
        if (isDevelopmentFlavor) Log.i(tag, message)
    }

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
