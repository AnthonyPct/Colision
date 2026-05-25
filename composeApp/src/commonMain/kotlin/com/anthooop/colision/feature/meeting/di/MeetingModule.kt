package com.anthooop.colision.feature.meeting.di

import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val meetingModule: Module = module {
    viewModelOf(::CreateMeetingViewModel)
}
