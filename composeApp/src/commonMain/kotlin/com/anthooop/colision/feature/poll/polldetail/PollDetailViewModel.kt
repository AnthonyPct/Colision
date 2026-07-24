package com.anthooop.colision.feature.poll.polldetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.core.common.ConnectivityObserver
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.core.database.entity.PollEntity
import com.anthooop.colision.core.database.entity.PollOptionEntity
import com.anthooop.colision.core.database.entity.PollVoteEntity
import com.anthooop.colision.feature.poll.data.PollsRepository
import com.anthooop.colision.feature.poll.domain.PollComputations
import com.anthooop.colision.feature.poll.navigation.PollDestination
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import com.anthooop.colision.feature.projecthub.data.MembersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PollDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val pollsRepository: PollsRepository,
    private val commissionsRepository: CommissionsRepository,
    private val membersRepository: MembersRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val connectivity: ConnectivityObserver,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val pollId: String = savedStateHandle.toRoute<PollDestination.PollDetail>().pollId

    private val _state = MutableStateFlow(PollDetailState())
    val state: StateFlow<PollDetailState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<PollDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PollDetailEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: PollDetailIntent) {
        when (intent) {
            is PollDetailIntent.OptionSelected ->
                _state.update { it.copy(pendingOptionId = intent.optionId, error = null) }
            PollDetailIntent.SubmitVote -> submitVote()
            PollDetailIntent.EditVote ->
                _state.update { it.copy(isEditing = true, pendingOptionId = it.myVoteOptionId) }
            PollDetailIntent.BackTapped ->
                viewModelScope.launch { _events.emit(PollDetailEvent.NavigateBack) }
            PollDetailIntent.DeleteTapped -> _state.update { it.copy(showDeleteConfirm = true) }
            PollDetailIntent.DeleteDismissed -> _state.update { it.copy(showDeleteConfirm = false) }
            PollDetailIntent.DeleteConfirmed -> performDelete()
            PollDetailIntent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch { observe() }
        viewModelScope.launch { observeConnectivity() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private suspend fun observeConnectivity() {
        connectivity.isOnline.collect { online -> _state.update { it.copy(isOnline = online) } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observe() {
        pollsRepository.observeById(pollId)
            .flatMapLatest { poll ->
                if (poll == null) {
                    flowOf(PollDetailState(isLoading = false, isDeleted = true))
                } else {
                    val projectData = combine(
                        membersRepository.observeByProject(poll.projectId),
                        membersRepository.observeAssignmentsForProject(poll.projectId),
                        currentMemberProvider.observe(),
                    ) { members, assignments, me -> ProjectBundle(members, assignments, me) }

                    combine(
                        pollsRepository.observeOptions(pollId),
                        pollsRepository.observeVotes(pollId),
                        pollsRepository.observeCommissionIds(pollId),
                        commissionsRepository.observeByProject(poll.projectId),
                        projectData,
                    ) { options, votes, scopeIds, commissions, project ->
                        buildSnapshot(poll, options, votes, scopeIds.toSet(), commissions, project)
                    }
                }
            }
            .collect { snapshot ->
                // Preserve UI-only fields across data-driven rebuilds.
                _state.update { current ->
                    snapshot.copy(
                        pendingOptionId = current.pendingOptionId,
                        isEditing = current.isEditing,
                        isVoting = current.isVoting,
                        showDeleteConfirm = current.showDeleteConfirm,
                        error = current.error,
                        isOnline = current.isOnline,
                    )
                }
            }
    }

    private fun buildSnapshot(
        poll: PollEntity,
        options: List<PollOptionEntity>,
        votes: List<PollVoteEntity>,
        scopeIds: Set<String>,
        commissions: List<CommissionEntity>,
        project: ProjectBundle,
    ): PollDetailState {
        val now = Clock.System.now()
        val myMemberId = project.currentMember?.id
        val myCommissionIds = project.assignments
            .filter { it.memberId == myMemberId }
            .map { it.commissionId }
            .toSet()
        val commissionNameById = commissions.associate { it.id to it.name }
        val total = votes.size
        val counts = votes.groupingBy { it.optionId }.eachCount()
        val optionUis = options.sortedBy { it.position }.map { o ->
            val v = counts[o.id] ?: 0
            PollOptionUi(
                id = o.id,
                label = o.label,
                votes = v,
                percent = if (total > 0) ((v.toDouble() / total) * 100).roundToInt() else 0,
            )
        }
        val creatorName = poll.createdByMemberId?.let { id ->
            project.members.firstOrNull { it.id == id }?.displayName
        }
        return PollDetailState(
            isLoading = false,
            isDeleted = false,
            question = poll.question,
            creatorName = creatorName,
            closesAtIso = poll.closesAt,
            isClosed = !PollComputations.isOpen(poll.closesAt, now),
            targetIsPublic = poll.targetType == PollComputations.TARGET_PUBLIC,
            commissionNames = scopeIds.mapNotNull { commissionNameById[it] },
            eligible = PollComputations.canVote(poll.targetType, scopeIds, myCommissionIds),
            isCreator = myMemberId != null && poll.createdByMemberId == myMemberId,
            options = optionUis,
            totalVotes = total,
            eligibleCount = PollComputations.eligibleCount(
                targetType = poll.targetType,
                scopeCommissionIds = scopeIds,
                projectMemberIds = project.members.map { it.id }.toSet(),
                assignments = project.assignments,
            ),
            daysLeft = PollComputations.daysLeft(poll.closesAt, now).coerceAtLeast(0),
            myVoteOptionId = votes.firstOrNull { it.memberId == myMemberId }?.optionId,
        )
    }

    private fun submitVote() {
        val current = _state.value
        val optionId = current.pendingOptionId ?: return
        if (!current.canSubmitVote) return
        _state.update { it.copy(isVoting = true, error = null) }
        viewModelScope.launch {
            val memberId = currentMemberProvider.current()?.id
            if (memberId == null) {
                _state.update { it.copy(isVoting = false, error = AppError.AnonymousSessionExpired) }
                return@launch
            }
            pollsRepository.vote(pollId, optionId, memberId).fold(
                onSuccess = { _state.update { it.copy(isVoting = false, isEditing = false) } },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update { it.copy(isVoting = false, error = appError) }
                },
            )
        }
    }

    private fun performDelete() {
        if (_state.value.isVoting) return
        _state.update { it.copy(showDeleteConfirm = false, error = null) }
        viewModelScope.launch {
            pollsRepository.delete(pollId).fold(
                onSuccess = {
                    _state.update { it.copy(isDeleted = true) }
                    _events.emit(PollDetailEvent.NavigateBack)
                },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update { it.copy(error = appError) }
                },
            )
        }
    }

    private data class ProjectBundle(
        val members: List<MemberEntity>,
        val assignments: List<MemberCommissionEntity>,
        val currentMember: MemberEntity?,
    )
}
