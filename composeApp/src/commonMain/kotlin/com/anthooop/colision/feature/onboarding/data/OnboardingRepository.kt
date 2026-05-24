package com.anthooop.colision.feature.onboarding.data

import com.anthooop.colision.core.common.CurrentMemberProvider
import com.anthooop.colision.core.database.dao.ProjectDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Cross-sub-screen state for the onboarding feature. Onboarding is "done"
 * only when both halves of the local state mirror the server invariant:
 *
 * 1. The project row is in Room (`JoinCode` / `CreateProject` cache it).
 * 2. A `member` row owned by the current device exists in Room — i.e. the
 *    user has either created the project (server bootstrap inserts the
 *    creator) or claimed an identity via `JoinIdentity`.
 *
 * Without the second check the start-graph would flip to Home as soon as
 * `JoinCode` resolves the share code, skipping `JoinConfirm`, `JoinIdentity`
 * and `JoinCommissions`.
 */
interface OnboardingRepository {
    fun observeHasJoinedProject(): Flow<Boolean>
}

class DefaultOnboardingRepository(
    private val projectDao: ProjectDao,
    private val currentMemberProvider: CurrentMemberProvider,
) : OnboardingRepository {
    override fun observeHasJoinedProject(): Flow<Boolean> =
        combine(
            projectDao.observeCount(),
            currentMemberProvider.observe(),
        ) { projectCount, ownMember ->
            projectCount > 0 && ownMember != null
        }
}
