package com.anthooop.colision.core.di

import com.anthooop.colision.core.common.Analytics
import com.anthooop.colision.core.common.CrashReporter
import com.anthooop.colision.core.common.DefaultDispatcherProvider
import com.anthooop.colision.core.common.DispatcherProvider
import com.anthooop.colision.core.common.InMemorySecureStorage
import com.anthooop.colision.core.common.NoopAnalytics
import com.anthooop.colision.core.common.NoopCrashReporter
import com.anthooop.colision.core.common.SecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module

// Bindings for cross-cutting common abstractions. Real platform impls for
// Logger / NotificationPermissionManager are bound in the per-platform
// modules. CrashReporter + Analytics swap to real impls in story 1.9.
val coreModule: Module = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<CrashReporter> { NoopCrashReporter() }
    single<Analytics> { NoopAnalytics() }
    // Transient fallback. Real platform-backed SecureStorage lands in
    // platform modules (story 1.8 swaps to EncryptedSharedPreferences /
    // Keychain). Keeping it on the common module lets feature code resolve
    // SecureStorage uniformly before that.
    single<SecureStorage> { InMemorySecureStorage() }
}
