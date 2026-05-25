package com.anthooop.colision.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
import com.anthooop.colision.core.network.createSessionDataStore
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
            name = iosDocumentsDirectory() + "/" + COLISION_DB_FILE,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single<DataStore<Preferences>> {
        createSessionDataStore(directory = iosDocumentsDirectory())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun iosDocumentsDirectory(): String {
    val documentDir: NSURL = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    ) ?: error("Could not resolve iOS Documents directory")
    return requireNotNull(documentDir.path)
}
