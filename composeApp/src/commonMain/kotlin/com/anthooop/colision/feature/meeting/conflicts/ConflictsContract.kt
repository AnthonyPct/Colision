package com.anthooop.colision.feature.meeting.conflicts

import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.feature.meeting.data.ConflictRow

data class ConflictsState(
    val conflicts: List<ConflictRow> = emptyList(),
    val isCreatingAnyway: Boolean = false,
    val error: AppError? = null,
)

sealed interface ConflictsIntent {
    data object BackTapped : ConflictsIntent
    data object PostponeTapped : ConflictsIntent
    data object SuggestionsTapped : ConflictsIntent
    data object CreateAnywayTapped : ConflictsIntent
    data object ErrorDismissed : ConflictsIntent
}

sealed interface ConflictsEvent {
    data object NavigateBack : ConflictsEvent
    data object NavigateToSuggestions : ConflictsEvent
    data class MeetingCreated(val meetingId: String) : ConflictsEvent
}
