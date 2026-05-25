package com.anthooop.colision.feature.meeting.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface MeetingDestination {
    @Serializable
    data object CreateMeeting : MeetingDestination
}
