package com.anthooop.colision.feature.projecthub.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.core.database.dao.MemberCommissionDao
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import com.anthooop.colision.feature.projecthub.data.MembersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MembersListViewModel(
    private val activeProject: ActiveProjectProvider,
    private val membersRepository: MembersRepository,
    private val commissionsRepository: CommissionsRepository,
    private val memberCommissionDao: MemberCommissionDao,
) : ViewModel() {

    private val _state = MutableStateFlow(MembersListState())
    val state: StateFlow<MembersListState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MembersListEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<MembersListEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            activeProject.observe()
                .flatMapLatest { project ->
                    if (project == null) {
                        flowOf(Triple(emptyList<MemberEntity>(), emptyList<CommissionEntity>(), emptyList<MemberCommissionEntity>()))
                    } else {
                        combine(
                            membersRepository.observeByProject(project.id),
                            commissionsRepository.observeByProject(project.id),
                            memberCommissionDao.observeForProject(project.id),
                        ) { members, commissions, links ->
                            Triple(members, commissions, links)
                        }
                    }
                }
                .collectLatest { (members, commissions, links) ->
                    val commissionsById = commissions.associateBy { it.id }
                    val linksByMember: Map<String, List<MemberCommissionEntity>> = links.groupBy { it.memberId }
                    val rows = members.map { member ->
                        val labels = linksByMember[member.id].orEmpty()
                            .mapNotNull { commissionsById[it.commissionId]?.name }
                        MemberRow(member = member, commissionLabels = labels)
                    }
                    _state.update { it.copy(rows = rows, isLoading = false) }
                }
        }
        viewModelScope.launch {
            val project = activeProject.current() ?: return@launch
            membersRepository.refresh(project.id)
            commissionsRepository.refresh(project.id)
        }
    }

    fun onIntent(intent: MembersListIntent) {
        when (intent) {
            MembersListIntent.BackTapped -> emit(MembersListEvent.NavigateBack)
            MembersListIntent.AddTapped -> _state.update { it.copy(addingMember = AddingMember()) }
            is MembersListIntent.AddNameChanged -> _state.update { s ->
                s.copy(addingMember = s.addingMember?.copy(name = intent.value))
            }
            MembersListIntent.AddCancelled -> _state.update { it.copy(addingMember = null) }
            MembersListIntent.AddConfirmed -> commitAdd()
            is MembersListIntent.MemberTapped -> emit(MembersListEvent.NavigateToCommissions(intent.memberId))
            MembersListIntent.ErrorDismissed -> _state.update { it.copy(pendingError = null) }
        }
    }

    private fun commitAdd() {
        val adding = _state.value.addingMember ?: return
        if (!adding.canConfirm) return
        viewModelScope.launch {
            val project = activeProject.current() ?: return@launch
            membersRepository.addMember(project.id, adding.name.trim()).fold(
                onSuccess = { _state.update { it.copy(addingMember = null) } },
                onFailure = { t ->
                    _state.update {
                        it.copy(
                            addingMember = null,
                            pendingError = MembersListError.Add(t.message.orEmpty()),
                        )
                    }
                },
            )
        }
    }

    private fun emit(event: MembersListEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
