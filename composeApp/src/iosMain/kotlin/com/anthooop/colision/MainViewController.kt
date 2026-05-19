package com.anthooop.colision

import androidx.compose.ui.window.ComposeUIViewController
import com.anthooop.colision.app.App
import com.anthooop.colision.core.common.ProjectSyncManager
import com.anthooop.colision.core.di.appModule
import com.anthooop.colision.core.di.coreModule
import com.anthooop.colision.core.di.featureModules
import com.anthooop.colision.core.di.iosPlatformModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification

private var koinStarted: Boolean = false
private var foregroundObserverRegistered: Boolean = false

// Sentry iOS is initialized from Swift in iosApp/ (follow-up) using the
// native Sentry iOS SDK via SPM — keeping the Kotlin framework free of
// the Sentry iOS framework link dependency. PostHog iOS same story.
fun MainViewController() = ComposeUIViewController {
    if (!koinStarted) {
        startKoin {
            modules(appModule, coreModule, iosPlatformModule, *featureModules.toTypedArray())
        }
        koinStarted = true
    }
    if (!foregroundObserverRegistered) {
        // Pull-on-foreground (FR40): refresh when iOS reactivates the app.
        // Connectivity-based refresh is owned by ProjectSyncManager.
        val syncManager = KoinPlatform.getKoin().get<ProjectSyncManager>()
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> syncManager.refreshNow() }
        foregroundObserverRegistered = true
        // Also refresh on cold start.
        syncManager.refreshNow()
    }
    App()
}
