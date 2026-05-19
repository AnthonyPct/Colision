package com.anthooop.colision.feature.onboarding.joinidentity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anthooop.colision.feature.projecthub.data.MembersRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
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

class JoinIdentityViewModel(
    private val membersRepository: MembersRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // UI STATE
    ///////////////////////////////////////////////////////////////////////////

    private val _state = MutableStateFlow(JoinIdentityState())
    val state: StateFlow<JoinIdentityState> = _state.asStateFlow()

    private var projectId: String = ""

    ///////////////////////////////////////////////////////////////////////////
    // EVENT
    ///////////////////////////////////////////////////////////////////////////

    private val _events = MutableSharedFlow<JoinIdentityEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<JoinIdentityEvent> = _events.asSharedFlow()

    fun onIntent(intent: JoinIdentityIntent) {
        when (intent) {
            is JoinIdentityIntent.QueryChanged -> _state.update { it.copy(query = intent.value) }
            is JoinIdentityIntent.MemberSelected -> _state.update {
                it.copy(selectedMemberId = intent.memberId)
            }
            JoinIdentityIntent.AddTapped -> _state.update { it.copy(addNewIdentity = AddNewIdentity()) }
            is JoinIdentityIntent.AddNameChanged -> _state.update { s ->
                s.copy(addNewIdentity = s.addNewIdentity?.copy(name = intent.value))
            }
            JoinIdentityIntent.AddCancelled -> _state.update { it.copy(addNewIdentity = null) }
            JoinIdentityIntent.AddConfirmed -> addNewIdentityAndClaim()
            JoinIdentityIntent.ConfirmTapped -> confirmAndClaim()
            JoinIdentityIntent.BackTapped -> emit(JoinIdentityEvent.NavigateBack)
            JoinIdentityIntent.ErrorDismissed -> _state.update { it.copy(pendingError = null) }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC API
    ///////////////////////////////////////////////////////////////////////////

    fun load(projectId: String) {
        this.projectId = projectId
        viewModelScope.launch { membersRepository.refresh(projectId) }
        viewModelScope.launch {
            membersRepository.observeByProject(projectId).collectLatest { members ->
                _state.update {
                    it.copy(
                        members = members,
                        isLoading = false,
                        // Auto-preselect the first member to match the design (Sophie default).
                        selectedMemberId = it.selectedMemberId ?: members.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // HELPER
    ///////////////////////////////////////////////////////////////////////////

    private fun currentDeviceId(): String? = supabase.auth.currentUserOrNull()?.id

    private fun confirmAndClaim() {
        val memberId = _state.value.selectedMemberId ?: return
        val deviceId = currentDeviceId() ?: run {
            _state.update { it.copy(pendingError = JoinIdentityError.SessionMissing) }
            return
        }
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            membersRepository.claimIdentity(memberId, deviceId).fold(
                onSuccess = {
                    _state.update { it.copy(isSubmitting = false) }
                    emit(JoinIdentityEvent.NavigateToCommissions(projectId, memberId))
                },
                onFailure = { t ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            pendingError = JoinIdentityError.Claim(t.message.orEmpty()),
                        )
                    }
                },
            )
        }
    }

    private fun addNewIdentityAndClaim() {
        val adding = _state.value.addNewIdentity ?: return
        if (!adding.canSubmit) return
        val deviceId = currentDeviceId() ?: run {
            _state.update { it.copy(pendingError = JoinIdentityError.SessionMissing) }
            return
        }
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            // Insert with device_id in a single call — the RLS policy
            // accepts a row whose device_id matches the current device even
            // before the device is a member of the project (self-bootstrap).
            membersRepository.addMember(
                projectId = projectId,
                displayName = adding.name.trim(),
                deviceId = deviceId,
            ).fold(
                onSuccess = { createdMember ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            addNewIdentity = null,
                            selectedMemberId = createdMember.id,
                        )
                    }
                    emit(JoinIdentityEvent.NavigateToCommissions(projectId, createdMember.id))
                },
                onFailure = { t ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            addNewIdentity = null,
                            pendingError = JoinIdentityError.Add(t.message.orEmpty()),
                        )
                    }
                },
            )
        }
    }

    private fun emit(event: JoinIdentityEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
