package com.anthooop.colision.feature.onboarding.joincode

import com.anthooop.colision.core.common.AppError

data class JoinCodeState(
    val code: String = "",
    val isResolving: Boolean = false,
    val resolvedProjectName: String? = null,
    val error: AppError? = null,
) {
    val canSubmit: Boolean = code.length == 6 && !isResolving
}

sealed interface JoinCodeIntent {
    data class CodeChanged(val value: String) : JoinCodeIntent
    data object SubmitTapped : JoinCodeIntent
    data object BackTapped : JoinCodeIntent
    data object ErrorDismissed : JoinCodeIntent
}

sealed interface JoinCodeEvent {
    data class NavigateToConfirm(val projectId: String) : JoinCodeEvent
    data object NavigateBack : JoinCodeEvent
}
