package com.anthooop.colision.core.push

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.anthooop.colision.MainActivity
import com.anthooop.colision.R
import com.anthooop.colision.core.common.DeviceRepository
import com.anthooop.colision.core.common.PushPlatform
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Receives data-only FCM messages emitted by the Supabase Edge Functions
 * (`dispatch_meeting_push`, `dispatch_conflict_push`,
 * `dispatch_arbitration_push`, `dispatch_meeting_change_push`). Each payload
 * carries `type`, `meeting_id`, `title`, `body`, and (for conflict/arbitration
 * pushes) a ready-made `deep_link` that the existing `colision://` scheme
 * routes via the navigation graph's deep-link patterns.
 */
class ColisionFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val deviceRepository: DeviceRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // FCM rotated the registration token (reinstall, restore from backup,
        // app data cleared). Persist it to the Supabase `device` row so the
        // Edge Functions reach the right handset on the next dispatch.
        scope.launch { deviceRepository.upsertToken(PushPlatform.Android, token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = data["title"] ?: getString(R.string.app_name)
        val body = data["body"].orEmpty()
        val deepLink = data["deep_link"]
        val notificationId = data["meeting_id"]?.hashCode()
            ?: System.currentTimeMillis().toInt()
        showNotification(notificationId, title, body, deepLink)
    }

    private fun showNotification(id: Int, title: String, body: String, deepLink: String?) {
        // PendingIntent → MainActivity. When deepLink is non-null, the intent
        // carries the `colision://…` URI; NavController's deepLinks list
        // (see ArbitrageGraph etc.) consumes it after onCreate.
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (deepLink != null) {
                action = Intent.ACTION_VIEW
                data = Uri.parse(deepLink)
            }
        }
        val pending = PendingIntent.getActivity(
            this,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.PUSH)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // POST_NOTIFICATIONS permission is gated by NotificationPermViewModel
        // earlier in onboarding. If the user denied it, NotificationManagerCompat
        // silently no-ops — that's the documented Story 2.7 behaviour.
        NotificationManagerCompat.from(this).notify(id, notification)
    }
}
