package com.anthooop.colision.feature.poll.createpoll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.core.common.ConnectivityObserver
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.feature.poll.data.CreatePollInput
import com.anthooop.colision.feature.poll.data.PollTarget
import com.anthooop.colision.feature.poll.data.PollsRepository
import com.anthooop.colision.feature.poll.domain.PollComputations
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import com.anthooop.colision.feature.projecthub.data.MembersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class CreatePollViewModel(
    private val activeProjectProvider: ActiveProjectProvider,
    private val commissionsRepository: CommissionsRepository,
    private val membersRepository: MembersRepository,
    private val pollsRepository: PollsRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val connectivity: ConnectivityObserver,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(CreatePollState(closesDate = defaultCloseDate()))
    val state: StateFlow<CreatePollState> = _state.asStateFlow()

    // Latest project membership, kept for the eligible-count preview.
    private var members: List<MemberEntity> = emptyList()
    private var assignments: List<MemberCommissionEntity> = emptyList()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<CreatePollEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<CreatePollEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: CreatePollIntent) {
        when (intent) {
            is CreatePollIntent.QuestionChanged ->
                _state.update { it.copy(question = intent.value, error = null) }
            is CreatePollIntent.OptionChanged -> _state.update { current ->
                current.copy(
                    options = current.options.mapIndexed { i, v -> if (i == intent.index) intent.value else v },
                    error = null,
                )
            }
            CreatePollIntent.OptionAdded ->
                _state.update { it.copy(options = it.options + "") }
            is CreatePollIntent.OptionRemoved -> _state.update { current ->
                if (current.options.size <= 2) {
                    current
                } else {
                    current.copy(options = current.options.filterIndexed { i, _ -> i != intent.index })
                }
            }
            is CreatePollIntent.PublicToggled -> _state.update {
                it.copy(
                    isPublic = intent.isPublic,
                    selectedCommissionIds = if (intent.isPublic) emptySet() else it.selectedCommissionIds,
                ).withEligibleCount()
            }
            is CreatePollIntent.CommissionToggled -> _state.update { current ->
                val next = current.selectedCommissionIds.toMutableSet()
                if (!next.add(intent.commissionId)) next.remove(intent.commissionId)
                current.copy(selectedCommissionIds = next, error = null).withEligibleCount()
            }
            is CreatePollIntent.DateSelected ->
                _state.update { it.copy(closesDate = intent.iso, error = null) }
            CreatePollIntent.SubmitTapped -> submit()
            CreatePollIntent.BackTapped -> emit(CreatePollEvent.NavigateBack)
            CreatePollIntent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch { observeProjectData() }
        viewModelScope.launch { observeConnectivity() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeProjectData() {
        activeProjectProvider.observe()
            .flatMapLatest { project ->
                if (project == null) {
                    flowOf(Triple(emptyList(), emptyList<MemberEntity>(), emptyList<MemberCommissionEntity>()))
                } else {
                    combine(
                        commissionsRepository.observeByProject(project.id),
                        membersRepository.observeByProject(project.id),
                        membersRepository.observeAssignmentsForProject(project.id),
                    ) { commissions, members, assignments -> Triple(commissions, members, assignments) }
                }
            }
            .collect { (commissions, members, assignments) ->
                this.members = members
                this.assignments = assignments
                _state.update {
                    it.copy(isLoading = false, commissions = commissions).withEligibleCount()
                }
            }
    }

    private suspend fun observeConnectivity() {
        connectivity.isOnline.collect { online -> _state.update { it.copy(isOnline = online) } }
    }

    private fun CreatePollState.withEligibleCount(): CreatePollState {
        val target = if (isPublic) PollComputations.TARGET_PUBLIC else PollComputations.TARGET_COMMISSIONS
        val count = PollComputations.eligibleCount(
            targetType = target,
            scopeCommissionIds = selectedCommissionIds,
            projectMemberIds = members.map { it.id }.toSet(),
            assignments = assignments,
        )
        return copy(eligibleCount = count)
    }

    private fun submit() {
        val current = _state.value
        if (!current.canCreate) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val projectId = activeProjectProvider.current()?.id
            if (projectId == null) {
                _state.update { it.copy(isSubmitting = false, error = AppError.Unknown(IllegalStateException("no active project"))) }
                return@launch
            }
            val creatorMemberId = currentMemberProvider.current()?.id
            if (creatorMemberId.isNullOrBlank()) {
                _state.update { it.copy(isSubmitting = false, error = AppError.AnonymousSessionExpired) }
                return@launch
            }
            val input = CreatePollInput(
                projectId = projectId,
                question = current.question.trim(),
                target = if (current.isPublic) PollTarget.Public else PollTarget.Commissions,
                commissionIds = current.selectedCommissionIds.toList(),
                optionLabels = current.options.map { it.trim() }.filter { it.isNotBlank() },
                closesAt = endOfDayIso(current.closesDate),
                createdByMemberId = creatorMemberId,
            )
            pollsRepository.create(input).fold(
                onSuccess = {
                    _state.update { it.copy(isSubmitting = false) }
                    emit(CreatePollEvent.PollCreated)
                },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update { it.copy(isSubmitting = false, error = appError) }
                },
            )
        }
    }

    private fun emit(event: CreatePollEvent) {
        viewModelScope.launch { _events.emit(event) }
    }

    private fun defaultCloseDate(): String {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return today.plus(DatePeriod(days = 7)).toString()
    }

    /** Poll closes at the end of the chosen day (local), so votes are accepted all day. */
    private fun endOfDayIso(dateIso: String): String {
        val tz = TimeZone.currentSystemDefault()
        val date = LocalDate.parse(dateIso)
        val endOfDay = LocalDateTime(date, LocalTime(23, 59, 0))
        return endOfDay.toInstant(tz).toString()
    }
}
