package com.anthooop.colision.core.di

import com.anthooop.colision.app.AppViewModel
import com.anthooop.colision.feature.agenda.di.agendaModule
import com.anthooop.colision.feature.meeting.di.meetingModule
import com.anthooop.colision.feature.onboarding.di.onboardingModule
import com.anthooop.colision.feature.projecthub.di.projectHubModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule: Module = module {
    viewModelOf(::AppViewModel)
}

val featureModules: List<Module> = listOf(
    onboardingModule,
    projectHubModule,
    agendaModule,
    meetingModule,
)
