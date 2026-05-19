package com.anthooop.colision.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface RootGraph {
    @Serializable
    data object Onboarding : RootGraph

    @Serializable
    data object Home : RootGraph
}
