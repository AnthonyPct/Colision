package com.anthooop.colision.feature.projecthub.navigation

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
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.projecthub.commissions.CommissionsListRoute
import com.anthooop.colision.feature.projecthub.settings.ProjectSettingsRoute

fun NavGraphBuilder.projectHubGraph(
    navController: NavController,
    onProjectReleased: () -> Unit,
) {
    navigation<RootGraph.Home>(startDestination = ProjectHubDestination.Settings) {
        composable<ProjectHubDestination.Settings> {
            ProjectSettingsRoute(
                onNavigateToCommissions = {
                    navController.navigate(ProjectHubDestination.Commissions)
                },
                onNavigateToMembers = {
                    navController.navigate(ProjectHubDestination.Members)
                },
                onProjectReleased = onProjectReleased,
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<ProjectHubDestination.Commissions> {
            CommissionsListRoute(
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<ProjectHubDestination.Members> {
            PlaceholderRoute("Membres")
        }
        composable<ProjectHubDestination.MemberCommissions> {
            PlaceholderRoute("Commissions du membre")
        }
    }
}

@Composable
private fun PlaceholderRoute(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}
