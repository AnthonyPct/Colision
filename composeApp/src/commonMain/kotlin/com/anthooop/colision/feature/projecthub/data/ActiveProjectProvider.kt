package com.anthooop.colision.feature.projecthub.data

import com.anthooop.colision.core.database.dao.ProjectDao
import com.anthooop.colision.core.database.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Bridge between the single-project assumption of the MVP UX and the data
 * model that nominally allows multiple. Returns the first project Antoine or
 * Sophie joined / created on this device — currently the only one.
 */
interface ActiveProjectProvider {
    fun observe(): Flow<ProjectEntity?>
    suspend fun current(): ProjectEntity?
}

class DefaultActiveProjectProvider(
    private val projectDao: ProjectDao,
) : ActiveProjectProvider {
    override fun observe(): Flow<ProjectEntity?> =
        projectDao.observeAll().map { it.firstOrNull() }

    override suspend fun current(): ProjectEntity? =
        projectDao.observeAll().first().firstOrNull()
}
