package com.anthooop.colision.feature.arbitrage.arbitration

import com.anthooop.colision.core.common.AppError

enum class ArbitrationChoice { GoingToA, GoingToB, Later }

data class ArbitrationMeetingUi(
    val meetingId: String,
    val commissionName: String,
    val title: String,
    val startsAt: String,
    val endsAt: String,
    val invitedCount: Int,
    val organizerName: String?,
)

data class ArbitrationState(
    val isLoading: Boolean = true,
    val isResolved: Boolean = false,
    val meetingA: ArbitrationMeetingUi? = null,
    val meetingB: ArbitrationMeetingUi? = null,
    val currentChoice: ArbitrationChoice? = null,
    val isSubmitting: Boolean = false,
    val error: AppError? = null,
)

sealed interface ArbitrationIntent {
    data object BackTapped : ArbitrationIntent
    data class ChoiceTapped(val choice: ArbitrationChoice) : ArbitrationIntent
    data object SubmitTapped : ArbitrationIntent
    data object ErrorDismissed : ArbitrationIntent
}

sealed interface ArbitrationEvent {
    data object NavigateBack : ArbitrationEvent
    data object Submitted : ArbitrationEvent
}
