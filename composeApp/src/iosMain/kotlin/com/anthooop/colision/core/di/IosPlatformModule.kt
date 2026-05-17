package com.anthooop.colision.core.di

import org.koin.core.module.Module
import org.koin.dsl.module

// iOS-specific bindings. Story 1.4 registers SecureStorage (Keychain), Logger
// and NotificationPermissionManager iOS actuals here; story 1.6/1.8 add the
// Room database driver and Supabase auth storage.
val iosPlatformModule: Module = module {
}
