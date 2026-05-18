package com.anthooop.colision

import android.app.Application
import com.anthooop.colision.config.BuildKonfig
import com.anthooop.colision.core.di.androidPlatformModule
import com.anthooop.colision.core.di.appModule
import com.anthooop.colision.core.di.coreModule
import com.anthooop.colision.core.di.featureModules
import io.sentry.kotlin.multiplatform.Sentry
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ColisionApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initSentry()
        initKoin()
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
