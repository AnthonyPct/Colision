package com.anthooop.colision.feature.meeting.conflicts

import com.anthooop.colision.feature.meeting.data.ConflictRow

data class ConflictsState(
    val conflicts: List<ConflictRow> = emptyList(),
)

sealed interface ConflictsIntent {
    data object BackTapped : ConflictsIntent
}

sealed interface ConflictsEvent {
    data object NavigateBack : ConflictsEvent
}
