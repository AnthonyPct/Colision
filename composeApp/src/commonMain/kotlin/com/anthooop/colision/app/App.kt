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
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.onboarding.navigation.onboardingGraph
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
        // Home graph is implemented by Epic 3.
        composable<RootGraph.Home> {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Text(
                    text = "Agenda — Epic 3",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
