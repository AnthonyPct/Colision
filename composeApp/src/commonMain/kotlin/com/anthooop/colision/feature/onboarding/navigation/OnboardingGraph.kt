package com.anthooop.colision.feature.onboarding.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.onboarding.welcome.WelcomeRoute

fun NavGraphBuilder.onboardingGraph(navController: NavController) {
    navigation<RootGraph.Onboarding>(startDestination = OnboardingDestination.Welcome) {
        composable<OnboardingDestination.Welcome> {
            WelcomeRoute(
                onNavigateToCreateProject = {
                    navController.navigate(OnboardingDestination.CreateProject)
                },
                onNavigateToJoinCode = {
                    navController.navigate(OnboardingDestination.JoinCode)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.CreateProject> {
            // Implemented in Story 2.2.
            PlaceholderRoute(label = "Créer un projet")
        }
        composable<OnboardingDestination.CreateProjectCode> {
            PlaceholderRoute(label = "Code de partage")
        }
        composable<OnboardingDestination.JoinCode> {
            // Implemented in Story 2.5.
            PlaceholderRoute(label = "Rejoindre un projet")
        }
        composable<OnboardingDestination.JoinConfirm> {
            PlaceholderRoute(label = "Confirmation du projet")
        }
        composable<OnboardingDestination.JoinIdentity> {
            PlaceholderRoute(label = "Qui es-tu ?")
        }
        composable<OnboardingDestination.JoinCommissions> {
            PlaceholderRoute(label = "Tes commissions")
        }
        composable<OnboardingDestination.NotificationPermission> {
            PlaceholderRoute(label = "Notifications")
        }
    }
}

@androidx.compose.runtime.Composable
private fun PlaceholderRoute(label: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}
