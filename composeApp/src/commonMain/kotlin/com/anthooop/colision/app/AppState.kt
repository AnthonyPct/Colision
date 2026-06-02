package com.anthooop.colision.app

import com.anthooop.colision.core.navigation.RootGraph

sealed interface AppStartState {
    data object Loading : AppStartState
    data class Ready(val startGraph: RootGraph) : AppStartState
}
