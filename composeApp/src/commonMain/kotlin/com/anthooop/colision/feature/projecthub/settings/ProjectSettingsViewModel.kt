package com.anthooop.colision.feature.projecthub.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.ProjectLifecycleRepository
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

class ProjectSettingsViewModel(
    private val activeProject: ActiveProjectProvider,
    private val projectLifecycle: ProjectLifecycleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectSettingsState())
    val state: StateFlow<ProjectSettingsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProjectSettingsEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<ProjectSettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            activeProject.observe().collectLatest { project ->
                _state.update {
                    it.copy(
                        projectName = project?.name.orEmpty(),
                        shareCode = project?.shareCode.orEmpty(),
                    )
                }
            }
        }
    }

    fun onIntent(intent: ProjectSettingsIntent) {
        when (intent) {
            ProjectSettingsIntent.OpenCommissions -> emit(ProjectSettingsEvent.NavigateToCommissions)
            ProjectSettingsIntent.OpenMembers -> emit(ProjectSettingsEvent.NavigateToMembers)
            ProjectSettingsIntent.LeaveTapped -> _state.update { it.copy(confirming = ConfirmingAction.Leave) }
            ProjectSettingsIntent.DeleteTapped -> _state.update { it.copy(confirming = ConfirmingAction.Delete()) }
            is ProjectSettingsIntent.DeleteConfirmTextChanged -> _state.update { s ->
                val c = s.confirming
                if (c is ConfirmingAction.Delete) s.copy(confirming = c.copy(typed = intent.value)) else s
            }
            ProjectSettingsIntent.CancelCurrentAction -> _state.update { it.copy(confirming = null) }
            ProjectSettingsIntent.ConfirmCurrentAction -> commitConfirming()
            ProjectSettingsIntent.CopyShareCode -> {
                val code = _state.value.shareCode
                if (code.isNotEmpty()) emit(ProjectSettingsEvent.CopyToClipboard(code))
            }
            ProjectSettingsIntent.ErrorDismissed -> _state.update { it.copy(pendingError = null) }
            ProjectSettingsIntent.TransientMessageShown -> _state.update { it.copy(transientMessage = null) }
        }
    }

    private fun commitConfirming() {
        val current = _state.value.confirming ?: return
        viewModelScope.launch {
            val project = activeProject.current() ?: return@launch
            _state.update { it.copy(isProcessing = true) }
            val result = when (current) {
                ConfirmingAction.Leave -> projectLifecycle.leaveProject(project.id)
                is ConfirmingAction.Delete -> {
                    if (!current.canConfirm) {
                        _state.update { it.copy(isProcessing = false) }
                        return@launch
                    }
                    projectLifecycle.deleteProject(project.id)
                }
            }
            result.fold(
                onSuccess = {
                    _state.update { it.copy(confirming = null, isProcessing = false) }
                    emit(ProjectSettingsEvent.NavigateToWelcome)
                },
                onFailure = { t ->
                    _state.update {
                        it.copy(
                            confirming = null,
                            isProcessing = false,
                            pendingError = ProjectSettingsError.Network(t.message.orEmpty()),
                        )
                    }
                },
            )
        }
    }

    private fun emit(event: ProjectSettingsEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
