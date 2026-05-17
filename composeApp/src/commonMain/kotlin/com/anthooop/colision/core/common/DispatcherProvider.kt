package com.anthooop.colision.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val default: CoroutineDispatcher = Dispatchers.Default
    // Dispatchers.IO is JVM-only — kotlinx-coroutines does not expose it on
    // Native targets, so on iOS we fall back to Default. Repositories should
    // treat `io` as "the dispatcher to use for blocking I/O" regardless.
    override val io: CoroutineDispatcher = Dispatchers.Default
}
