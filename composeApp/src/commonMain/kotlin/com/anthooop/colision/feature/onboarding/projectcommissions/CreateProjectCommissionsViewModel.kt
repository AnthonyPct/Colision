package com.anthooop.colision.feature.onboarding.projectcommissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateProjectCommissionsViewModel(
    private val commissionsRepository: CommissionsRepository,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(CreateProjectCommissionsState())
    val state: StateFlow<CreateProjectCommissionsState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<CreateProjectCommissionsEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<CreateProjectCommissionsEvent> = _events.asSharedFlow()

    private var observeJob: Job? = null

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    // Called by the Route with the projectId carried by the nav destination —
    // the project was just created remotely, so we observe Room and refresh once.
    fun load(projectId: String) {
        if (_state.value.projectId == projectId && observeJob != null) return
        _state.update { it.copy(projectId = projectId) }
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            commissionsRepository.observeByProject(projectId).collect { list ->
                _state.update { it.copy(commissions = list) }
            }
        }
        viewModelScope.launch { commissionsRepository.refresh(projectId) }
    }

    fun onIntent(intent: CreateProjectCommissionsIntent) {
        when (intent) {
            is CreateProjectCommissionsIntent.DraftNameChanged ->
                _state.update { it.copy(draftName = intent.value, pendingError = null) }
            CreateProjectCommissionsIntent.AddTapped -> addCommission()
            is CreateProjectCommissionsIntent.RemoveTapped -> removeCommission(intent.id)
            CreateProjectCommissionsIntent.ContinueTapped ->
                emit(CreateProjectCommissionsEvent.NavigateToShareCode(_state.value.projectId))
            CreateProjectCommissionsIntent.BackTapped ->
                emit(CreateProjectCommissionsEvent.NavigateBack)
            CreateProjectCommissionsIntent.ErrorDismissed ->
                _state.update { it.copy(pendingError = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun addCommission() {
        val current = _state.value
        val name = current.draftName.trim()
        if (name.length < 2 || current.isAdding) return
        _state.update { it.copy(isAdding = true) }
        viewModelScope.launch {
            commissionsRepository.create(current.projectId, name).fold(
                onSuccess = { _state.update { it.copy(isAdding = false, draftName = "") } },
                onFailure = { t ->
                    _state.update {
                        it.copy(
                            isAdding = false,
                            pendingError = CreateProjectCommissionsError.Network(t.message.orEmpty()),
                        )
                    }
                },
            )
        }
    }

    private fun removeCommission(id: String) {
        viewModelScope.launch {
            commissionsRepository.delete(id).onFailure { t ->
                _state.update {
                    it.copy(pendingError = CreateProjectCommissionsError.Network(t.message.orEmpty()))
                }
            }
        }
    }

    private fun emit(event: CreateProjectCommissionsEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
