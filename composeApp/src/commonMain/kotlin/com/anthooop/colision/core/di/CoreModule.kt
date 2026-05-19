package com.anthooop.colision.core.di

import com.anthooop.colision.core.common.Analytics
import com.anthooop.colision.core.common.AnonymousAuthManager
import com.anthooop.colision.core.common.ConnectivityObserver
import com.anthooop.colision.core.common.CrashReporter
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.common.DefaultCurrentMemberProvider
import com.anthooop.colision.core.common.DefaultDispatcherProvider
import com.anthooop.colision.core.common.DefaultProjectSyncManager
import com.anthooop.colision.core.common.DispatcherProvider
import com.anthooop.colision.core.common.ProjectSyncManager
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
    single<CurrentMemberProvider> {
        DefaultCurrentMemberProvider(supabase = get(), memberDao = get())
    }
    single<ProjectSyncManager> {
        DefaultProjectSyncManager(
            connectivity = get<ConnectivityObserver>(),
            activeProjectProvider = get(),
            commissionsRepository = get(),
            membersRepository = get(),
            meetingsRepository = get(),
            logger = get(),
        )
    }

    // DAOs are derived from the platform-provided ColisionDatabase singleton.
    single { get<ColisionDatabase>().projectDao() }
    single { get<ColisionDatabase>().commissionDao() }
    single { get<ColisionDatabase>().memberDao() }
    single { get<ColisionDatabase>().memberCommissionDao() }
    single { get<ColisionDatabase>().meetingDao() }
}
