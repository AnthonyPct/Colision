package com.anthooop.colision.feature.agenda.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anthooop.colision.feature.agenda.agenda.AgendaRoute
import com.anthooop.colision.feature.agenda.commissiondetail.CommissionDetailRoute
import com.anthooop.colision.feature.agenda.meetingdetail.MeetingDetailRoute
import com.anthooop.colision.feature.meeting.navigation.MeetingDestination

fun NavGraphBuilder.agendaDestinations(navController: NavController) {
    composable<AgendaDestination.Agenda> {
        AgendaRoute(
            onNavigateToMeetingDetail = { meetingId ->
                navController.navigate(AgendaDestination.MeetingDetail(meetingId))
            },
            onNavigateToCreateMeeting = {
                navController.navigate(MeetingDestination.CreateMeeting)
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
    composable<AgendaDestination.MeetingDetail> {
        MeetingDetailRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEdit = { meetingId ->
                navController.navigate(MeetingDestination.EditMeeting(meetingId))
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
    composable<AgendaDestination.CommissionDetail> {
        CommissionDetailRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToMeetingDetail = { meetingId ->
                navController.navigate(AgendaDestination.MeetingDetail(meetingId))
            },
            onNavigateToCreateMeeting = {
                navController.navigate(MeetingDestination.CreateMeeting)
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
