package com.anthooop.colision.feature.onboarding.joincommissions

import com.anthooop.colision.core.database.entity.CommissionEntity

data class JoinCommissionsState(
    val isLoading: Boolean = true,
    val commissions: List<CommissionEntity> = emptyList(),
    val checkedIds: Set<String> = emptySet(),
    val isSubmitting: Boolean = false,
    val pendingError: JoinCommissionsError? = null,
) {
    val canSubmit: Boolean = checkedIds.isNotEmpty() && !isSubmitting
}

sealed interface JoinCommissionsError {
    data class PartialSave(val reason: String) : JoinCommissionsError
}

sealed interface JoinCommissionsIntent {
    data class CommissionToggled(val commissionId: String) : JoinCommissionsIntent
    data object ContinueTapped : JoinCommissionsIntent
    data object BackTapped : JoinCommissionsIntent
    data object ErrorDismissed : JoinCommissionsIntent
}

sealed interface JoinCommissionsEvent {
    data class NavigateToNotificationPermission(val projectId: String, val memberId: String) :
        JoinCommissionsEvent
    data object NavigateBack : JoinCommissionsEvent
}
