package com.anthooop.colision.feature.agenda.di

import com.anthooop.colision.feature.agenda.agenda.AgendaViewModel
import com.anthooop.colision.feature.agenda.commissiondetail.CommissionDetailViewModel
import com.anthooop.colision.feature.agenda.data.DefaultMeetingsRepository
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.agenda.meetingdetail.MeetingDetailViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val agendaModule: Module = module {
    single<MeetingsRepository> {
        DefaultMeetingsRepository(supabase = get(), meetingDao = get())
    }
    viewModelOf(::AgendaViewModel)
    viewModelOf(::MeetingDetailViewModel)
    viewModelOf(::CommissionDetailViewModel)
}
