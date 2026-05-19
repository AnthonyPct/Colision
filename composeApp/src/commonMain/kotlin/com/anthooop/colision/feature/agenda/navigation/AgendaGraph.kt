package com.anthooop.colision.feature.agenda.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anthooop.colision.feature.agenda.agenda.AgendaRoute
import com.anthooop.colision.feature.agenda.meetingdetail.MeetingDetailRoute

fun NavGraphBuilder.agendaDestinations(navController: NavController) {
    composable<AgendaDestination.Agenda> {
        AgendaRoute(
            onNavigateToMeetingDetail = { meetingId ->
                navController.navigate(AgendaDestination.MeetingDetail(meetingId))
            },
            onNavigateToCreateMeeting = {
                // Epic 4 — placeholder until "Nouvelle réunion" flow ships.
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
    composable<AgendaDestination.MeetingDetail> {
        MeetingDetailRoute(
            onNavigateBack = { navController.popBackStack() },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
