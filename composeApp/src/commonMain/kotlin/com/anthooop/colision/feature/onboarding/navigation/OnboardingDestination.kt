package com.anthooop.colision.feature.onboarding.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface OnboardingDestination {
    @Serializable
    data object Welcome : OnboardingDestination

    @Serializable
    data object CreateProject : OnboardingDestination

    @Serializable
    data class CreateProjectCode(val projectId: String) : OnboardingDestination

    @Serializable
    data object JoinCode : OnboardingDestination

    @Serializable
    data class JoinConfirm(val projectId: String) : OnboardingDestination

    @Serializable
    data class JoinIdentity(val projectId: String) : OnboardingDestination

    @Serializable
    data class JoinCommissions(val projectId: String, val memberId: String) : OnboardingDestination

    @Serializable
    data class NotificationPermission(val projectId: String, val memberId: String) : OnboardingDestination
}
