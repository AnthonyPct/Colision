package com.anthooop.colision.feature.projecthub.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import com.anthooop.colision.feature.projecthub.data.MembersRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MemberCommissionsViewModel(
    private val activeProject: ActiveProjectProvider,
    private val membersRepository: MembersRepository,
    private val commissionsRepository: CommissionsRepository,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(MemberCommissionsState())
    val state: StateFlow<MemberCommissionsState> = _state.asStateFlow()

    private var memberId: String = ""

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<MemberCommissionsEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<MemberCommissionsEvent> = _events.asSharedFlow()

    fun onIntent(intent: MemberCommissionsIntent) {
        when (intent) {
            MemberCommissionsIntent.BackTapped -> emit(MemberCommissionsEvent.NavigateBack)
            is MemberCommissionsIntent.CommissionToggled -> toggle(intent.commissionId)
            MemberCommissionsIntent.ErrorDismissed -> _state.update { it.copy(pendingError = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun load(memberId: String) {
        this.memberId = memberId
        viewModelScope.launch {
            val project = activeProject.current() ?: return@launch
            combine(
                commissionsRepository.observeByProject(project.id),
                membersRepository.observeAssignments(memberId),
                membersRepository.observeByProject(project.id),
            ) { commissions, assignments, members ->
                Triple(commissions, assignments.map { it.commissionId }.toSet(), members)
            }.collectLatest { (commissions, assignedIds, members) ->
                val memberName = members.firstOrNull { it.id == memberId }?.displayName.orEmpty()
                _state.update {
                    it.copy(
                        memberName = memberName,
                        commissions = commissions,
                        assignedIds = assignedIds,
                        isLoading = false,
                    )
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun toggle(commissionId: String) {
        if (memberId.isEmpty()) return
        val currentlyAssigned = commissionId in _state.value.assignedIds
        // Optimistic UI update; on failure surface an error and re-sync.
        _state.update {
            val next = it.assignedIds.toMutableSet().apply {
                if (currentlyAssigned) remove(commissionId) else add(commissionId)
            }
            it.copy(assignedIds = next)
        }
        viewModelScope.launch {
            membersRepository.setAssignment(memberId, commissionId, !currentlyAssigned).onFailure { t ->
                _state.update {
                    it.copy(pendingError = MemberCommissionsError.Toggle(t.message.orEmpty()))
                }
            }
        }
    }

    private fun emit(event: MemberCommissionsEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
