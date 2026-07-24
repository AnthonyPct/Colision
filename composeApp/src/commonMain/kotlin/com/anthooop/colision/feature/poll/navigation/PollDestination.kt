package com.anthooop.colision.feature.poll.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface PollDestination {
    @Serializable
    data object PollsList : PollDestination

    @Serializable
    data class PollDetail(val pollId: String) : PollDestination

    @Serializable
    data object CreatePoll : PollDestination
}
