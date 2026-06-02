import UIKit
import UserNotifications
import ComposeApp

/// Captures the APNs device token after iOS resolves
/// `registerForRemoteNotifications`, hex-encodes it, and forwards it to the
/// shared Kotlin module via `IosPushTokenHolder.shared`. The Kotlin side
/// then upserts it onto the Supabase `device` row after the user grants
/// notification permission (see `NotificationPermViewModel`).
///
/// Also routes notification taps to the existing `colision://` deep-link
/// scheme so the Compose NavController picks them up via its registered
/// `navDeepLink` patterns (see `ArbitrageGraph`).
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        // Kick off APNs registration unconditionally — iOS only actually
        // resolves the token if/when the user grants notification permission
        // (asked separately via NotificationPermissionManagerIos). Calling
        // this at launch shortens the path: by the time the user lands on
        // NotificationPermScreen and accepts, the token is typically already
        // in IosPushTokenHolder.
        DispatchQueue.main.async {
            application.registerForRemoteNotifications()
        }
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let hex = deviceToken.map { String(format: "%02x", $0) }.joined()
        IosPushTokenHolder.shared.setToken(token: hex)
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        NSLog("APNs registration failed: \(error.localizedDescription)")
        IosPushTokenHolder.shared.setToken(token: nil)
    }

    /// Foreground: iOS otherwise suppresses banners while the app is active.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }

    /// Tap: open the `colision://…` URI if the payload carries one. The
    /// dispatch_*_push Edge Functions inject `deep_link` for conflict and
    /// arbitration pushes.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        if let deepLink = userInfo["deep_link"] as? String,
           let url = URL(string: deepLink) {
            UIApplication.shared.open(url)
        }
        completionHandler()
    }
}
