package com.anthooop.colision.feature.onboarding.projectsharecode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class ProjectShareCodeViewModel(
    private val projectsRepository: ProjectsRepository,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(ProjectShareCodeState())
    val state: StateFlow<ProjectShareCodeState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<ProjectShareCodeEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<ProjectShareCodeEvent> = _events.asSharedFlow()

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun load(projectId: String) {
        viewModelScope.launch {
            val project = projectsRepository.getProject(projectId)
            if (project != null) {
                _state.update {
                    it.copy(
                        projectName = project.name,
                        shareCode = project.shareCode,
                        isLoading = false,
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onIntent(intent: ProjectShareCodeIntent) {
        when (intent) {
            ProjectShareCodeIntent.CopyTapped -> {
                val code = _state.value.shareCode
                if (code.isNotEmpty()) emit(ProjectShareCodeEvent.CopyToClipboard(code))
            }
            ProjectShareCodeIntent.ContinueTapped -> emit(ProjectShareCodeEvent.NavigateToHome)
            ProjectShareCodeIntent.BackTapped -> emit(ProjectShareCodeEvent.NavigateBack)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun emit(event: ProjectShareCodeEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
