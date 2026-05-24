package com.anthooop.colision.feature.agenda.commissiondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.database.dao.CommissionDao
import com.anthooop.colision.core.database.dao.MemberCommissionDao
import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.agenda.navigation.AgendaDestination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommissionDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val commissionDao: CommissionDao,
    private val memberDao: MemberDao,
    private val memberCommissionDao: MemberCommissionDao,
    private val meetingsRepository: MeetingsRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val commissionId: String = savedStateHandle
        .toRoute<AgendaDestination.CommissionDetail>()
        .commissionId

    private val _state = MutableStateFlow(CommissionDetailState())
    val state: StateFlow<CommissionDetailState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<CommissionDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CommissionDetailEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: CommissionDetailIntent) {
        when (intent) {
            CommissionDetailIntent.BackTapped -> emit(CommissionDetailEvent.NavigateBack)
            is CommissionDetailIntent.MeetingTapped ->
                emit(CommissionDetailEvent.NavigateToMeetingDetail(intent.meetingId))
            CommissionDetailIntent.CreateMeetingTapped ->
                emit(CommissionDetailEvent.NavigateToCreateMeeting)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch { observe() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private suspend fun observe() {
        val commissionFlow = flow { emit(commissionDao.findById(commissionId)) }
        combine(
            commissionFlow,
            memberDao.observeByCommission(commissionId),
            meetingsRepository.observeByCommission(commissionId),
            memberCommissionDao.observeByCommission(commissionId),
            currentMemberProvider.observe(),
        ) { commission, members, meetings, links, currentMember ->
            CommissionDetailState(
                isLoading = false,
                commission = commission,
                members = members,
                meetings = meetings,
                currentMemberIsMember = currentMember?.id != null &&
                    links.any { it.memberId == currentMember.id },
            )
        }.collect { snapshot ->
            _state.update { snapshot }
        }
    }

    private fun emit(event: CommissionDetailEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
