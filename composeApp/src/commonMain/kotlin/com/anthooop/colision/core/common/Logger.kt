package com.anthooop.colision.core.common

interface Logger {
    fun debug(tag: String, message: String, throwable: Throwable? = null)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
