package com.anthooop.colision.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.anthooop.colision.core.database.entity.CommissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommissionDao {
    @Query("SELECT * FROM commission WHERE projectId = :projectId ORDER BY name ASC")
    fun observeByProject(projectId: String): Flow<List<CommissionEntity>>

    @Query("SELECT * FROM commission WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CommissionEntity?

    @Upsert
    suspend fun upsert(commission: CommissionEntity)

    @Upsert
    suspend fun upsertAll(commissions: List<CommissionEntity>)

    @Update
    suspend fun update(commission: CommissionEntity)

    @Query("DELETE FROM commission WHERE id = :id")
    suspend fun deleteById(id: String)
}
