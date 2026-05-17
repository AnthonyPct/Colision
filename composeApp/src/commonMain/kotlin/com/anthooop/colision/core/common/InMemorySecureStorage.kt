package com.anthooop.colision.core.common

// Transient fallback used until story 1.8 ships the real Android
// (EncryptedSharedPreferences) and iOS (Keychain) actuals. Values are NOT
// persisted across app restarts. NEVER use in production.
class InMemorySecureStorage : SecureStorage {
    private val store: MutableMap<String, String> = mutableMapOf()
    override fun get(key: String): String? = store[key]
    override fun put(key: String, value: String) { store[key] = value }
    override fun remove(key: String) { store.remove(key) }
    override fun clear() { store.clear() }
}
