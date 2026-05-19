package com.anthooop.colision.feature.onboarding.joincommissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinCommissionsViewModel(
    private val commissionsRepository: CommissionsRepository,
    private val membersRepository: MembersRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(JoinCommissionsState())
    val state: StateFlow<JoinCommissionsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<JoinCommissionsEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<JoinCommissionsEvent> = _events.asSharedFlow()

    private var projectId: String = ""
    private var memberId: String = ""
    private var hasInitialised: Boolean = false

    fun load(projectId: String, memberId: String) {
        this.projectId = projectId
        this.memberId = memberId
        viewModelScope.launch { commissionsRepository.refresh(projectId) }
        viewModelScope.launch { membersRepository.refresh(projectId) }
        viewModelScope.launch {
            combine(
                commissionsRepository.observeByProject(projectId),
                membersRepository.observeAssignments(memberId),
            ) { commissions, assignments ->
                commissions to assignments.map { it.commissionId }.toSet()
            }.collectLatest { (commissions, assigned) ->
                _state.update { current ->
                    current.copy(
                        commissions = commissions,
                        isLoading = false,
                        // Seed pre-checked state from server assignments only once.
                        checkedIds = if (hasInitialised) current.checkedIds else assigned,
                    )
                }
                hasInitialised = true
            }
        }
    }

    fun onIntent(intent: JoinCommissionsIntent) {
        when (intent) {
            is JoinCommissionsIntent.CommissionToggled -> _state.update { s ->
                val next = s.checkedIds.toMutableSet().apply {
                    if (contains(intent.commissionId)) remove(intent.commissionId)
                    else add(intent.commissionId)
                }
                s.copy(checkedIds = next)
            }
            JoinCommissionsIntent.ContinueTapped -> persistAndContinue()
            JoinCommissionsIntent.BackTapped -> emit(JoinCommissionsEvent.NavigateBack)
            JoinCommissionsIntent.ErrorDismissed -> _state.update { it.copy(pendingError = null) }
        }
    }

    private fun persistAndContinue() {
        if (!_state.value.canSubmit) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val currentChecked = _state.value.checkedIds
            val existing = membersRepository.observeAssignments(memberId).first()
                .map { it.commissionId }.toSet()
            val toAdd = currentChecked - existing
            val toRemove = existing - currentChecked
            val failures = mutableListOf<Throwable>()
            for (commissionId in toAdd) {
                membersRepository.setAssignment(memberId, commissionId, assigned = true)
                    .onFailure { failures.add(it) }
            }
            for (commissionId in toRemove) {
                membersRepository.setAssignment(memberId, commissionId, assigned = false)
                    .onFailure { failures.add(it) }
            }
            if (failures.isEmpty()) {
                _state.update { it.copy(isSubmitting = false) }
                emit(JoinCommissionsEvent.NavigateToNotificationPermission(projectId, memberId))
            } else {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        pendingError = "Impossible d'enregistrer toutes les commissions — réessaie.",
                    )
                }
            }
        }
    }

    private fun emit(event: JoinCommissionsEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
