package com.anthooop.colision.core.di

import com.anthooop.colision.core.common.Analytics
import com.anthooop.colision.core.common.AnonymousAuthManager
import com.anthooop.colision.core.common.CrashReporter
import com.anthooop.colision.core.common.DefaultDispatcherProvider
import com.anthooop.colision.core.common.DispatcherProvider
import com.anthooop.colision.core.common.Logger
import com.anthooop.colision.core.common.PlatformAnalytics
import com.anthooop.colision.core.common.SentryCrashReporter
import com.anthooop.colision.core.common.SupabaseAnonymousAuthManager
import com.anthooop.colision.core.network.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<CrashReporter> { SentryCrashReporter() }
    single<Analytics> { PlatformAnalytics() }
    single<SupabaseClient> { SupabaseClientProvider.create() }
    single<AnonymousAuthManager> {
        SupabaseAnonymousAuthManager(
            client = get(),
            logger = get<Logger>(),
            crashReporter = get(),
        )
    }
}
