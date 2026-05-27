package com.anthooop.colision.feature.onboarding.projectcommissions

import com.anthooop.colision.core.database.entity.CommissionEntity

data class CreateProjectCommissionsState(
    val projectId: String = "",
    val draftName: String = "",
    val commissions: List<CommissionEntity> = emptyList(),
    val isAdding: Boolean = false,
    val pendingError: CreateProjectCommissionsError? = null,
) {
    val canAdd: Boolean get() = draftName.trim().length >= 2 && !isAdding
}

sealed interface CreateProjectCommissionsError {
    data class Network(val reason: String) : CreateProjectCommissionsError
}

sealed interface CreateProjectCommissionsIntent {
    data class DraftNameChanged(val value: String) : CreateProjectCommissionsIntent
    data object AddTapped : CreateProjectCommissionsIntent
    data class RemoveTapped(val id: String) : CreateProjectCommissionsIntent
    data object ContinueTapped : CreateProjectCommissionsIntent
    data object BackTapped : CreateProjectCommissionsIntent
    data object ErrorDismissed : CreateProjectCommissionsIntent
}

sealed interface CreateProjectCommissionsEvent {
    data class NavigateToShareCode(val projectId: String) : CreateProjectCommissionsEvent
    data object NavigateBack : CreateProjectCommissionsEvent
}
