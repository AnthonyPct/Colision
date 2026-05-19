package com.anthooop.colision.feature.projecthub.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface ProjectHubDestination {
    @Serializable
    data object Settings : ProjectHubDestination

    @Serializable
    data object Commissions : ProjectHubDestination

    @Serializable
    data object Members : ProjectHubDestination

    @Serializable
    data class MemberCommissions(val memberId: String) : ProjectHubDestination
}
