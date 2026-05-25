package com.anthooop.colision.feature.meeting.createmeeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.core.common.ConnectivityObserver
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.feature.agenda.data.CreateMeetingInput
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.meeting.data.ConflictsRepository
import com.anthooop.colision.feature.meeting.data.DetectConflictsArgs
import com.anthooop.colision.feature.meeting.data.DetectConflictsLocallyUseCase
import com.anthooop.colision.feature.meeting.data.PendingMeetingDraft
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class CreateMeetingViewModel(
    private val activeProjectProvider: ActiveProjectProvider,
    private val commissionsRepository: CommissionsRepository,
    private val meetingsRepository: MeetingsRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val connectivity: ConnectivityObserver,
    private val conflictsRepository: ConflictsRepository,
    private val detectLocally: DetectConflictsLocallyUseCase,
    private val pendingDraft: PendingMeetingDraft,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(
        CreateMeetingState(
            availableDates = nextDates(count = 14),
            date = nextDates(count = 1).first(),
        ),
    )
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
        viewModelScope.launch { observeLocalConflicts() }
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

    @OptIn(FlowPreview::class)
    private suspend fun observeLocalConflicts() {
        // Live, debounced pre-check against the Room cache. Discreet badge
        // only — the server response on tap is authoritative (story 4.2 AC4).
        _state
            .map { LocalPreCheckKey(it.date, it.time, it.duration.minutes, it.selectedCommissionIds) }
            .distinctUntilChanged()
            .debounce(LOCAL_PRECHECK_DEBOUNCE_MS)
            .collect { key ->
                val projectId = activeProjectProvider.current()?.id
                val count = if (projectId != null && key.commissionIds.isNotEmpty() && isValidTime(key.time)) {
                    val (start, end) = isoRange(key.date, key.time, key.durationMin)
                    runCatching {
                        detectLocally(
                            projectId = projectId,
                            commissionIds = key.commissionIds.toList(),
                            startIso = start,
                            endIso = end,
                        )
                    }.getOrDefault(0)
                } else 0
                _state.update { it.copy(potentialConflictCount = count) }
            }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val projectId = activeProjectProvider.current()?.id
            if (projectId == null) {
                _state.update {
                    it.copy(isSubmitting = false, error = AppError.Unknown(IllegalStateException("no active project")))
                }
                return@launch
            }
            val (startsAt, endsAt) = isoRange(current.date, current.time, current.duration.minutes)
            val memberId = currentMemberProvider.current()?.id
            val input = CreateMeetingInput(
                projectId = projectId,
                title = current.title.trim().takeIf { it.isNotBlank() },
                startsAt = startsAt,
                endsAt = endsAt,
                commissionIds = current.selectedCommissionIds.toList(),
                createdByMemberId = memberId,
            )

            // Authoritative server-side conflict check (FR20, NFR-P2 < 200ms).
            val detection = conflictsRepository.detect(
                DetectConflictsArgs(
                    projectId = projectId,
                    commissionIds = input.commissionIds,
                    start = startsAt,
                    end = endsAt,
                ),
            )
            val conflicts = detection.getOrNull().orEmpty()
            if (conflicts.isNotEmpty()) {
                pendingDraft.update(input = input, conflicts = conflicts)
                _state.update { it.copy(isSubmitting = false) }
                emit(CreateMeetingEvent.NavigateToConflicts)
                return@launch
            }
            // No conflicts → proceed straight to creation.
            val result = meetingsRepository.create(input)
            result.fold(
                onSuccess = { meeting ->
                    pendingDraft.clear()
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

    private fun nextDates(count: Int): List<String> {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        return (0 until count).map { offset ->
            today.plus(DatePeriod(days = offset)).toString()
        }
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

    private data class LocalPreCheckKey(
        val date: String,
        val time: String,
        val durationMin: Int,
        val commissionIds: Set<String>,
    )

    companion object {
        private const val LOCAL_PRECHECK_DEBOUNCE_MS = 300L
    }
}
