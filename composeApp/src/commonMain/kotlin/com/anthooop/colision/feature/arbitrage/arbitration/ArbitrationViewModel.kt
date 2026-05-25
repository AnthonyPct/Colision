package com.anthooop.colision.feature.arbitrage.arbitration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.common.ProjectSyncManager
import com.anthooop.colision.core.database.dao.ArbitrationDao
import com.anthooop.colision.core.database.dao.CommissionDao
import com.anthooop.colision.core.database.dao.MeetingDao
import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.core.database.entity.MeetingEntity
import com.anthooop.colision.feature.arbitrage.navigation.ArbitrageDestination
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArbitrationViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingDao: MeetingDao,
    private val memberDao: MemberDao,
    private val commissionDao: CommissionDao,
    private val arbitrationDao: ArbitrationDao,
    private val currentMemberProvider: CurrentMemberProvider,
    private val syncManager: ProjectSyncManager,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val conflictMeetingId: String = savedStateHandle
        .toRoute<ArbitrageDestination.Arbitration>()
        .conflictMeetingId

    private val _state = MutableStateFlow(ArbitrationState())
    val state: StateFlow<ArbitrationState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<ArbitrationEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<ArbitrationEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: ArbitrationIntent) {
        when (intent) {
            ArbitrationIntent.BackTapped ->
                emit(ArbitrationEvent.NavigateBack)
            is ArbitrationIntent.ChoiceTapped ->
                _state.update { it.copy(currentChoice = intent.choice) }
            ArbitrationIntent.SubmitTapped -> Unit // wired in story 5.2
            ArbitrationIntent.ErrorDismissed ->
                _state.update { it.copy(error = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        // Tap-from-push (FR32) hits this VM cold. Trigger a sync first so the
        // freshly-created conflicting meeting (still in flight server-side
        // when the push fired) is present in Room before we resolve the pair.
        syncManager.refreshNow()
        viewModelScope.launch { observe() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private suspend fun observe() {
        combine(
            currentMemberProvider.observe(),
            meetingDao.observeById(conflictMeetingId),
        ) { member, meetingB -> member to meetingB }
            .collect { (member, meetingB) ->
                // The arbitration target is the meeting from the push payload.
                // If it disappeared (creator cancelled while the push was in
                // flight), tell the user there's nothing to arbitrate.
                if (meetingB == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isResolved = true,
                            meetingA = null,
                            meetingB = null,
                        )
                    }
                    return@collect
                }
                if (member == null) {
                    _state.update { it.copy(isLoading = false) }
                    return@collect
                }
                val meetingA = findOverlappingOwnMeeting(member.id, meetingB)
                if (meetingA == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isResolved = true,
                            meetingA = null,
                            meetingB = null,
                        )
                    }
                    return@collect
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        isResolved = false,
                        meetingA = toUi(meetingA),
                        meetingB = toUi(meetingB),
                        currentChoice = existingChoice(
                            memberId = member.id,
                            meetingAId = meetingA.id,
                            meetingBId = meetingB.id,
                        ),
                    )
                }
            }
    }

    private suspend fun findOverlappingOwnMeeting(
        memberId: String,
        meetingB: MeetingEntity,
    ): MeetingEntity? {
        val mine = meetingDao.observeForMember(memberId).first()
        return mine.firstOrNull { other ->
            other.id != meetingB.id &&
                other.projectId == meetingB.projectId &&
                other.startsAt < meetingB.endsAt &&
                other.endsAt > meetingB.startsAt
        }
    }

    /**
     * Looks up any persisted arbitration row for the (A, B) pair on the
     * current member. By story 5.2 convention `meeting_id` holds the meeting
     * the member chose to skip and `conflicting_meeting_id` holds the one
     * they chose to attend — so a row `meetingId = B` means "I go to A".
     */
    private suspend fun existingChoice(
        memberId: String,
        meetingAId: String,
        meetingBId: String,
    ): ArbitrationChoice? {
        val candidates = arbitrationDao.observeSkippingMeeting(meetingAId).first() +
            arbitrationDao.observeSkippingMeeting(meetingBId).first()
        val row = candidates.firstOrNull {
            it.memberId == memberId &&
                ((it.meetingId == meetingAId && it.conflictingMeetingId == meetingBId) ||
                    (it.meetingId == meetingBId && it.conflictingMeetingId == meetingAId))
        } ?: return null
        return if (row.meetingId == meetingBId) ArbitrationChoice.GoingToA
        else ArbitrationChoice.GoingToB
    }

    private suspend fun toUi(meeting: MeetingEntity): ArbitrationMeetingUi {
        val commissionIds = meetingDao.observeCommissionIdsFor(meeting.id).first()
        val commissionName = commissionIds
            .firstNotNullOfOrNull { commissionDao.findById(it)?.name }
            .orEmpty()
        val invited = memberDao.observeAttendingMeeting(meeting.id).first().size
        val organizer = meeting.createdByMemberId?.let { memberDao.findById(it)?.displayName }
        val titleFallback = meeting.title?.takeIf { it.isNotBlank() } ?: commissionName
        return ArbitrationMeetingUi(
            meetingId = meeting.id,
            commissionName = commissionName,
            title = titleFallback,
            startsAt = meeting.startsAt,
            endsAt = meeting.endsAt,
            invitedCount = invited,
            organizerName = organizer,
        )
    }

    private fun emit(event: ArbitrationEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
