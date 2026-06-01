package com.anthooop.colision

import android.app.Application
import com.anthooop.colision.config.BuildKonfig
import com.anthooop.colision.core.di.androidPlatformModule
import com.anthooop.colision.core.di.appModule
import com.anthooop.colision.core.di.coreModule
import com.anthooop.colision.core.di.featureModules
import com.anthooop.colision.core.push.NotificationChannels
import io.sentry.kotlin.multiplatform.Sentry
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ColisionApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initSentry()
        initKoin()
        // Create the FCM notification channel before the first message arrives.
        // Idempotent — the system collapses duplicate creates to the same id.
        NotificationChannels.ensureCreated(this)
    }

    private fun initSentry() {
        val dsn = BuildKonfig.SENTRY_DSN
        if (dsn.isBlank()) return
        Sentry.init { options ->
            options.dsn = dsn
            options.environment = if (BuildKonfig.IS_DEVELOPMENT_FLAVOR) "development" else "production"
            options.debug = BuildKonfig.IS_DEVELOPMENT_FLAVOR
        }
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@ColisionApplication)
            modules(appModule, coreModule, androidPlatformModule, *featureModules.toTypedArray())
        }
    }
}
