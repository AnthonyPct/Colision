package com.anthooop.colision.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.anthooop.colision.core.database.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM project ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM project WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProjectEntity?

    @Query("SELECT COUNT(*) FROM project")
    fun observeCount(): Flow<Int>

    // @Upsert (INSERT ... ON CONFLICT DO UPDATE) NOT @Insert(onConflict=REPLACE).
    // The latter compiles to INSERT OR REPLACE which deletes-then-inserts on
    // conflict — and ProjectEntity is the parent of CASCADE FK chains
    // (commission, member, meeting, *_commission). Re-pulling the project
    // from the server would wipe every child row via cascade before
    // re-inserting the same project id. Same rule applies to every other
    // entity DAO whose row is parent of an ON DELETE CASCADE FK below.
    @Upsert
    suspend fun upsert(project: ProjectEntity)

    @Query("DELETE FROM project WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM project")
    suspend fun clear()
}
