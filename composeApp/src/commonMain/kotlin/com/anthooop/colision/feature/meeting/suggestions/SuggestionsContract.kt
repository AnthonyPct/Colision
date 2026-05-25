package com.anthooop.colision.feature.meeting.suggestions

import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.feature.meeting.data.SuggestedSlot

data class SuggestionsState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val slots: List<SuggestedSlot> = emptyList(),
    val selectedIndex: Int? = null,
    val error: AppError? = null,
) {
    val canSubmit: Boolean get() = !isSubmitting && selectedIndex != null
}

sealed interface SuggestionsIntent {
    data object BackTapped : SuggestionsIntent
    data class SlotSelected(val index: Int) : SuggestionsIntent
    data object SubmitTapped : SuggestionsIntent
    data object ErrorDismissed : SuggestionsIntent
}

sealed interface SuggestionsEvent {
    data object NavigateBack : SuggestionsEvent
    data class MeetingCreated(val meetingId: String) : SuggestionsEvent
}
