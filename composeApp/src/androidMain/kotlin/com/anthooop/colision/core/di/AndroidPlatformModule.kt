package com.anthooop.colision.core.di

import org.koin.core.module.Module
import org.koin.dsl.module

// Android-specific bindings. Story 1.4 registers SecureStorage, Logger and
// NotificationPermissionManager Android actuals here; story 1.6/1.8 add the
// Room database driver and Supabase auth storage.
val androidPlatformModule: Module = module {
}
