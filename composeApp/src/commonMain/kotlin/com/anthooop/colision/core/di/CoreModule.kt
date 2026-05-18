package com.anthooop.colision.core.di

import com.anthooop.colision.core.common.Analytics
import com.anthooop.colision.core.common.AnonymousAuthManager
import com.anthooop.colision.core.common.CrashReporter
import com.anthooop.colision.core.common.DefaultDispatcherProvider
import com.anthooop.colision.core.common.DispatcherProvider
import com.anthooop.colision.core.common.Logger
import com.anthooop.colision.core.common.SentryAnalytics
import com.anthooop.colision.core.common.SentryCrashReporter
import com.anthooop.colision.core.common.SupabaseAnonymousAuthManager
import com.anthooop.colision.core.database.ColisionDatabase
import com.anthooop.colision.core.network.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<CrashReporter> { SentryCrashReporter() }
    single<Analytics> { SentryAnalytics() }
    single<SupabaseClient> { SupabaseClientProvider.create() }
    single<AnonymousAuthManager> {
        SupabaseAnonymousAuthManager(
            client = get(),
            logger = get<Logger>(),
            crashReporter = get(),
        )
    }

    // DAOs are derived from the platform-provided ColisionDatabase singleton.
    single { get<ColisionDatabase>().projectDao() }
    single { get<ColisionDatabase>().commissionDao() }
    single { get<ColisionDatabase>().memberDao() }
    single { get<ColisionDatabase>().memberCommissionDao() }
}
