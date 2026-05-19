package com.anthooop.colision.feature.projecthub.settings

data class ProjectSettingsState(
    val projectName: String = "",
    val shareCode: String = "",
    val confirming: ConfirmingAction? = null,
    val transientMessage: String? = null,
    val pendingError: ProjectSettingsError? = null,
    val isProcessing: Boolean = false,
)

sealed interface ProjectSettingsError {
    data class Network(val reason: String) : ProjectSettingsError
}

sealed interface ConfirmingAction {
    data object Leave : ConfirmingAction
    data class Delete(val typed: String = "") : ConfirmingAction {
        val canConfirm: Boolean = typed.trim().equals("supprimer", ignoreCase = true)
    }
}

sealed interface ProjectSettingsIntent {
    data object OpenCommissions : ProjectSettingsIntent
    data object OpenMembers : ProjectSettingsIntent
    data object CopyShareCode : ProjectSettingsIntent
    data object LeaveTapped : ProjectSettingsIntent
    data object DeleteTapped : ProjectSettingsIntent
    data class DeleteConfirmTextChanged(val value: String) : ProjectSettingsIntent
    data object ConfirmCurrentAction : ProjectSettingsIntent
    data object CancelCurrentAction : ProjectSettingsIntent
    data object ErrorDismissed : ProjectSettingsIntent
    data object TransientMessageShown : ProjectSettingsIntent
}

sealed interface ProjectSettingsEvent {
    data object NavigateToCommissions : ProjectSettingsEvent
    data object NavigateToMembers : ProjectSettingsEvent
    data object NavigateToWelcome : ProjectSettingsEvent
    data class CopyToClipboard(val text: String) : ProjectSettingsEvent
}
