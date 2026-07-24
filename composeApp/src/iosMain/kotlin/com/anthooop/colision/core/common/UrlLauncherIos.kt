package com.anthooop.colision.core.common

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class UrlLauncherIos : UrlLauncher {
    override fun open(url: String) {
        if (url.isBlank()) return
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(
            url = nsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}
