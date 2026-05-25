package com.anthooop.colision.feature.meeting.editmeeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.core.common.ConnectivityObserver
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.agenda.data.UpdateMeetingInput
import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingEvent
import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingIntent
import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingState
import com.anthooop.colision.feature.meeting.createmeeting.DurationOption
import com.anthooop.colision.feature.meeting.navigation.MeetingDestination
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class EditMeetingViewModel(
    savedStateHandle: SavedStateHandle,
    private val activeProjectProvider: ActiveProjectProvider,
    private val commissionsRepository: CommissionsRepository,
    private val meetingsRepository: MeetingsRepository,
    private val connectivity: ConnectivityObserver,
) : ViewModel() {

    private val meetingId: String = savedStateHandle
        .toRoute<MeetingDestination.EditMeeting>()
        .meetingId

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(CreateMeetingState())
    val state: StateFlow<CreateMeetingState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<CreateMeetingEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<CreateMeetingEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: CreateMeetingIntent) {
        when (intent) {
            is CreateMeetingIntent.TitleChanged -> _state.update { it.copy(title = intent.value, error = null) }
            is CreateMeetingIntent.DateSelected -> _state.update { current ->
                val nextAvailable = if (intent.iso in current.availableDates) {
                    current.availableDates
                } else {
                    listOf(intent.iso) + current.availableDates
                }
                current.copy(date = intent.iso, availableDates = nextAvailable, error = null)
            }
            is CreateMeetingIntent.TimeChanged -> _state.update { it.copy(time = intent.value, error = null) }
            is CreateMeetingIntent.DurationSelected -> _state.update { it.copy(duration = intent.option, error = null) }
            is CreateMeetingIntent.CommissionToggled -> _state.update { current ->
                val next = current.selectedCommissionIds.toMutableSet()
                if (!next.add(intent.commissionId)) next.remove(intent.commissionId)
                current.copy(selectedCommissionIds = next, error = null)
            }
            CreateMeetingIntent.SubmitTapped -> submit()
            CreateMeetingIntent.BackTapped -> emit(CreateMeetingEvent.NavigateBack)
            CreateMeetingIntent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch { observeCommissions() }
        viewModelScope.launch { observeConnectivity() }
        viewModelScope.launch { prefill() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeCommissions() {
        activeProjectProvider.observe()
            .flatMapLatest { project ->
                if (project == null) flowOf(emptyList()) else commissionsRepository.observeByProject(project.id)
            }
            .collect { commissions ->
                _state.update { it.copy(isLoading = false, commissions = commissions) }
            }
    }

    private suspend fun observeConnectivity() {
        connectivity.isOnline.collect { online ->
            _state.update { it.copy(isOnline = online) }
        }
    }

    private suspend fun prefill() {
        val meeting = meetingsRepository.observeById(meetingId).first() ?: return
        val commissionIds = meetingsRepository.observeCommissionIds(meetingId).first().toSet()
        val tz = TimeZone.currentSystemDefault()
        val start = Instant.parse(meeting.startsAt).toLocalDateTime(tz)
        val end = Instant.parse(meeting.endsAt).toLocalDateTime(tz)
        val durationMin = (Instant.parse(meeting.endsAt) - Instant.parse(meeting.startsAt)) / 1.minutes
        val duration = DurationOption.entries.firstOrNull { it.minutes == durationMin.toInt() }
            ?: DurationOption.Min90
        val hh = start.hour.toString().padStart(2, '0')
        val mm = start.minute.toString().padStart(2, '0')
        _state.update {
            it.copy(
                title = meeting.title.orEmpty(),
                date = start.date.toString(),
                time = "$hh:$mm",
                duration = duration,
                selectedCommissionIds = commissionIds,
                availableDates = listOf(start.date.toString()) + it.availableDates.filter { d -> d != start.date.toString() },
            )
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val (startsAt, endsAt) = isoRange(current.date, current.time, current.duration.minutes)
            val result = meetingsRepository.update(
                UpdateMeetingInput(
                    meetingId = meetingId,
                    title = current.title.trim().takeIf { it.isNotBlank() },
                    startsAt = startsAt,
                    endsAt = endsAt,
                    commissionIds = current.selectedCommissionIds.toList(),
                ),
            )
            result.fold(
                onSuccess = { meeting ->
                    _state.update { it.copy(isSubmitting = false) }
                    emit(CreateMeetingEvent.MeetingCreated(meeting.id))
                },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update { it.copy(isSubmitting = false, error = appError) }
                },
            )
        }
    }

    private fun emit(event: CreateMeetingEvent) {
        viewModelScope.launch { _events.emit(event) }
    }

    private fun isoRange(dateIso: String, time: String, durationMinutes: Int): Pair<String, String> {
        val tz = TimeZone.currentSystemDefault()
        val hh = time.substring(0, 2).toInt()
        val mm = time.substring(3, 5).toInt()
        val date = LocalDate.parse(dateIso)
        val startLocal = LocalDateTime(date, LocalTime(hh, mm))
        val startInstant = startLocal.toInstant(tz)
        val endInstant = startInstant + durationMinutes.minutes
        return startInstant.toString() to endInstant.toString()
    }
}
