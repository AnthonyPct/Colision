package com.anthooop.colision.core.common

/** Opens an external URL (e.g. the app's store page) in the platform browser/store. */
interface UrlLauncher {
    fun open(url: String)
}
