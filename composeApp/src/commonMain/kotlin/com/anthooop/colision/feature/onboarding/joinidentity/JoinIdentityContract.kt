package com.anthooop.colision.feature.onboarding.joinidentity

import com.anthooop.colision.core.database.entity.MemberEntity

data class JoinIdentityState(
    val isLoading: Boolean = true,
    val members: List<MemberEntity> = emptyList(),
    val query: String = "",
    val selectedMemberId: String? = null,
    val addNewIdentity: AddNewIdentity? = null,
    val isSubmitting: Boolean = false,
    val pendingError: JoinIdentityError? = null,
) {
    val filteredMembers: List<MemberEntity>
        get() = if (query.isBlank()) members
        else members.filter { it.displayName.contains(query, ignoreCase = true) }
    val canConfirm: Boolean = selectedMemberId != null && !isSubmitting
}

data class AddNewIdentity(val name: String = "") {
    val canSubmit: Boolean = name.trim().length >= 2
}

sealed interface JoinIdentityError {
    data object SessionMissing : JoinIdentityError
    data class Claim(val reason: String) : JoinIdentityError
    data class Add(val reason: String) : JoinIdentityError
}

sealed interface JoinIdentityIntent {
    data class QueryChanged(val value: String) : JoinIdentityIntent
    data class MemberSelected(val memberId: String) : JoinIdentityIntent
    data object AddTapped : JoinIdentityIntent
    data class AddNameChanged(val value: String) : JoinIdentityIntent
    data object AddCancelled : JoinIdentityIntent
    data object AddConfirmed : JoinIdentityIntent
    data object ConfirmTapped : JoinIdentityIntent
    data object BackTapped : JoinIdentityIntent
    data object ErrorDismissed : JoinIdentityIntent
}

sealed interface JoinIdentityEvent {
    data class NavigateToCommissions(val projectId: String, val memberId: String) : JoinIdentityEvent
    data object NavigateBack : JoinIdentityEvent
}
