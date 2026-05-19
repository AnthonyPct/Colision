package com.anthooop.colision.feature.projecthub.data

import com.anthooop.colision.core.database.dao.CommissionDao
import com.anthooop.colision.core.database.entity.CommissionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow

interface CommissionsRepository {
    fun observeByProject(projectId: String): Flow<List<CommissionEntity>>
    suspend fun refresh(projectId: String): Result<Unit>
    suspend fun create(projectId: String, name: String): Result<CommissionEntity>
    suspend fun rename(commissionId: String, name: String): Result<Unit>
    suspend fun delete(commissionId: String): Result<Unit>
}

class DefaultCommissionsRepository(
    private val supabase: SupabaseClient,
    private val commissionDao: CommissionDao,
) : CommissionsRepository {

    override fun observeByProject(projectId: String): Flow<List<CommissionEntity>> =
        commissionDao.observeByProject(projectId)

    override suspend fun refresh(projectId: String): Result<Unit> = runCatching {
        val dtos = supabase.from("commission")
            .select(Columns.ALL) {
                filter { eq("project_id", projectId) }
            }
            .decodeList<CommissionDto>()
        commissionDao.upsertAll(dtos.map { it.toEntity() })
    }

    override suspend fun create(projectId: String, name: String): Result<CommissionEntity> = runCatching {
        val dto = supabase.from("commission")
            .insert(CommissionInsertDto(projectId = projectId, name = name)) {
                select()
            }
            .decodeSingle<CommissionDto>()
        val entity = dto.toEntity()
        commissionDao.upsert(entity)
        entity
    }

    override suspend fun rename(commissionId: String, name: String): Result<Unit> = runCatching {
        supabase.from("commission").update(CommissionUpdateDto(name = name)) {
            filter { eq("id", commissionId) }
        }
        val existing = commissionDao.findById(commissionId)
        if (existing != null) {
            commissionDao.update(existing.copy(name = name))
        }
    }

    override suspend fun delete(commissionId: String): Result<Unit> = runCatching {
        supabase.from("commission").delete {
            filter { eq("id", commissionId) }
        }
        commissionDao.deleteById(commissionId)
    }
}
