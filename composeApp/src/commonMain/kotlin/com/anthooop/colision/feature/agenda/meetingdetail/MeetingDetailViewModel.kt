package com.anthooop.colision.feature.agenda.meetingdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.agenda.navigation.AgendaDestination
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
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

class MeetingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingsRepository: MeetingsRepository,
    private val memberDao: MemberDao,
    private val commissionsRepository: CommissionsRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : ViewModel() {

    private val meetingId: String = savedStateHandle
        .toRoute<AgendaDestination.MeetingDetail>()
        .meetingId

    private val _state = MutableStateFlow(MeetingDetailState())
    val state: StateFlow<MeetingDetailState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MeetingDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<MeetingDetailEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch { observe() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observe() {
        meetingsRepository.observeById(meetingId)
            .flatMapLatest { meeting ->
                if (meeting == null) {
                    flowOf(MeetingDetailState(isLoading = false, isDeleted = true))
                } else {
                    combine(
                        meetingsRepository.observeCommissionIds(meetingId),
                        commissionsRepository.observeByProject(meeting.projectId),
                        memberDao.observeAttendingMeeting(meetingId),
                        currentMemberProvider.observe(),
                    ) { commissionIds, allCommissions, attendees, currentMember ->
                        val commissions = allCommissions.filter { it.id in commissionIds }
                        val creator = meeting.createdByMemberId?.let { memberDao.findById(it) }
                        MeetingDetailState(
                            isLoading = false,
                            isDeleted = false,
                            isCreator = creator != null && creator.id == currentMember?.id,
                            meeting = meeting,
                            commissions = commissions,
                            attendees = attendees,
                            creator = creator,
                        )
                    }
                }
            }
            .collect { snapshot ->
                _state.update { snapshot }
            }
    }

    fun onIntent(intent: MeetingDetailIntent) {
        when (intent) {
            MeetingDetailIntent.BackTapped -> {
                viewModelScope.launch { _events.emit(MeetingDetailEvent.NavigateBack) }
            }
            MeetingDetailIntent.EditTapped, MeetingDetailIntent.DeleteTapped -> {
                // Epic 4 — wired up later.
            }
        }
    }
}
