package com.anthooop.colision.feature.projecthub.commissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CommissionsListViewModel(
    private val activeProject: ActiveProjectProvider,
    private val commissionsRepository: CommissionsRepository,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(CommissionsListState())
    val state: StateFlow<CommissionsListState> = _state.asStateFlow()

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<CommissionsListEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<CommissionsListEvent> = _events.asSharedFlow()

    fun onIntent(intent: CommissionsListIntent) {
        when (intent) {
            CommissionsListIntent.BackTapped -> emit(CommissionsListEvent.NavigateBack)
            CommissionsListIntent.AddTapped -> _state.update {
                it.copy(editing = EditingState.Create())
            }
            is CommissionsListIntent.RenameTapped -> _state.update {
                it.copy(editing = EditingState.Rename(intent.id, intent.name, intent.name))
            }
            is CommissionsListIntent.DeleteTapped -> _state.update {
                it.copy(editing = EditingState.ConfirmDelete(intent.id, intent.name))
            }
            is CommissionsListIntent.EditorNameChanged -> _state.update { s ->
                val editing = when (val e = s.editing) {
                    is EditingState.Create -> e.copy(name = intent.value)
                    is EditingState.Rename -> e.copy(name = intent.value)
                    else -> e
                }
                s.copy(editing = editing)
            }
            CommissionsListIntent.EditorCancelled -> _state.update { it.copy(editing = null) }
            CommissionsListIntent.EditorConfirmed -> commitEditor()
            CommissionsListIntent.ErrorDismissed -> _state.update { it.copy(pendingError = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INIT
    ///////////////////////////////////////////////////////////////////////////

    init {
        viewModelScope.launch {
            activeProject.observe()
                .flatMapLatest { project ->
                    if (project == null) flowOf(emptyList())
                    else commissionsRepository.observeByProject(project.id)
                }
                .collectLatest { list ->
                    _state.update { it.copy(commissions = list, isLoading = false) }
                }
        }
        viewModelScope.launch {
            val current = activeProject.current() ?: return@launch
            commissionsRepository.refresh(current.id)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun commitEditor() {
        val current = _state.value.editing ?: return
        viewModelScope.launch {
            val projectId = activeProject.current()?.id ?: return@launch
            val outcome: Result<*> = when (current) {
                is EditingState.Create -> {
                    val n = current.name.trim()
                    if (n.length < 2) return@launch
                    commissionsRepository.create(projectId, n)
                }
                is EditingState.Rename -> {
                    val n = current.name.trim()
                    if (n.length < 2 || n == current.originalName) return@launch
                    commissionsRepository.rename(current.id, n)
                }
                is EditingState.ConfirmDelete -> commissionsRepository.delete(current.id)
            }
            outcome.fold(
                onSuccess = { _state.update { it.copy(editing = null) } },
                onFailure = { t ->
                    _state.update {
                        it.copy(
                            editing = null,
                            pendingError = CommissionsListError.Network(t.message.orEmpty()),
                        )
                    }
                },
            )
        }
    }

    private fun emit(event: CommissionsListEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
