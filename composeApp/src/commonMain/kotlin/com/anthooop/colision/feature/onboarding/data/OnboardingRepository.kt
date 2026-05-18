package com.anthooop.colision.feature.onboarding.data

import com.anthooop.colision.core.database.dao.ProjectDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Cross-sub-screen state for the onboarding feature. For Story 2.1, the only
 * concern is whether the device has any locally-cached project — this drives
 * the start-graph decision in [com.anthooop.colision.app.AppViewModel].
 */
interface OnboardingRepository {
    fun observeHasJoinedProject(): Flow<Boolean>
}

class DefaultOnboardingRepository(
    private val projectDao: ProjectDao,
) : OnboardingRepository {
    override fun observeHasJoinedProject(): Flow<Boolean> =
        projectDao.observeCount().map { it > 0 }
}
