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
import com.anthooop.colision.feature.onboarding.joincommissions.JoinCommissionsRoute
import com.anthooop.colision.feature.onboarding.joinidentity.JoinIdentityRoute
import com.anthooop.colision.feature.onboarding.notificationperm.NotificationPermRoute
import com.anthooop.colision.feature.onboarding.projectcommissions.CreateProjectCommissionsRoute
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
                // J1: the creator sets up commissions before sharing the code, so
                // the project isn't empty when invitees pick their commissions.
                onNavigateToShareCode = { projectId ->
                    navController.navigate(OnboardingDestination.CreateProjectCommissions(projectId)) {
                        popUpTo(OnboardingDestination.CreateProject) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.CreateProjectCommissions> { backStackEntry ->
            val args = backStackEntry.toRoute<OnboardingDestination.CreateProjectCommissions>()
            CreateProjectCommissionsRoute(
                projectId = args.projectId,
                onNavigateToShareCode = { projectId ->
                    navController.navigate(OnboardingDestination.CreateProjectCode(projectId))
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.CreateProjectCode> { backStackEntry ->
            val args = backStackEntry.toRoute<OnboardingDestination.CreateProjectCode>()
            ProjectShareCodeRoute(
                projectId = args.projectId,
                // After sharing the code, the creator still goes through the
                // notification-permission step (PRD §"permissions appareil"),
                // like any other member, before landing on Home.
                onNavigateToHome = {
                    navController.navigate(OnboardingDestination.CreatorNotificationPermission)
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.CreatorNotificationPermission> {
            NotificationPermRoute(
                onNavigateToHome = {
                    navController.navigate(RootGraph.Home) {
                        popUpTo(RootGraph.Onboarding) { inclusive = true }
                    }
                },
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
        composable<OnboardingDestination.JoinIdentity> { backStackEntry ->
            val args = backStackEntry.toRoute<OnboardingDestination.JoinIdentity>()
            JoinIdentityRoute(
                projectId = args.projectId,
                onNavigateToCommissions = { projectId, memberId ->
                    navController.navigate(
                        OnboardingDestination.JoinCommissions(projectId, memberId),
                    )
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.JoinCommissions> { backStackEntry ->
            val args = backStackEntry.toRoute<OnboardingDestination.JoinCommissions>()
            JoinCommissionsRoute(
                projectId = args.projectId,
                memberId = args.memberId,
                onNavigateToNotificationPermission = { projectId, memberId ->
                    navController.navigate(
                        OnboardingDestination.NotificationPermission(projectId, memberId),
                    )
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<OnboardingDestination.NotificationPermission> {
            NotificationPermRoute(
                onNavigateToHome = {
                    navController.navigate(RootGraph.Home) {
                        popUpTo(RootGraph.Onboarding) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PlaceholderRoute(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}
