package com.anthooop.colision.feature.agenda.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.common.ProjectSyncManager
import com.anthooop.colision.core.database.entity.ArbitrationEntity
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MeetingCommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.meeting.data.ArbitrationsRepository
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AgendaViewModel(
    private val activeProjectProvider: ActiveProjectProvider,
    private val currentMemberProvider: CurrentMemberProvider,
    private val meetingsRepository: MeetingsRepository,
    private val commissionsRepository: CommissionsRepository,
    private val arbitrationsRepository: ArbitrationsRepository,
    private val syncManager: ProjectSyncManager,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(AgendaState())
    val state: StateFlow<AgendaState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<AgendaEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AgendaEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: AgendaIntent) {
        when (intent) {
            is AgendaIntent.ViewSelected -> _state.update { it.copy(view = intent.view) }
            is AgendaIntent.MeetingTapped -> {
                // A tap on a meeting with an unresolved overlap shortcuts to
                // the arbitration screen rather than the detail — without
                // this, the conflicted member has no UI path to arbitrate
                // until the FCM/APNs deep-link is wired in Epic 6.
                val tapped = _state.value.meetings.firstOrNull { it.meeting.id == intent.meetingId }
                val peer = tapped?.conflictWithMeetingId
                viewModelScope.launch {
                    if (peer != null) {
                        _events.emit(AgendaEvent.NavigateToArbitration(peer))
                    } else {
                        _events.emit(AgendaEvent.NavigateToMeetingDetail(intent.meetingId))
                    }
                }
            }
            AgendaIntent.CreateMeetingTapped -> {
                viewModelScope.launch { _events.emit(AgendaEvent.NavigateToCreateMeeting) }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch { observeData() }
        viewModelScope.launch { observeSync() }
        syncManager.refreshNow()
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeData() {
        activeProjectProvider.observe()
            .flatMapLatest { project -> snapshotFlow(project?.id) }
            .collect { snapshot ->
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        firstName = snapshot.firstName,
                        meetings = snapshot.meetings,
                    )
                }
            }
    }

    private suspend fun observeSync() {
        combine(
            syncManager.isOnline,
            syncManager.lastSyncAt,
        ) { online, lastSync ->
            online to lastSync
        }.collect { (online, lastSync) ->
            _state.update {
                it.copy(
                    isOnline = online,
                    lastSyncTime = lastSync?.let(::formatLocalTime),
                )
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
            val arbitrationsFlow: Flow<List<ArbitrationEntity>> = if (memberId == null) {
                flowOf(emptyList())
            } else {
                arbitrationsRepository.observeForMember(memberId)
            }
            combine(
                meetingsFlow,
                commissionsRepository.observeByProject(projectId),
                meetingsRepository.observeLinksForProject(projectId),
                arbitrationsFlow,
            ) { meetings, commissions, links, arbitrations ->
                buildState(name, memberId, meetings, commissions, links, arbitrations)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun buildState(
        firstName: String,
        memberId: String?,
        meetings: List<MeetingEntity>,
        commissions: List<CommissionEntity>,
        links: List<MeetingCommissionEntity>,
        arbitrations: List<ArbitrationEntity>,
    ): AgendaState {
        // The dashboard only shows what's still to come, so the first row is
        // always the next meeting. Past (finished) meetings are dropped before
        // conflict computation too — there's nothing left to arbitrate on them.
        val upcoming = upcomingMeetings(meetings, Clock.System.now())
        val byId = commissions.associateBy { it.id }
        val linksByMeeting = links.groupBy { it.meetingId }
        val conflictPeers = if (memberId == null) {
            emptyMap()
        } else {
            computeConflictPeers(memberId, upcoming, arbitrations)
        }
        val items = upcoming.map { m ->
            val peer = conflictPeers[m.id]
            AgendaMeeting(
                meeting = m,
                commissions = linksByMeeting[m.id].orEmpty().mapNotNull { byId[it.commissionId] },
                conflicted = peer != null,
                conflictWithMeetingId = peer,
            )
        }
        return AgendaState(
            isLoading = false,
            firstName = firstName,
            meetings = items,
        )
    }

    /**
     * Returns `meetingId → peerMeetingId` for every meeting of the current
     * member that overlaps another of the member's meetings *and* has not
     * yet been arbitrated. Resolved pairs (any arbitration row for the
     * member on the (A, B) pair, in either ordering) are filtered out so
     * the badge disappears the moment the user commits a choice.
     */
    private fun computeConflictPeers(
        memberId: String,
        meetings: List<MeetingEntity>,
        arbitrations: List<ArbitrationEntity>,
    ): Map<String, String> {
        if (meetings.size < 2) return emptyMap()
        val resolved: Set<Pair<String, String>> = arbitrations
            .asSequence()
            .filter { it.memberId == memberId }
            .map { unorderedPair(it.meetingId, it.conflictingMeetingId) }
            .toSet()
        val peers = mutableMapOf<String, String>()
        val sorted = meetings.sortedBy { it.startsAt }
        for (i in sorted.indices) {
            val a = sorted[i]
            for (j in i + 1 until sorted.size) {
                val b = sorted[j]
                if (b.startsAt >= a.endsAt) break // sorted: no further overlap possible
                if (a.id == b.id) continue
                if (unorderedPair(a.id, b.id) in resolved) continue
                // Keep the *first* peer found for each side; one banner is
                // enough to drive the user into the arbitration screen, and
                // the arbitration screen itself re-resolves the pair from
                // the current member's overlapping engagements. `putIfAbsent`
                // is JVM-only — use an explicit guard so this stays callable
                // from Kotlin/Native (iOS).
                if (a.id !in peers) peers[a.id] = b.id
                if (b.id !in peers) peers[b.id] = a.id
            }
        }
        return peers
    }

    private fun unorderedPair(a: String, b: String): Pair<String, String> =
        if (a <= b) a to b else b to a

    @OptIn(ExperimentalTime::class)
    private fun formatLocalTime(instant: Instant): String {
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hh = ldt.hour.toString().padStart(2, '0')
        val mm = ldt.minute.toString().padStart(2, '0')
        return "${hh}h${mm}"
    }
}

/**
 * Keeps only meetings that are not over yet, relative to [now]: a meeting is
 * dropped once its `endsAt` is in the past. Ongoing meetings (started but not
 * finished) are kept, so "the next thing" is never hidden while it's happening.
 *
 * Meetings whose `endsAt` can't be parsed are kept (fail-open) rather than
 * silently dropped. Extracted as a pure function so the dashboard's
 * past-meeting filter is unit-testable without the ViewModel.
 */
@OptIn(ExperimentalTime::class)
internal fun upcomingMeetings(
    meetings: List<MeetingEntity>,
    now: Instant,
): List<MeetingEntity> = meetings.filter { meeting ->
    val end = runCatching { Instant.parse(meeting.endsAt) }.getOrNull()
    end == null || end >= now
}
