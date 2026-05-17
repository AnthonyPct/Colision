package com.anthooop.colision.core.di

import com.anthooop.colision.core.common.Logger
import com.anthooop.colision.core.common.LoggerAndroid
import com.anthooop.colision.core.common.NotificationPermissionManager
import com.anthooop.colision.core.common.NotificationPermissionManagerAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val androidPlatformModule: Module = module {
    single<Logger> { LoggerAndroid() }
    single<NotificationPermissionManager> {
        NotificationPermissionManagerAndroid(androidContext().applicationContext)
    }
}
