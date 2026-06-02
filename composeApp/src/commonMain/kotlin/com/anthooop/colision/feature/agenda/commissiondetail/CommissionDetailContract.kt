package com.anthooop.colision.feature.agenda.commissiondetail

import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import com.anthooop.colision.core.database.entity.MemberEntity

data class CommissionDetailState(
    val isLoading: Boolean = true,
    val commission: CommissionEntity? = null,
    val members: List<MemberEntity> = emptyList(),
    val meetings: List<MeetingEntity> = emptyList(),
    val currentMemberIsMember: Boolean = false,
)

sealed interface CommissionDetailIntent {
    data object BackTapped : CommissionDetailIntent
    data class MeetingTapped(val meetingId: String) : CommissionDetailIntent
    data object CreateMeetingTapped : CommissionDetailIntent
}

sealed interface CommissionDetailEvent {
    data object NavigateBack : CommissionDetailEvent
    data class NavigateToMeetingDetail(val meetingId: String) : CommissionDetailEvent
    data object NavigateToCreateMeeting : CommissionDetailEvent
}
