package com.anthooop.colision.core.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

// Read-only Android impl. Actual request flow (registerForActivityResult)
// belongs to a dedicated Activity-scoped helper added when the Onboarding
// permission step is implemented in epic 2 (story 2.7).
class NotificationPermissionManagerAndroid(
    private val applicationContext: Context,
) : NotificationPermissionManager {

    override suspend fun currentStatus(): NotificationPermissionStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Pre-Android 13: notifications enabled by default.
            return NotificationPermissionStatus.Granted
        }
        val granted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        return if (granted) NotificationPermissionStatus.Granted else NotificationPermissionStatus.NotRequested
    }

    // Request must run in an Activity scope; epic 2 story 2.7 will introduce
    // an Activity-bound helper that calls into this manager. For now, return
    // the current status so callers don't have to special-case null.
    override suspend fun request(): NotificationPermissionStatus = currentStatus()
}
