package com.anthooop.colision.feature.meeting.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface MeetingDestination {
    @Serializable
    data object CreateMeeting : MeetingDestination

    @Serializable
    data class EditMeeting(val meetingId: String) : MeetingDestination

    @Serializable
    data object Conflicts : MeetingDestination

    @Serializable
    data object Suggestions : MeetingDestination
}
