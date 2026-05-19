package com.anthooop.colision.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.onboarding.navigation.onboardingGraph
import com.anthooop.colision.feature.projecthub.navigation.projectHubGraph
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    ColisionTheme {
        val appViewModel: AppViewModel = koinViewModel()
        val startState by appViewModel.startState.collectAsStateWithLifecycle()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (val s = startState) {
                AppStartState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is AppStartState.Ready -> {
                    ColisionNavHost(startGraph = s.startGraph)
                }
            }
        }
    }
}

@Composable
private fun ColisionNavHost(startGraph: RootGraph) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startGraph,
        modifier = Modifier.fillMaxSize(),
    ) {
        onboardingGraph(navController)
        projectHubGraph(
            navController = navController,
            onProjectReleased = {
                // Pop back to the onboarding graph after the user leaves /
                // deletes the active project. The AppViewModel's start-graph
                // flow will catch up on the next emission.
                navController.navigate(RootGraph.Onboarding) {
                    popUpTo(RootGraph.Home) { inclusive = true }
                }
            },
        )
    }
}
