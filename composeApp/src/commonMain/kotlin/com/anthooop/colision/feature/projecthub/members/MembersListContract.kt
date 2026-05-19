package com.anthooop.colision.feature.projecthub.members

import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MemberEntity

data class MemberRow(
    val member: MemberEntity,
    val commissionLabels: List<String>,
)

data class MembersListState(
    val isLoading: Boolean = true,
    val rows: List<MemberRow> = emptyList(),
    val addingMember: AddingMember? = null,
    val pendingError: MembersListError? = null,
)

sealed interface MembersListError {
    data class Add(val reason: String) : MembersListError
}

data class AddingMember(
    val name: String = "",
) {
    val canConfirm: Boolean = name.trim().length >= 2
}

sealed interface MembersListIntent {
    data object BackTapped : MembersListIntent
    data object AddTapped : MembersListIntent
    data class AddNameChanged(val value: String) : MembersListIntent
    data object AddCancelled : MembersListIntent
    data object AddConfirmed : MembersListIntent
    data class MemberTapped(val memberId: String) : MembersListIntent
    data object ErrorDismissed : MembersListIntent
}

sealed interface MembersListEvent {
    data object NavigateBack : MembersListEvent
    data class NavigateToCommissions(val memberId: String) : MembersListEvent
}
