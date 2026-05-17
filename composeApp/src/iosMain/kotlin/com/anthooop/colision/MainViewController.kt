package com.anthooop.colision

import androidx.compose.ui.window.ComposeUIViewController
import com.anthooop.colision.app.App
import com.anthooop.colision.core.di.appModule
import com.anthooop.colision.core.di.coreModule
import com.anthooop.colision.core.di.featureModules
import com.anthooop.colision.core.di.iosPlatformModule
import org.koin.core.context.startKoin

private var koinStarted: Boolean = false

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
    App()
}
