package com.anthooop.colision.feature.onboarding.projectcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.feature.onboarding.data.ProjectsRepository
import com.anthooop.colision.feature.projecthub.data.MembersRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateProjectViewModel(
    private val projectsRepository: ProjectsRepository,
    private val membersRepository: MembersRepository,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(CreateProjectState())
    val state: StateFlow<CreateProjectState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<CreateProjectEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<CreateProjectEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun onIntent(intent: CreateProjectIntent) {
        when (intent) {
            is CreateProjectIntent.NameChanged -> _state.update { it.copy(name = intent.value, error = null) }
            is CreateProjectIntent.DisplayNameChanged -> _state.update {
                it.copy(displayName = intent.value, error = null)
            }
            CreateProjectIntent.SubmitTapped -> submit()
            CreateProjectIntent.ErrorDismissed -> _state.update { it.copy(error = null) }
            CreateProjectIntent.BackTapped -> emit(CreateProjectEvent.NavigateBack)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = projectsRepository.createProject(
                name = current.name.trim(),
                displayName = current.displayName.trim(),
            )
            result.fold(
                onSuccess = { project ->
                    // The server-side `create_project` RPC inserts the creator's
                    // own member row in the same transaction (see migration
                    // 20260519_008). Mirror that row into Room so the onboarding
                    // gate (project ∧ own member) flips to "done" once the user
                    // finishes the welcome flow — otherwise the start graph
                    // would stay on Onboarding.
                    membersRepository.refresh(project.id)
                    _state.update { it.copy(isSubmitting = false) }
                    emit(CreateProjectEvent.NavigateToShareCode(project.id))
                },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update { it.copy(isSubmitting = false, error = appError) }
                },
            )
        }
    }

    private fun emit(event: CreateProjectEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
