package com.anthooop.colision.feature.onboarding.joincode

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

class JoinCodeViewModel(
    private val projectsRepository: ProjectsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(JoinCodeState())
    val state: StateFlow<JoinCodeState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<JoinCodeEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<JoinCodeEvent> = _events.asSharedFlow()

    fun onIntent(intent: JoinCodeIntent) {
        when (intent) {
            is JoinCodeIntent.CodeChanged -> {
                val cleaned = intent.value
                    .uppercase()
                    .filter { it.isLetterOrDigit() }
                    .take(6)
                _state.update {
                    it.copy(code = cleaned, error = null, resolvedProjectName = null)
                }
                if (cleaned.length == 6) resolve()
            }
            JoinCodeIntent.SubmitTapped -> {
                if (_state.value.resolvedProjectName != null) {
                    // Already resolved; emit navigation event with cached projectId via a re-resolve.
                    resolve(navigateOnSuccess = true)
                } else {
                    resolve(navigateOnSuccess = true)
                }
            }
            JoinCodeIntent.BackTapped -> emit(JoinCodeEvent.NavigateBack)
            JoinCodeIntent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun resolve(navigateOnSuccess: Boolean = false) {
        val code = _state.value.code
        if (code.length != 6 || _state.value.isResolving) return
        _state.update { it.copy(isResolving = true, error = null) }
        viewModelScope.launch {
            projectsRepository.resolveByCode(code).fold(
                onSuccess = { project ->
                    _state.update {
                        it.copy(isResolving = false, resolvedProjectName = project.name)
                    }
                    if (navigateOnSuccess) {
                        emit(JoinCodeEvent.NavigateToConfirm(project.id))
                    }
                },
                onFailure = { t ->
                    val appError = (t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)
                    _state.update {
                        it.copy(
                            isResolving = false,
                            resolvedProjectName = null,
                            error = appError,
                        )
                    }
                },
            )
        }
    }

    private fun emit(event: JoinCodeEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
