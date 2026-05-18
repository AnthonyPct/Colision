package com.anthooop.colision.core.common

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

class NotificationPermissionManagerIos : NotificationPermissionManager {

    override suspend fun currentStatus(): NotificationPermissionStatus =
        suspendCancellableCoroutine { cont ->
            UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
                val status = when (settings?.authorizationStatus) {
                    UNAuthorizationStatusAuthorized,
                    UNAuthorizationStatusProvisional,
                    UNAuthorizationStatusEphemeral -> NotificationPermissionStatus.Granted
                    UNAuthorizationStatusDenied -> NotificationPermissionStatus.Denied
                    UNAuthorizationStatusNotDetermined -> NotificationPermissionStatus.NotRequested
                    else -> NotificationPermissionStatus.NotRequested
                }
                cont.resume(status)
            }
        }

    override suspend fun request(): NotificationPermissionStatus =
        suspendCancellableCoroutine { cont ->
            val options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
            UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(options) { granted, _ ->
                cont.resume(if (granted) NotificationPermissionStatus.Granted else NotificationPermissionStatus.Denied)
            }
        }
}
