package com.anthooop.colision.core.di

import com.anthooop.colision.core.common.Logger
import com.anthooop.colision.core.common.LoggerIos
import com.anthooop.colision.core.common.NotificationPermissionManager
import com.anthooop.colision.core.common.NotificationPermissionManagerIos
import org.koin.core.module.Module
import org.koin.dsl.module

val iosPlatformModule: Module = module {
    single<Logger> { LoggerIos() }
    single<NotificationPermissionManager> { NotificationPermissionManagerIos() }
}
