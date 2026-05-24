package com.anthooop.colision.feature.agenda.agenda

import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity

enum class AgendaView { Week, Month }

data class AgendaMeeting(
    val meeting: MeetingEntity,
    val commissions: List<CommissionEntity>,
    val conflicted: Boolean,
)

data class AgendaState(
    val isLoading: Boolean = true,
    val view: AgendaView = AgendaView.Week,
    val firstName: String = "",
    val meetings: List<AgendaMeeting> = emptyList(),
    val isOnline: Boolean = true,
    val lastSyncTime: String? = null,
)

sealed interface AgendaIntent {
    data class ViewSelected(val view: AgendaView) : AgendaIntent
    data class MeetingTapped(val meetingId: String) : AgendaIntent
    data object CreateMeetingTapped : AgendaIntent
}

sealed interface AgendaEvent {
    data class NavigateToMeetingDetail(val meetingId: String) : AgendaEvent
    data object NavigateToCreateMeeting : AgendaEvent
}
