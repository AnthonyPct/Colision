package com.anthooop.colision.feature.onboarding.data

import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.database.dao.ProjectDao
import com.anthooop.colision.core.database.entity.ProjectEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface ProjectsRepository {
    fun observeProject(projectId: String): Flow<ProjectEntity?>
    suspend fun getProject(projectId: String): ProjectEntity?
    suspend fun createProject(name: String): Result<ProjectEntity>
    suspend fun resolveByCode(code: String): Result<ProjectEntity>
    suspend fun deleteLocalProject(projectId: String)
}

class DefaultProjectsRepository(
    private val supabase: SupabaseClient,
    private val projectDao: ProjectDao,
) : ProjectsRepository {

    override fun observeProject(projectId: String): Flow<ProjectEntity?> =
        kotlinx.coroutines.flow.flow {
            emit(projectDao.findById(projectId))
        }

    override suspend fun getProject(projectId: String): ProjectEntity? =
        projectDao.findById(projectId)

    override suspend fun createProject(name: String): Result<ProjectEntity> = runCatching {
        val dto = supabase.postgrest.rpc(
            function = "create_project",
            parameters = buildJsonObject { put("p_name", name) },
        ).decodeAs<ProjectDto>()
        val entity = dto.toEntity()
        projectDao.upsert(entity)
        entity
    }.recoverCatching { throw it.toAppErrorThrowable() }

    override suspend fun resolveByCode(code: String): Result<ProjectEntity> = runCatching {
        val dto = supabase.postgrest.rpc(
            function = "try_resolve_code",
            parameters = buildJsonObject { put("p_code", code) },
        ).decodeAs<ProjectDto>()
        val entity = dto.toEntity()
        projectDao.upsert(entity)
        entity
    }.recoverCatching { throw it.toAppErrorThrowable(isResolve = true) }

    override suspend fun deleteLocalProject(projectId: String) {
        projectDao.deleteById(projectId)
    }
}

private fun Throwable.toAppErrorThrowable(isResolve: Boolean = false): Throwable {
    val error: AppError = when (this) {
        is RestException -> {
            // P0002 → code not found (try_resolve_code).
            val raw = (message ?: "") + " " + (statusCode.toString())
            if (isResolve && (raw.contains("P0002") || raw.contains("code not found"))) {
                AppError.ProjectCodeInvalid
            } else {
                AppError.Unknown(this)
            }
        }
        is HttpRequestTimeoutException, is IOException -> AppError.NetworkUnavailable
        else -> AppError.Unknown(this)
    }
    return com.anthooop.colision.core.common.AppErrorThrowable(error)
}
