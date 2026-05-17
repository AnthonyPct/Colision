package com.anthooop.colision.core.di

import org.koin.core.module.Module
import org.koin.dsl.module

// Top-level Koin module composition. Each feature adds its own module via
// featureModules. Story 1.4 will register the core/common wrappers
// (Logger, CrashReporter, Analytics, etc.) in CoreModule.
val appModule: Module = module {
    // Placeholder. Concrete bindings are declared in CoreModule (story 1.4)
    // and per-feature modules (epic 2+).
}

val featureModules: List<Module> = emptyList()
