package com.anthooop.colision.feature.poll.createpoll

import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.database.entity.CommissionEntity

data class CreatePollState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val question: String = "",
    val options: List<String> = listOf("", ""),
    val isPublic: Boolean = false,
    val selectedCommissionIds: Set<String> = emptySet(),
    val commissions: List<CommissionEntity> = emptyList(),
    val closesDate: String = "",
    val eligibleCount: Int = 0,
    val isOnline: Boolean = true,
    val error: AppError? = null,
) {
    val validOptionCount: Int get() = options.count { it.isNotBlank() }

    val canCreate: Boolean
        get() = !isSubmitting &&
            !isLoading &&
            question.isNotBlank() &&
            validOptionCount >= 2 &&
            (isPublic || selectedCommissionIds.isNotEmpty()) &&
            closesDate.isNotBlank() &&
            isOnline
}

sealed interface CreatePollIntent {
    data class QuestionChanged(val value: String) : CreatePollIntent
    data class OptionChanged(val index: Int, val value: String) : CreatePollIntent
    data object OptionAdded : CreatePollIntent
    data class OptionRemoved(val index: Int) : CreatePollIntent
    data class PublicToggled(val isPublic: Boolean) : CreatePollIntent
    data class CommissionToggled(val commissionId: String) : CreatePollIntent
    data class DateSelected(val iso: String) : CreatePollIntent
    data object SubmitTapped : CreatePollIntent
    data object BackTapped : CreatePollIntent
    data object ErrorDismissed : CreatePollIntent
}

sealed interface CreatePollEvent {
    data object NavigateBack : CreatePollEvent
    data object PollCreated : CreatePollEvent
}
