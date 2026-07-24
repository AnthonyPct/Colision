package com.anthooop.colision.feature.poll.di

import com.anthooop.colision.feature.poll.createpoll.CreatePollViewModel
import com.anthooop.colision.feature.poll.data.DefaultPollsRepository
import com.anthooop.colision.feature.poll.data.PollsRepository
import com.anthooop.colision.feature.poll.polldetail.PollDetailViewModel
import com.anthooop.colision.feature.poll.pollslist.PollsListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pollModule: Module = module {
    single<PollsRepository> {
        DefaultPollsRepository(supabase = get(), pollDao = get())
    }
    viewModelOf(::PollsListViewModel)
    viewModelOf(::PollDetailViewModel)
    viewModelOf(::CreatePollViewModel)
}
