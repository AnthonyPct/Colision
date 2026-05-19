package com.anthooop.colision.feature.onboarding.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.onboarding.joincode.JoinCodeRoute
import com.anthooop.colision.feature.onboarding.joinconfirm.JoinConfirmRoute
import com.anthooop.colision.feature.onboarding.projectcreate.CreateProjectRoute
import com.anthooop.colision.feature.onboarding.projectsharecode.ProjectShareCodeRoute
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
            CreateProjectRoute(
                onNavigateToShareCode = { projectId ->
                    navController.navigate(OnboardingDestination.CreateProjectCode(projectId)) {
                        popUpTo(OnboardingDestination.CreateProject) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.CreateProjectCode> { backStackEntry ->
            val args = backStackEntry.toRoute<OnboardingDestination.CreateProjectCode>()
            ProjectShareCodeRoute(
                projectId = args.projectId,
                onNavigateToHome = {
                    navController.navigate(RootGraph.Home) {
                        popUpTo(RootGraph.Onboarding) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.JoinCode> {
            JoinCodeRoute(
                onNavigateToConfirm = { projectId ->
                    navController.navigate(OnboardingDestination.JoinConfirm(projectId))
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.JoinConfirm> { backStackEntry ->
            val args = backStackEntry.toRoute<OnboardingDestination.JoinConfirm>()
            JoinConfirmRoute(
                projectId = args.projectId,
                onNavigateToIdentity = { projectId ->
                    navController.navigate(OnboardingDestination.JoinIdentity(projectId))
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
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

@Composable
private fun PlaceholderRoute(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}
