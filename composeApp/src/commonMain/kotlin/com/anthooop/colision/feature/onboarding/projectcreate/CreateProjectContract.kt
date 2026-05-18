package com.anthooop.colision.feature.onboarding.projectcreate

import com.anthooop.colision.core.common.AppError

data class CreateProjectState(
    val name: String = "",
    val isSubmitting: Boolean = false,
    val error: AppError? = null,
) {
    val canSubmit: Boolean = name.trim().length >= 2 && !isSubmitting
}

sealed interface CreateProjectIntent {
    data class NameChanged(val value: String) : CreateProjectIntent
    data object SubmitTapped : CreateProjectIntent
    data object ErrorDismissed : CreateProjectIntent
    data object BackTapped : CreateProjectIntent
}

sealed interface CreateProjectEvent {
    data class NavigateToShareCode(val projectId: String) : CreateProjectEvent
    data object NavigateBack : CreateProjectEvent
}
