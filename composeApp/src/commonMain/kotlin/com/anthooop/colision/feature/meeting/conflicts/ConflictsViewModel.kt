package com.anthooop.colision.feature.meeting.conflicts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.feature.meeting.data.PendingMeetingDraft
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConflictsViewModel(
    private val draft: PendingMeetingDraft,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(ConflictsState())
    val state: StateFlow<ConflictsState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<ConflictsEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<ConflictsEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: ConflictsIntent) {
        when (intent) {
            ConflictsIntent.BackTapped ->
                viewModelScope.launch { _events.emit(ConflictsEvent.NavigateBack) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch {
            draft.state.collect { snapshot ->
                _state.update { it.copy(conflicts = snapshot.conflicts) }
            }
        }
    }
}
