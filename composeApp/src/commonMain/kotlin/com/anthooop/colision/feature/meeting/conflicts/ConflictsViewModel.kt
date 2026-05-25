package com.anthooop.colision.feature.meeting.conflicts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
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
    private val meetingsRepository: MeetingsRepository,
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
            ConflictsIntent.BackTapped, ConflictsIntent.PostponeTapped ->
                emit(ConflictsEvent.NavigateBack)
            ConflictsIntent.SuggestionsTapped ->
                emit(ConflictsEvent.NavigateToSuggestions)
            ConflictsIntent.CreateAnywayTapped -> createAnyway()
            ConflictsIntent.ErrorDismissed -> _state.update { it.copy(error = null) }
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

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun createAnyway() {
        val input = draft.state.value.input ?: return
        if (_state.value.isCreatingAnyway) return
        _state.update { it.copy(isCreatingAnyway = true, error = null) }
        viewModelScope.launch {
            meetingsRepository.create(input).fold(
                onSuccess = { meeting ->
                    draft.clear()
                    _state.update { it.copy(isCreatingAnyway = false) }
                    emit(ConflictsEvent.MeetingCreated(meeting.id))
                },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update { it.copy(isCreatingAnyway = false, error = appError) }
                },
            )
        }
    }

    private fun emit(event: ConflictsEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
