package com.anthooop.colision.core.common

// Platform-backed CrashReporter:
// - androidMain: Sentry KMP (sentry-android transitively).
// - iosMain: no-op for now; Sentry iOS SDK is bridged in via Swift in iosApp/
//   as a follow-up. Initializing Sentry from Swift keeps the Kotlin
//   framework free of the Sentry iOS framework link dependency.
expect class PlatformCrashReporter() : CrashReporter
