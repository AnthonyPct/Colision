package com.anthooop.colision.feature.poll.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anthooop.colision.feature.poll.createpoll.CreatePollRoute
import com.anthooop.colision.feature.poll.polldetail.PollDetailRoute
import com.anthooop.colision.feature.poll.pollslist.PollsListRoute

fun NavGraphBuilder.pollDestinations(navController: NavController) {
    composable<PollDestination.PollsList> {
        PollsListRoute(
            onNavigateToDetail = { pollId ->
                navController.navigate(PollDestination.PollDetail(pollId))
            },
            onNavigateToCreate = {
                navController.navigate(PollDestination.CreatePoll)
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
    composable<PollDestination.PollDetail> {
        PollDetailRoute(
            onNavigateBack = { navController.popBackStack() },
            modifier = Modifier.fillMaxSize(),
        )
    }
    composable<PollDestination.CreatePoll> {
        CreatePollRoute(
            onNavigateBack = { navController.popBackStack() },
            onPollCreated = { navController.popBackStack() },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
