package com.anthooop.colision.feature.onboarding.di

import com.anthooop.colision.feature.onboarding.data.DefaultOnboardingRepository
import com.anthooop.colision.feature.onboarding.data.OnboardingRepository
import com.anthooop.colision.feature.onboarding.welcome.WelcomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule: Module = module {
    single<OnboardingRepository> { DefaultOnboardingRepository(projectDao = get()) }
    viewModelOf(::WelcomeViewModel)
}
