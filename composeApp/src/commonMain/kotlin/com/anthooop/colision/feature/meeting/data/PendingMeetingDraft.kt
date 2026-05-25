package com.anthooop.colision.feature.meeting.data

import com.anthooop.colision.feature.agenda.data.CreateMeetingInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory bridge between the CreateMeeting → Conflicts → Suggestions
 * sub-screens. The form lives in CreateMeetingViewModel, but when the user
 * taps "Vérifier les conflits" we need the next screen(s) to see the same
 * draft + the server-returned conflict list. A Koin singleton scoped to
 * the running process is the simplest KMP-friendly bridge.
 *
 * Cleared whenever the user navigates back to the form or finalizes the
 * creation.
 */
class PendingMeetingDraft {
    private val _state = MutableStateFlow(Snapshot())
    val state: StateFlow<Snapshot> = _state.asStateFlow()

    fun update(input: CreateMeetingInput, conflicts: List<ConflictRow>) {
        _state.value = Snapshot(input = input, conflicts = conflicts)
    }

    fun updateConflicts(conflicts: List<ConflictRow>) {
        _state.value = _state.value.copy(conflicts = conflicts)
    }

    fun clear() {
        _state.value = Snapshot()
    }

    data class Snapshot(
        val input: CreateMeetingInput? = null,
        val conflicts: List<ConflictRow> = emptyList(),
    )
}
