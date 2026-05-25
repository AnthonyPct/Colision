package com.anthooop.colision.feature.meeting.di

import com.anthooop.colision.feature.meeting.conflicts.ConflictsViewModel
import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingViewModel
import com.anthooop.colision.feature.meeting.editmeeting.EditMeetingViewModel
import com.anthooop.colision.feature.meeting.data.ConflictsRepository
import com.anthooop.colision.feature.meeting.data.DefaultConflictsRepository
import com.anthooop.colision.feature.meeting.data.DefaultSuggestionsRepository
import com.anthooop.colision.feature.meeting.data.DetectConflictsLocallyUseCase
import com.anthooop.colision.feature.meeting.data.PendingMeetingDraft
import com.anthooop.colision.feature.meeting.data.SuggestionsRepository
import com.anthooop.colision.feature.meeting.suggestions.SuggestionsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val meetingModule: Module = module {
    single<ConflictsRepository> { DefaultConflictsRepository(supabase = get()) }
    single<SuggestionsRepository> { DefaultSuggestionsRepository(supabase = get()) }
    single { DetectConflictsLocallyUseCase(meetingDao = get()) }
    single { PendingMeetingDraft() }

    viewModelOf(::CreateMeetingViewModel)
    viewModelOf(::EditMeetingViewModel)
    viewModelOf(::ConflictsViewModel)
    viewModelOf(::SuggestionsViewModel)
}
