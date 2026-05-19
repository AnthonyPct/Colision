package com.anthooop.colision.feature.onboarding.di

import com.anthooop.colision.feature.onboarding.data.DefaultOnboardingRepository
import com.anthooop.colision.feature.onboarding.data.DefaultProjectsRepository
import com.anthooop.colision.feature.onboarding.data.OnboardingRepository
import com.anthooop.colision.feature.onboarding.data.ProjectsRepository
import com.anthooop.colision.feature.onboarding.joincode.JoinCodeViewModel
import com.anthooop.colision.feature.onboarding.joinconfirm.JoinConfirmViewModel
import com.anthooop.colision.feature.onboarding.projectcreate.CreateProjectViewModel
import com.anthooop.colision.feature.onboarding.projectsharecode.ProjectShareCodeViewModel
import com.anthooop.colision.feature.onboarding.welcome.WelcomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule: Module = module {
    single<OnboardingRepository> { DefaultOnboardingRepository(projectDao = get()) }
    single<ProjectsRepository> { DefaultProjectsRepository(supabase = get(), projectDao = get()) }

    viewModelOf(::WelcomeViewModel)
    viewModelOf(::CreateProjectViewModel)
    viewModelOf(::ProjectShareCodeViewModel)
    viewModelOf(::JoinCodeViewModel)
    viewModelOf(::JoinConfirmViewModel)
}
