package com.anthooop.colision.feature.projecthub.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.projecthub.commissions.CommissionsListRoute
import com.anthooop.colision.feature.projecthub.members.MemberCommissionsRoute
import com.anthooop.colision.feature.projecthub.members.MembersListRoute
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
            MembersListRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCommissions = { memberId ->
                    navController.navigate(ProjectHubDestination.MemberCommissions(memberId))
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable<ProjectHubDestination.MemberCommissions> { backStackEntry ->
            val args = backStackEntry.toRoute<ProjectHubDestination.MemberCommissions>()
            MemberCommissionsRoute(
                memberId = args.memberId,
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
