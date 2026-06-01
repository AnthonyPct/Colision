package com.anthooop.colision.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.anthooop.colision.R

/** Single source of truth for the Android notification channel IDs. */
object NotificationChannels {

    /** All Colision push notifications (meetings, conflicts, arbitrations). */
    const val PUSH = "colision_push"

    /**
     * Idempotent: re-creating a channel with the same id is a no-op. Called
     * once from [com.anthooop.colision.ColisionApplication.onCreate] so the
     * channel exists before the first FCM message arrives.
     */
    fun ensureCreated(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        val channel = NotificationChannel(
            PUSH,
            context.getString(R.string.notif_channel_push_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notif_channel_push_description)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}
