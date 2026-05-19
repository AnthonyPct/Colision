package com.anthooop.colision.feature.agenda.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MeetingCommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
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

class AgendaViewModel(
    private val activeProjectProvider: ActiveProjectProvider,
    private val currentMemberProvider: CurrentMemberProvider,
    private val meetingsRepository: MeetingsRepository,
    private val commissionsRepository: CommissionsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaState())
    val state: StateFlow<AgendaState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AgendaEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AgendaEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch { observeData() }
        viewModelScope.launch { refresh() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeData() {
        activeProjectProvider.observe()
            .flatMapLatest { project -> snapshotFlow(project?.id) }
            .collect { snapshot ->
                _state.update { current ->
                    snapshot.copy(view = current.view)
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun snapshotFlow(projectId: String?): Flow<AgendaState> {
        if (projectId == null) {
            return flowOf(AgendaState(isLoading = false))
        }
        return currentMemberProvider.observe().flatMapLatest { member ->
            val name = member?.displayName?.substringBefore(' ').orEmpty()
            val memberId = member?.id
            val meetingsFlow: Flow<List<MeetingEntity>> = if (memberId == null) {
                flowOf(emptyList())
            } else {
                meetingsRepository.observeForMember(memberId)
            }
            combine(
                meetingsFlow,
                commissionsRepository.observeByProject(projectId),
                meetingsRepository.observeLinksForProject(projectId),
            ) { meetings, commissions, links ->
                buildState(name, meetings, commissions, links)
            }
        }
    }

    private fun buildState(
        firstName: String,
        meetings: List<MeetingEntity>,
        commissions: List<CommissionEntity>,
        links: List<MeetingCommissionEntity>,
    ): AgendaState {
        val byId = commissions.associateBy { it.id }
        val linksByMeeting = links.groupBy { it.meetingId }
        val items = meetings.map { m ->
            AgendaMeeting(
                meeting = m,
                commissions = linksByMeeting[m.id].orEmpty().mapNotNull { byId[it.commissionId] },
                conflicted = false,
            )
        }
        return AgendaState(
            isLoading = false,
            firstName = firstName,
            meetings = items,
        )
    }

    private suspend fun refresh() {
        val projectId = activeProjectProvider.current()?.id ?: return
        meetingsRepository.refresh(projectId)
    }

    fun onIntent(intent: AgendaIntent) {
        when (intent) {
            is AgendaIntent.ViewSelected -> _state.update { it.copy(view = intent.view) }
            is AgendaIntent.MeetingTapped -> {
                viewModelScope.launch { _events.emit(AgendaEvent.NavigateToMeetingDetail(intent.meetingId)) }
            }
            AgendaIntent.CreateMeetingTapped -> {
                viewModelScope.launch { _events.emit(AgendaEvent.NavigateToCreateMeeting) }
            }
        }
    }
}
