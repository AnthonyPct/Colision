package com.anthooop.colision

import androidx.compose.ui.window.ComposeUIViewController
import com.anthooop.colision.app.App
import com.anthooop.colision.config.BuildConfig
import com.anthooop.colision.core.di.appModule
import com.anthooop.colision.core.di.coreModule
import com.anthooop.colision.core.di.featureModules
import com.anthooop.colision.core.di.iosPlatformModule
import io.sentry.kotlin.multiplatform.Sentry
import org.koin.core.context.startKoin

private var bootstrapped: Boolean = false

fun MainViewController() = ComposeUIViewController {
    if (!bootstrapped) {
        initSentry()
        startKoin {
            modules(appModule, coreModule, iosPlatformModule, *featureModules.toTypedArray())
        }
        bootstrapped = true
    }
    App()
}

private fun initSentry() {
    val dsn = BuildConfig.sentryDsn
    if (dsn.isBlank()) return // Ops fills BuildConfig.sentryDsn post-merge.
    Sentry.init { options ->
        options.dsn = dsn
        options.environment = if (BuildConfig.isDevelopmentFlavor) "development" else "production"
        options.debug = BuildConfig.isDevelopmentFlavor
    }
}
