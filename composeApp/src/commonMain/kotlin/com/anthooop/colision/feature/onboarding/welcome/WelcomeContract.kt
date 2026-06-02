package com.anthooop.colision.feature.onboarding.welcome

data class WelcomeState(
    val isLoading: Boolean = false,
)

sealed interface WelcomeIntent {
    data object CreateProjectTapped : WelcomeIntent
    data object JoinProjectTapped : WelcomeIntent
}

sealed interface WelcomeEvent {
    data object NavigateToCreateProject : WelcomeEvent
    data object NavigateToJoinCode : WelcomeEvent
}
