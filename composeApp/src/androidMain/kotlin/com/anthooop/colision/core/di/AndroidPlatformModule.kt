package com.anthooop.colision.core.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.anthooop.colision.core.common.AndroidConnectivityObserver
import com.anthooop.colision.core.common.ConnectivityObserver
import com.anthooop.colision.core.common.Logger
import com.anthooop.colision.core.common.LoggerAndroid
import com.anthooop.colision.core.common.NotificationPermissionManager
import com.anthooop.colision.core.common.NotificationPermissionManagerAndroid
import com.anthooop.colision.core.database.COLISION_DB_FILE
import com.anthooop.colision.core.database.ColisionDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val androidPlatformModule: Module = module {
    single<Logger> { LoggerAndroid() }
    single<NotificationPermissionManager> {
        NotificationPermissionManagerAndroid(androidContext().applicationContext)
    }
    single<ConnectivityObserver> {
        AndroidConnectivityObserver(androidContext().applicationContext)
    }
    single<ColisionDatabase> {
        val ctx = androidContext().applicationContext
        val dbFile = ctx.getDatabasePath(COLISION_DB_FILE)
        Room.databaseBuilder<ColisionDatabase>(
            context = ctx,
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}
