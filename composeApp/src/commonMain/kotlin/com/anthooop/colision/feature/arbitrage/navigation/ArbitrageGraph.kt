package com.anthooop.colision.feature.arbitrage.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.anthooop.colision.feature.arbitrage.arbitration.ArbitrationRoute

/**
 * Deep-link target shared with the push pipeline (FR32). The
 * `dispatch_conflict_push` edge function emits payloads carrying
 * `colision://arbitration/{conflictMeetingId}` — tapping the push opens the
 * app and routes here directly, with [ArbitrageDestination.Arbitration]
 * decoded from the path segment.
 */
const val ARBITRATION_DEEP_LINK_URI: String = "colision://arbitration/{conflictMeetingId}"

fun NavGraphBuilder.arbitrageDestinations(navController: NavController) {
    composable<ArbitrageDestination.Arbitration>(
        deepLinks = listOf(navDeepLink { uriPattern = ARBITRATION_DEEP_LINK_URI }),
    ) {
        ArbitrationRoute(
            onNavigateBack = { navController.popBackStack() },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
