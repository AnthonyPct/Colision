package com.anthooop.colision.feature.meeting.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anthooop.colision.feature.meeting.conflicts.ConflictsRoute
import com.anthooop.colision.feature.meeting.createmeeting.CreateMeetingRoute
import com.anthooop.colision.feature.meeting.suggestions.SuggestionsRoute

fun NavGraphBuilder.meetingDestinations(
    navController: NavController,
    onMeetingCreated: () -> Unit,
) {
    composable<MeetingDestination.CreateMeeting> {
        CreateMeetingRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToConflicts = { navController.navigate(MeetingDestination.Conflicts) },
            onMeetingCreated = onMeetingCreated,
            modifier = Modifier.fillMaxSize(),
        )
    }
    composable<MeetingDestination.Conflicts> {
        ConflictsRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSuggestions = { navController.navigate(MeetingDestination.Suggestions) },
            onMeetingCreated = onMeetingCreated,
            modifier = Modifier.fillMaxSize(),
        )
    }
    composable<MeetingDestination.Suggestions> {
        SuggestionsRoute(
            onNavigateBack = { navController.popBackStack() },
            onMeetingCreated = onMeetingCreated,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
