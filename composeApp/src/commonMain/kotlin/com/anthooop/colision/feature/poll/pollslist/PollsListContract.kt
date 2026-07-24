package com.anthooop.colision.feature.poll.pollslist

enum class PollFilter { Open, Closed }

/**
 * One row in the polls list. Date formatting is deferred to the Screen (needs
 * month-name string resources), so the item carries the raw `closesAtIso`
 * plus the already-derived open/closed + day-count.
 */
data class PollListItem(
    val id: String,
    val question: String,
    val targetIsPublic: Boolean,
    val commissionNames: List<String>,
    val isClosed: Boolean,
    val eligible: Boolean,
    val hasVoted: Boolean,
    val voters: Int,
    val daysLeft: Int,
    val closesAtIso: String,
    val winnerLabel: String?,
)

data class PollsListState(
    val isLoading: Boolean = true,
    val filter: PollFilter = PollFilter.Open,
    val openPolls: List<PollListItem> = emptyList(),
    val closedPolls: List<PollListItem> = emptyList(),
    val isOnline: Boolean = true,
) {
    val visiblePolls: List<PollListItem>
        get() = if (filter == PollFilter.Open) openPolls else closedPolls
}

sealed interface PollsListIntent {
    data class FilterSelected(val filter: PollFilter) : PollsListIntent
    data class PollTapped(val pollId: String) : PollsListIntent
    data object CreateTapped : PollsListIntent
}

sealed interface PollsListEvent {
    data class NavigateToDetail(val pollId: String) : PollsListEvent
    data object NavigateToCreate : PollsListEvent
}
