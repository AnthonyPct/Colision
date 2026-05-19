package com.anthooop.colision.core.common

import com.anthooop.colision.feature.agenda.data.MeetingsRepository
import com.anthooop.colision.feature.projecthub.data.ActiveProjectProvider
import com.anthooop.colision.feature.projecthub.data.CommissionsRepository
import com.anthooop.colision.feature.projecthub.data.MembersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Owns the pull-on-foreground / pull-on-reconnect cycle that keeps the local
 * Room cache in sync with Supabase. Architecture decision 4: no Realtime —
 * push notifications (Epic 5) provide the only live signal.
 */
@OptIn(ExperimentalTime::class)
interface ProjectSyncManager {
    val isOnline: StateFlow<Boolean>
    val lastSyncAt: StateFlow<Instant?>

    /** Forces a refresh of the current project. Safe to call on any thread. */
    fun refreshNow()
}

@OptIn(ExperimentalTime::class)
class DefaultProjectSyncManager(
    private val connectivity: ConnectivityObserver,
    private val activeProjectProvider: ActiveProjectProvider,
    private val commissionsRepository: CommissionsRepository,
    private val membersRepository: MembersRepository,
    private val meetingsRepository: MeetingsRepository,
    private val logger: Logger,
) : ProjectSyncManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _lastSyncAt = MutableStateFlow<Instant?>(null)

    override val isOnline: StateFlow<Boolean> = connectivity.isOnline
    override val lastSyncAt: StateFlow<Instant?> = _lastSyncAt.asStateFlow()

    init {
        // Re-pull every time we transition offline → online. StateFlow already
        // deduplicates identical values, so we only need to skip the initial
        // emission.
        scope.launch {
            connectivity.isOnline
                .drop(1)
                .collect { online -> if (online) syncInternal() }
        }
    }

    override fun refreshNow() {
        scope.launch { syncInternal() }
    }

    private suspend fun syncInternal() {
        val projectId = activeProjectProvider.current()?.id ?: return
        if (!connectivity.isOnline.value) return
        val results = listOf(
            commissionsRepository.refresh(projectId),
            membersRepository.refresh(projectId),
            meetingsRepository.refresh(projectId),
        )
        val failure = results.firstOrNull { it.isFailure }?.exceptionOrNull()
        if (failure != null) {
            logger.warn("ProjectSyncManager", "sync failed: ${failure.message}")
        } else {
            _lastSyncAt.value = Clock.System.now()
        }
    }
}
