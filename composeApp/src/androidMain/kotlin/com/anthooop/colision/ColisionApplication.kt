package com.anthooop.colision

import android.app.Application
import com.anthooop.colision.config.BuildKonfig
import com.anthooop.colision.core.di.androidPlatformModule
import com.anthooop.colision.core.di.appModule
import com.anthooop.colision.core.di.coreModule
import com.anthooop.colision.core.di.featureModules
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import io.sentry.kotlin.multiplatform.Sentry
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ColisionApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initSentry()
        initPostHog()
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

    private fun initPostHog() {
        val apiKey = BuildKonfig.POSTHOG_API_KEY
        if (apiKey.isBlank()) return
        PostHogAndroid.setup(
            context = this,
            config = PostHogAndroidConfig(
                apiKey = apiKey,
                host = BuildKonfig.POSTHOG_HOST,
            ).apply {
                captureScreenViews = false
                sendFeatureFlagEvent = false
            },
        )
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@ColisionApplication)
            modules(appModule, coreModule, androidPlatformModule, *featureModules.toTypedArray())
        }
    }
}
