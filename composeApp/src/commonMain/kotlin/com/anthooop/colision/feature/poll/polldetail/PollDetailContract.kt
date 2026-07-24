package com.anthooop.colision.feature.poll.polldetail

import com.anthooop.colision.core.common.AppError

data class PollOptionUi(
    val id: String,
    val label: String,
    val votes: Int,
    val percent: Int,
)

data class PollDetailState(
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val question: String = "",
    val creatorName: String? = null,
    val closesAtIso: String = "",
    val isClosed: Boolean = false,
    val targetIsPublic: Boolean = false,
    val commissionNames: List<String> = emptyList(),
    val eligible: Boolean = false,
    val isCreator: Boolean = false,
    val options: List<PollOptionUi> = emptyList(),
    val totalVotes: Int = 0,
    val eligibleCount: Int = 0,
    val daysLeft: Int = 0,
    val myVoteOptionId: String? = null,
    // Local UI: selection while (re)voting, and whether the results are being
    // temporarily replaced by the ballot after "Changer mon vote".
    val pendingOptionId: String? = null,
    val isEditing: Boolean = false,
    val isVoting: Boolean = false,
    val isOnline: Boolean = true,
    val showDeleteConfirm: Boolean = false,
    val error: AppError? = null,
) {
    val hasVoted: Boolean get() = myVoteOptionId != null

    /** Results replace the ballot when closed, when not eligible, or once voted (unless editing). */
    val showResults: Boolean get() = isClosed || !eligible || (hasVoted && !isEditing)

    val canSubmitVote: Boolean
        get() = !isVoting && !isClosed && eligible && isOnline && pendingOptionId != null
}

sealed interface PollDetailIntent {
    data class OptionSelected(val optionId: String) : PollDetailIntent
    data object SubmitVote : PollDetailIntent
    data object EditVote : PollDetailIntent
    data object BackTapped : PollDetailIntent
    data object DeleteTapped : PollDetailIntent
    data object DeleteDismissed : PollDetailIntent
    data object DeleteConfirmed : PollDetailIntent
    data object ErrorDismissed : PollDetailIntent
}

sealed interface PollDetailEvent {
    data object NavigateBack : PollDetailEvent
}
