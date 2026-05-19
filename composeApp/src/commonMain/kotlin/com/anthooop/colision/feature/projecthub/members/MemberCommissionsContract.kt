package com.anthooop.colision.feature.projecthub.members

import com.anthooop.colision.core.database.entity.CommissionEntity

data class MemberCommissionsState(
    val memberName: String = "",
    val commissions: List<CommissionEntity> = emptyList(),
    val assignedIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val pendingError: MemberCommissionsError? = null,
)

sealed interface MemberCommissionsError {
    data class Toggle(val reason: String) : MemberCommissionsError
}

sealed interface MemberCommissionsIntent {
    data object BackTapped : MemberCommissionsIntent
    data class CommissionToggled(val commissionId: String) : MemberCommissionsIntent
    data object ErrorDismissed : MemberCommissionsIntent
}

sealed interface MemberCommissionsEvent {
    data object NavigateBack : MemberCommissionsEvent
}
