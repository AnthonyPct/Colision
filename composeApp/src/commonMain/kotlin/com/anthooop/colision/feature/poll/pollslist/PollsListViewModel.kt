package com.anthooop.colision.feature.poll.pollslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.ConnectivityObserver
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.common.ProjectSyncManager
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.core.database.entity.PollCommissionEntity
import com.anthooop.colision.core.database.entity.PollEntity
import com.anthooop.colision.core.database.entity.PollOptionEntity
import com.anthooop.colision.core.database.entity.PollVoteEntity
import com.anthooop.colision.feature.poll.data.PollsRepository
import com.anthooop.colision.feature.poll.domain.PollComputations
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import com.anthooop.colision.feature.projecthub.data.MembersRepository
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PollsListViewModel(
    private val activeProjectProvider: ActiveProjectProvider,
    private val currentMemberProvider: CurrentMemberProvider,
    private val pollsRepository: PollsRepository,
    private val commissionsRepository: CommissionsRepository,
    private val membersRepository: MembersRepository,
    private val connectivity: ConnectivityObserver,
    private val syncManager: ProjectSyncManager,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(PollsListState())
    val state: StateFlow<PollsListState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<PollsListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PollsListEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: PollsListIntent) {
        when (intent) {
            is PollsListIntent.FilterSelected -> _state.update { it.copy(filter = intent.filter) }
            is PollsListIntent.PollTapped ->
                viewModelScope.launch { _events.emit(PollsListEvent.NavigateToDetail(intent.pollId)) }
            PollsListIntent.CreateTapped ->
                viewModelScope.launch { _events.emit(PollsListEvent.NavigateToCreate) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch { observeData() }
        viewModelScope.launch { observeConnectivity() }
        syncManager.refreshNow()
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeData() {
        activeProjectProvider.observe()
            .flatMapLatest { project ->
                if (project == null) flowOf(PollsListState(isLoading = false)) else snapshotFlow(project.id)
            }
            .collect { snapshot -> _state.update { snapshot.copy(filter = it.filter, isOnline = it.isOnline) } }
    }

    private suspend fun observeConnectivity() {
        connectivity.isOnline.collect { online -> _state.update { it.copy(isOnline = online) } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun snapshotFlow(projectId: String): Flow<PollsListState> {
        val pollData = combine(
            pollsRepository.observeByProject(projectId),
            pollsRepository.observeOptionsForProject(projectId),
            pollsRepository.observeCommissionLinksForProject(projectId),
            pollsRepository.observeVotesForProject(projectId),
        ) { polls, options, links, votes -> PollBundle(polls, options, links, votes) }

        val projectData = combine(
            commissionsRepository.observeByProject(projectId),
            membersRepository.observeByProject(projectId),
            membersRepository.observeAssignmentsForProject(projectId),
        ) { commissions, members, assignments -> ProjectBundle(commissions, members, assignments) }

        return combine(
            pollData,
            projectData,
            currentMemberProvider.observe(),
        ) { poll, project, me ->
            buildState(poll, project, me?.id)
        }
    }

    private fun buildState(
        poll: PollBundle,
        project: ProjectBundle,
        myMemberId: String?,
    ): PollsListState {
        val now = Clock.System.now()
        val commissionNameById = project.commissions.associate { it.id to it.name }
        val projectMemberIds = project.members.map { it.id }.toSet()
        val myCommissionIds = project.assignments
            .filter { it.memberId == myMemberId }
            .map { it.commissionId }
            .toSet()
        val optionsByPoll = poll.options.groupBy { it.pollId }
        val votesByPoll = poll.votes.groupBy { it.pollId }
        val scopeByPoll = poll.commissionLinks.groupBy { it.pollId }

        val items = poll.polls.map { entity ->
            val scope = scopeByPoll[entity.id].orEmpty().map { it.commissionId }.toSet()
            val options = optionsByPoll[entity.id].orEmpty()
            val votes = votesByPoll[entity.id].orEmpty()
            val isClosed = !PollComputations.isOpen(entity.closesAt, now)
            val winner = if (isClosed) winnerLabel(options, votes) else null
            PollListItem(
                id = entity.id,
                question = entity.question,
                targetIsPublic = entity.targetType == PollComputations.TARGET_PUBLIC,
                commissionNames = scope.mapNotNull { commissionNameById[it] },
                isClosed = isClosed,
                eligible = PollComputations.canVote(entity.targetType, scope, myCommissionIds),
                hasVoted = myMemberId != null && votes.any { it.memberId == myMemberId },
                voters = votes.map { it.memberId }.toSet().size,
                daysLeft = PollComputations.daysLeft(entity.closesAt, now).coerceAtLeast(0),
                closesAtIso = entity.closesAt,
                winnerLabel = winner,
            )
        }
        return PollsListState(
            isLoading = false,
            openPolls = items.filter { !it.isClosed },
            closedPolls = items.filter { it.isClosed },
        )
    }

    private fun winnerLabel(options: List<PollOptionEntity>, votes: List<PollVoteEntity>): String? {
        if (options.isEmpty()) return null
        val counts = votes.groupingBy { it.optionId }.eachCount()
        return options.maxByOrNull { counts[it.id] ?: 0 }?.label
    }

    private data class PollBundle(
        val polls: List<PollEntity>,
        val options: List<PollOptionEntity>,
        val commissionLinks: List<PollCommissionEntity>,
        val votes: List<PollVoteEntity>,
    )

    private data class ProjectBundle(
        val commissions: List<CommissionEntity>,
        val members: List<MemberEntity>,
        val assignments: List<MemberCommissionEntity>,
    )
}
