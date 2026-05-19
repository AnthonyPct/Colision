package com.anthooop.colision.feature.onboarding.joinconfirm

import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MemberEntity

data class JoinConfirmState(
    val projectName: String = "",
    val commissions: List<CommissionEntity> = emptyList(),
    val members: List<MemberEntity> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface JoinConfirmIntent {
    data object ConfirmTapped : JoinConfirmIntent
    data object WrongProjectTapped : JoinConfirmIntent
    data object BackTapped : JoinConfirmIntent
}

sealed interface JoinConfirmEvent {
    data class NavigateToIdentity(val projectId: String) : JoinConfirmEvent
    data object NavigateBack : JoinConfirmEvent
}
