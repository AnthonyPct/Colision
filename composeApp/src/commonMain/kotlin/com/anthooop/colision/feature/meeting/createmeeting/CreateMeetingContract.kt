package com.anthooop.colision.feature.meeting.createmeeting

import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.database.entity.CommissionEntity

enum class DurationOption(val minutes: Int) {
    Min60(60),
    Min90(90),
    Min120(120),
}

data class CreateMeetingState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val title: String = "",
    val date: String = "",
    val time: String = "20:00",
    val duration: DurationOption = DurationOption.Min90,
    val selectedCommissionIds: Set<String> = emptySet(),
    val commissions: List<CommissionEntity> = emptyList(),
    val availableDates: List<String> = emptyList(),
    val isOnline: Boolean = true,
    val potentialConflictCount: Int = 0,
    val error: AppError? = null,
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            !isLoading &&
            date.isNotBlank() &&
            isValidTime(time) &&
            selectedCommissionIds.isNotEmpty() &&
            isOnline
}

internal fun isValidTime(time: String): Boolean {
    if (time.length != 5 || time[2] != ':') return false
    val hh = time.substring(0, 2).toIntOrNull() ?: return false
    val mm = time.substring(3, 5).toIntOrNull() ?: return false
    return hh in 0..23 && mm in 0..59
}

sealed interface CreateMeetingIntent {
    data class TitleChanged(val value: String) : CreateMeetingIntent
    data class DateSelected(val iso: String) : CreateMeetingIntent
    data class TimeChanged(val value: String) : CreateMeetingIntent
    data class DurationSelected(val option: DurationOption) : CreateMeetingIntent
    data class CommissionToggled(val commissionId: String) : CreateMeetingIntent
    data object SubmitTapped : CreateMeetingIntent
    data object BackTapped : CreateMeetingIntent
    data object ErrorDismissed : CreateMeetingIntent
}

sealed interface CreateMeetingEvent {
    data object NavigateBack : CreateMeetingEvent
    data class MeetingCreated(val meetingId: String) : CreateMeetingEvent
    data object NavigateToConflicts : CreateMeetingEvent
}
