package com.anthooop.colision

import android.app.Application
import com.anthooop.colision.config.BuildConfig
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
        val dsn = BuildConfig.sentryDsn
        if (dsn.isBlank()) return // Ops fills BuildConfig.sentryDsn post-merge.
        Sentry.init { options ->
            options.dsn = dsn
            options.environment = if (BuildConfig.isDevelopmentFlavor) "development" else "production"
            options.debug = BuildConfig.isDevelopmentFlavor
        }
    }

    private fun initPostHog() {
        val apiKey = BuildConfig.posthogApiKey
        if (apiKey.isBlank()) return // Ops fills BuildConfig.posthogApiKey post-merge.
        PostHogAndroid.setup(
            context = this,
            config = PostHogAndroidConfig(
                apiKey = apiKey,
                host = BuildConfig.posthogHost,
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
