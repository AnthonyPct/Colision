package com.anthooop.colision.core.common

import android.content.Context
import android.content.Intent
import android.net.Uri

class UrlLauncherAndroid(private val context: Context) : UrlLauncher {
    override fun open(url: String) {
        if (url.isBlank()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
