package com.anthooop.colision.feature.arbitrage.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface ArbitrageDestination {
    @Serializable
    data class Arbitration(val conflictMeetingId: String) : ArbitrageDestination
}
