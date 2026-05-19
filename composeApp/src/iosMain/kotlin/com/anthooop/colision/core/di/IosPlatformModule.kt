package com.anthooop.colision.core.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.anthooop.colision.core.common.ConnectivityObserver
import com.anthooop.colision.core.common.IosConnectivityObserver
import com.anthooop.colision.core.common.Logger
import com.anthooop.colision.core.common.LoggerIos
import com.anthooop.colision.core.common.NotificationPermissionManager
import com.anthooop.colision.core.common.NotificationPermissionManagerIos
import com.anthooop.colision.core.database.COLISION_DB_FILE
import com.anthooop.colision.core.database.ColisionDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

val iosPlatformModule: Module = module {
    single<Logger> { LoggerIos() }
    single<NotificationPermissionManager> { NotificationPermissionManagerIos() }
    single<ConnectivityObserver> { IosConnectivityObserver() }
    single<ColisionDatabase> {
        Room.databaseBuilder<ColisionDatabase>(
            name = iosDatabasePath(),
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun iosDatabasePath(): String {
    val documentDir: NSURL = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    ) ?: error("Could not resolve iOS Documents directory")
    return requireNotNull(documentDir.path) + "/" + COLISION_DB_FILE
}
