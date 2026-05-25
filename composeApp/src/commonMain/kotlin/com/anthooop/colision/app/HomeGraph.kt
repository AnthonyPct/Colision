package com.anthooop.colision.app

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.agenda.navigation.AgendaDestination
import com.anthooop.colision.feature.agenda.navigation.agendaDestinations
import com.anthooop.colision.feature.meeting.navigation.meetingDestinations
import com.anthooop.colision.feature.projecthub.navigation.projectHubDestinations

fun NavGraphBuilder.homeGraph(
    navController: NavController,
    onProjectReleased: () -> Unit,
) {
    navigation<RootGraph.Home>(startDestination = AgendaDestination.Agenda) {
        agendaDestinations(navController)
        meetingDestinations(
            navController = navController,
            // Pop the whole meeting sub-flow (Create → Conflicts →
            // Suggestions / Create-anyway) back to the agenda, otherwise
            // the user lands on the previous step (e.g. CreateMeeting)
            // after a successful create. inclusive=false keeps Agenda
            // itself in the back stack.
            onMeetingCreated = {
                navController.popBackStack(
                    route = AgendaDestination.Agenda,
                    inclusive = false,
                )
            },
        )
        projectHubDestinations(navController, onProjectReleased)
    }
}
