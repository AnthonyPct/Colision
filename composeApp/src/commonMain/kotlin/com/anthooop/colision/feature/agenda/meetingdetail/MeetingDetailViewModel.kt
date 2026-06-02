package com.anthooop.colision.feature.agenda.meetingdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.database.entity.ArbitrationEntity
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.agenda.navigation.AgendaDestination
import com.anthooop.colision.feature.meeting.data.ArbitrationsRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MeetingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingsRepository: MeetingsRepository,
    private val membersRepository: MembersRepository,
    private val arbitrationsRepository: ArbitrationsRepository,
    private val commissionsRepository: CommissionsRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val meetingId: String = savedStateHandle
        .toRoute<AgendaDestination.MeetingDetail>()
        .meetingId

    private val _state = MutableStateFlow(MeetingDetailState())
    val state: StateFlow<MeetingDetailState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<MeetingDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<MeetingDetailEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: MeetingDetailIntent) {
        when (intent) {
            MeetingDetailIntent.BackTapped ->
                viewModelScope.launch { _events.emit(MeetingDetailEvent.NavigateBack) }
            MeetingDetailIntent.EditTapped ->
                viewModelScope.launch { _events.emit(MeetingDetailEvent.NavigateToEdit(meetingId)) }
            MeetingDetailIntent.DeleteTapped ->
                _state.update { it.copy(showDeleteConfirm = true) }
            MeetingDetailIntent.DeleteDismissed ->
                _state.update { it.copy(showDeleteConfirm = false) }
            MeetingDetailIntent.DeleteConfirmed -> performDelete()
            MeetingDetailIntent.ErrorDismissed ->
                _state.update { it.copy(error = null) }
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

    private fun performDelete() {
        if (_state.value.isDeleting) return
        _state.update { it.copy(isDeleting = true, showDeleteConfirm = false, error = null) }
        viewModelScope.launch {
            meetingsRepository.delete(meetingId).fold(
                onSuccess = {
                    _state.update { it.copy(isDeleting = false, isDeleted = true) }
                    _events.emit(MeetingDetailEvent.NavigateBack)
                },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update { it.copy(isDeleting = false, error = appError) }
                },
            )
        }
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
                        membersRepository.observeAttendingMeeting(meetingId),
                        currentMemberProvider.observe(),
                        arbitrationsForMeetingFlow(),
                    ) { commissionIds, allCommissions, attendees, currentMember, arbitrations ->
                        val commissions = allCommissions.filter { it.id in commissionIds }
                        val creator = meeting.createdByMemberId?.let { membersRepository.findById(it) }
                        val isCreator = creator != null && creator.id == currentMember?.id
                        MeetingDetailState(
                            isLoading = false,
                            isDeleted = false,
                            isCreator = isCreator,
                            meeting = meeting,
                            commissions = commissions,
                            attendees = attendees,
                            creator = creator,
                            conflictedAttendees = if (isCreator) {
                                buildConflictedAttendees(
                                    meeting = meeting,
                                    meetingCommissionIds = commissionIds,
                                    allCommissions = allCommissions,
                                    arbitrations = arbitrations,
                                )
                            } else {
                                emptyList()
                            },
                        )
                    }
                }
            }
            .collect { snapshot ->
                _state.update { snapshot }
            }
    }

    private fun arbitrationsForMeetingFlow() =
        combine(
            arbitrationsRepository.observeSkippingMeeting(meetingId),
            arbitrationsRepository.observeChoosingMeeting(meetingId),
        ) { skipping, choosing -> skipping + choosing }

    private suspend fun buildConflictedAttendees(
        meeting: MeetingEntity,
        meetingCommissionIds: List<String>,
        allCommissions: List<CommissionEntity>,
        arbitrations: List<ArbitrationEntity>,
    ): List<ConflictedAttendeeUi> {
        if (meetingCommissionIds.isEmpty()) return emptyList()
        val conflictedMemberIds = meetingsRepository.findConflictMemberIds(
            projectId = meeting.projectId,
            commissionIds = meetingCommissionIds,
            startsAt = meeting.startsAt,
            endsAt = meeting.endsAt,
        )
        if (conflictedMemberIds.isEmpty()) return emptyList()
        val members = conflictedMemberIds.mapNotNull { membersRepository.findById(it) }
        val commissionsById = allCommissions.associateBy { it.id }
        return members.map { member ->
            val arbitration = arbitrations.firstOrNull { it.memberId == member.id }
            when {
                arbitration == null -> ConflictedAttendeeUi(
                    memberId = member.id,
                    memberName = member.displayName,
                    status = ConflictedArbitrationStatus.Pending,
                )
                arbitration.conflictingMeetingId == meetingId -> ConflictedAttendeeUi(
                    memberId = member.id,
                    memberName = member.displayName,
                    status = ConflictedArbitrationStatus.Attends,
                )
                else -> {
                    val otherCommissionIds = meetingsRepository
                        .observeCommissionIds(arbitration.conflictingMeetingId)
                        .first()
                    val otherName = otherCommissionIds.firstNotNullOfOrNull { commissionsById[it] }?.name
                    ConflictedAttendeeUi(
                        memberId = member.id,
                        memberName = member.displayName,
                        status = ConflictedArbitrationStatus.Skips,
                        otherCommissionName = otherName,
                    )
                }
            }
        }.sortedBy { it.memberName }
    }
}
