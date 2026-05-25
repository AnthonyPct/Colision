package com.anthooop.colision.feature.meeting.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.feature.agenda.data.CreateMeetingInput
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.meeting.data.PendingMeetingDraft
import com.anthooop.colision.feature.meeting.data.SuggestionsRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class SuggestionsViewModel(
    private val draft: PendingMeetingDraft,
    private val suggestions: SuggestionsRepository,
    private val meetingsRepository: MeetingsRepository,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(SuggestionsState())
    val state: StateFlow<SuggestionsState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<SuggestionsEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<SuggestionsEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: SuggestionsIntent) {
        when (intent) {
            SuggestionsIntent.BackTapped -> emit(SuggestionsEvent.NavigateBack)
            is SuggestionsIntent.SlotSelected -> _state.update {
                it.copy(selectedIndex = intent.index, error = null)
            }
            SuggestionsIntent.SubmitTapped -> submit()
            SuggestionsIntent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch { loadSuggestions() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private suspend fun loadSuggestions() {
        val input = draft.state.value.input
        if (input == null) {
            _state.update { it.copy(isLoading = false, slots = emptyList()) }
            return
        }
        val durationMinutes = durationMinutesOf(input)
        val result = suggestions.suggest(
            projectId = input.projectId,
            commissionIds = input.commissionIds,
            anchor = input.startsAt,
            durationMinutes = durationMinutes,
        )
        result.fold(
            onSuccess = { slots ->
                _state.update { it.copy(isLoading = false, slots = slots) }
            },
            onFailure = { t ->
                val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                _state.update { it.copy(isLoading = false, error = appError) }
            },
        )
    }

    private fun submit() {
        val current = _state.value
        val baseInput = draft.state.value.input ?: return
        val index = current.selectedIndex ?: return
        val slot = current.slots.getOrNull(index) ?: return
        if (current.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val rescheduled = baseInput.copy(startsAt = slot.startsAt, endsAt = slot.endsAt)
            meetingsRepository.create(rescheduled).fold(
                onSuccess = { meeting ->
                    draft.clear()
                    _state.update { it.copy(isSubmitting = false) }
                    emit(SuggestionsEvent.MeetingCreated(meeting.id))
                },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update { it.copy(isSubmitting = false, error = appError) }
                },
            )
        }
    }

    private fun durationMinutesOf(input: CreateMeetingInput): Int {
        // Naïve ISO-8601 instant diff in whole minutes. Both endpoints are
        // produced by CreateMeetingViewModel.isoRange so they're trustworthy.
        val start = Instant.parse(input.startsAt)
        val end = Instant.parse(input.endsAt)
        return ((end - start) / 1.minutes).toInt()
    }

    private fun emit(event: SuggestionsEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
