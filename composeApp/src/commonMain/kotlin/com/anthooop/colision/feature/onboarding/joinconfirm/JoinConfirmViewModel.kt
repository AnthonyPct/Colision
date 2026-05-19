package com.anthooop.colision.feature.onboarding.joinconfirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.feature.onboarding.data.ProjectsRepository
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinConfirmViewModel(
    private val projectsRepository: ProjectsRepository,
    private val commissionsRepository: CommissionsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(JoinConfirmState())
    val state: StateFlow<JoinConfirmState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<JoinConfirmEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<JoinConfirmEvent> = _events.asSharedFlow()

    private var projectId: String = ""

    fun load(projectId: String) {
        this.projectId = projectId
        viewModelScope.launch {
            val project = projectsRepository.getProject(projectId)
            _state.update {
                it.copy(projectName = project?.name.orEmpty(), isLoading = false)
            }
            launch { commissionsRepository.refresh(projectId) }
            commissionsRepository.observeByProject(projectId)
                .collectLatest { commissions ->
                    _state.update { it.copy(commissions = commissions) }
                }
        }
    }

    fun onIntent(intent: JoinConfirmIntent) {
        when (intent) {
            JoinConfirmIntent.ConfirmTapped -> emit(JoinConfirmEvent.NavigateToIdentity(projectId))
            JoinConfirmIntent.WrongProjectTapped, JoinConfirmIntent.BackTapped ->
                emit(JoinConfirmEvent.NavigateBack)
        }
    }

    private fun emit(event: JoinConfirmEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
