package com.anthooop.colision.feature.projecthub.di

import com.anthooop.colision.feature.projecthub.commissions.CommissionsListViewModel
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import com.anthooop.colision.feature.projecthub.data.DefaultActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.DefaultCommissionsRepository
import com.anthooop.colision.feature.projecthub.data.DefaultProjectLifecycleRepository
import com.anthooop.colision.feature.projecthub.data.ProjectLifecycleRepository
import com.anthooop.colision.feature.projecthub.settings.ProjectSettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val projectHubModule: Module = module {
    single<ActiveProjectProvider> { DefaultActiveProjectProvider(projectDao = get()) }
    single<CommissionsRepository> {
        DefaultCommissionsRepository(supabase = get(), commissionDao = get())
    }
    single<ProjectLifecycleRepository> {
        DefaultProjectLifecycleRepository(supabase = get(), projectDao = get())
    }

    viewModelOf(::ProjectSettingsViewModel)
    viewModelOf(::CommissionsListViewModel)
}
