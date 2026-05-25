package com.anthooop.colision.feature.agenda.meetingdetail

import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import com.anthooop.colision.core.database.entity.MemberEntity

data class MeetingDetailState(
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val isCreator: Boolean = false,
    val meeting: MeetingEntity? = null,
    val commissions: List<CommissionEntity> = emptyList(),
    val attendees: List<MemberEntity> = emptyList(),
    val creator: MemberEntity? = null,
    val isDeleting: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val error: AppError? = null,
)

sealed interface MeetingDetailIntent {
    data object BackTapped : MeetingDetailIntent
    data object EditTapped : MeetingDetailIntent
    data object DeleteTapped : MeetingDetailIntent
    data object DeleteConfirmed : MeetingDetailIntent
    data object DeleteDismissed : MeetingDetailIntent
    data object ErrorDismissed : MeetingDetailIntent
}

sealed interface MeetingDetailEvent {
    data object NavigateBack : MeetingDetailEvent
    data class NavigateToEdit(val meetingId: String) : MeetingDetailEvent
}
