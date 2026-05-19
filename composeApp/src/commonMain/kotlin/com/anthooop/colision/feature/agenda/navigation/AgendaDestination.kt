package com.anthooop.colision.feature.agenda.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AgendaDestination {
    @Serializable
    data object Agenda : AgendaDestination

    @Serializable
    data class MeetingDetail(val meetingId: String) : AgendaDestination

    @Serializable
    data class CommissionDetail(val commissionId: String) : AgendaDestination
}
