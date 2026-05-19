package com.anthooop.colision.feature.projecthub.commissions

import com.anthooop.colision.core.database.entity.CommissionEntity

data class CommissionsListState(
    val isLoading: Boolean = true,
    val commissions: List<CommissionEntity> = emptyList(),
    val editing: EditingState? = null,
    val pendingError: CommissionsListError? = null,
)

sealed interface CommissionsListError {
    data class Network(val reason: String) : CommissionsListError
}

sealed interface EditingState {
    data class Create(val name: String = "") : EditingState
    data class Rename(val id: String, val originalName: String, val name: String) : EditingState
    data class ConfirmDelete(val id: String, val name: String) : EditingState
}

sealed interface CommissionsListIntent {
    data object BackTapped : CommissionsListIntent
    data object AddTapped : CommissionsListIntent
    data class RenameTapped(val id: String, val name: String) : CommissionsListIntent
    data class DeleteTapped(val id: String, val name: String) : CommissionsListIntent
    data class EditorNameChanged(val value: String) : CommissionsListIntent
    data object EditorCancelled : CommissionsListIntent
    data object EditorConfirmed : CommissionsListIntent
    data object ErrorDismissed : CommissionsListIntent
}

sealed interface CommissionsListEvent {
    data object NavigateBack : CommissionsListEvent
}
