package com.anthooop.colision.feature.meeting.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingRoute

fun NavGraphBuilder.meetingDestinations(
    navController: NavController,
    onMeetingCreated: () -> Unit,
) {
    composable<MeetingDestination.CreateMeeting> {
        CreateMeetingRoute(
            onNavigateBack = { navController.popBackStack() },
            onMeetingCreated = onMeetingCreated,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
