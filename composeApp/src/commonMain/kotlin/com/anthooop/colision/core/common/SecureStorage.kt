package com.anthooop.colision.core.common

// expect interface so each platform plugs an actual that lives where Kotlin
// can store secrets safely (EncryptedSharedPreferences on Android, Keychain
// Services on iOS). Story 1.8 wires real impls; story 1.4 lands a no-op
// in-memory fallback so the rest of the wiring compiles.
interface SecureStorage {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun remove(key: String)
    fun clear()
}
