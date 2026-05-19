package com.anthooop.colision.feature.onboarding.projectsharecode

data class ProjectShareCodeState(
    val projectName: String = "",
    val shareCode: String = "",
    val isLoading: Boolean = true,
)

sealed interface ProjectShareCodeIntent {
    data object CopyTapped : ProjectShareCodeIntent
    data object ContinueTapped : ProjectShareCodeIntent
    data object BackTapped : ProjectShareCodeIntent
}

sealed interface ProjectShareCodeEvent {
    data class CopyToClipboard(val text: String) : ProjectShareCodeEvent
    data object NavigateToHome : ProjectShareCodeEvent
    data object NavigateBack : ProjectShareCodeEvent
}
