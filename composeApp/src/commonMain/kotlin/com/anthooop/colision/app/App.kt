package com.anthooop.colision.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anthooop.colision.core.navigation.RootGraph
import com.anthooop.colision.feature.agenda.navigation.AgendaDestination
import com.anthooop.colision.feature.onboarding.navigation.onboardingGraph
import com.anthooop.colision.feature.projecthub.navigation.ProjectHubDestination
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

private data class HomeTab(
    val label: String,
    val route: Any,
    val routeClass: kotlin.reflect.KClass<*>,
)

private val HOME_TABS = listOf(
    HomeTab("Agenda", AgendaDestination.Agenda, AgendaDestination.Agenda::class),
    HomeTab("Commissions", ProjectHubDestination.Commissions, ProjectHubDestination.Commissions::class),
    HomeTab("Membres", ProjectHubDestination.Members, ProjectHubDestination.Members::class),
    HomeTab("Projet", ProjectHubDestination.Settings, ProjectHubDestination.Settings::class),
)

private fun NavDestination?.matchesTab(tab: HomeTab): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

private fun NavDestination?.isTopLevelHomeTab(): Boolean =
    HOME_TABS.any { matchesTab(it) }

@Composable
private fun ColisionNavHost(startGraph: RootGraph) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination.isTopLevelHomeTab()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                HomeBottomBar(navController = navController, current = currentDestination)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startGraph,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            onboardingGraph(navController)
            homeGraph(
                navController = navController,
                onProjectReleased = {
                    navController.navigate(RootGraph.Onboarding) {
                        popUpTo(RootGraph.Home) { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeBottomBar(navController: NavController, current: NavDestination?) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        HOME_TABS.forEach { tab ->
            val selected = current.matchesTab(tab)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Text(
                        text = tab.label.first().toString(),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                label = { Text(tab.label) },
            )
        }
    }
}
