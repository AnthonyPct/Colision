package com.anthooop.colision.app

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.agenda.navigation.AgendaDestination
import com.anthooop.colision.feature.agenda.navigation.agendaDestinations
import com.anthooop.colision.feature.projecthub.navigation.projectHubDestinations

fun NavGraphBuilder.homeGraph(
    navController: NavController,
    onProjectReleased: () -> Unit,
) {
    navigation<RootGraph.Home>(startDestination = AgendaDestination.Agenda) {
        agendaDestinations(navController)
        projectHubDestinations(navController, onProjectReleased)
    }
}
