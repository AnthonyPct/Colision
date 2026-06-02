package com.anthooop.colision.core.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform abstraction for network reachability. Emits the current online
 * status as a hot StateFlow so callers can both poll the current value and
 * react to transitions (online ↔ offline). The implementation is responsible
 * for registering platform listeners eagerly and keeping the flow in sync.
 */
interface ConnectivityObserver {
    val isOnline: StateFlow<Boolean>
}
