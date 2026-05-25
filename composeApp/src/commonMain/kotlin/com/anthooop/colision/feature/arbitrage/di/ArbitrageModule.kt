package com.anthooop.colision.feature.arbitrage.di

import com.anthooop.colision.feature.arbitrage.arbitration.ArbitrationViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val arbitrageModule: Module = module {
    viewModelOf(::ArbitrationViewModel)
}
