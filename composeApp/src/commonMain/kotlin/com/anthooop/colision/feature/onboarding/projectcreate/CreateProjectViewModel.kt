package com.anthooop.colision.feature.onboarding.projectcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.AppErrorThrowable
import com.anthooop.colision.feature.onboarding.data.ProjectsRepository
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
) : ViewModel() {

    private val _state = MutableStateFlow(CreateProjectState())
    val state: StateFlow<CreateProjectState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateProjectEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<CreateProjectEvent> = _events.asSharedFlow()

    fun onIntent(intent: CreateProjectIntent) {
        when (intent) {
            is CreateProjectIntent.NameChanged -> _state.update { it.copy(name = intent.value, error = null) }
            CreateProjectIntent.SubmitTapped -> submit()
            CreateProjectIntent.ErrorDismissed -> _state.update { it.copy(error = null) }
            CreateProjectIntent.BackTapped -> emit(CreateProjectEvent.NavigateBack)
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = projectsRepository.createProject(current.name.trim())
            result.fold(
                onSuccess = { project ->
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
