package com.anthooop.colision

import androidx.compose.ui.window.ComposeUIViewController
import com.anthooop.colision.app.App
import com.anthooop.colision.core.di.appModule
import com.anthooop.colision.core.di.coreModule
import com.anthooop.colision.core.di.featureModules
import com.anthooop.colision.core.di.iosPlatformModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(appModule, coreModule, iosPlatformModule, *featureModules.toTypedArray())
        }
    }
    App()
}
