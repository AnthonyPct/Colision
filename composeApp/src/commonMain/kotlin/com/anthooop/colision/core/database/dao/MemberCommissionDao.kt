package com.anthooop.colision.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberCommissionDao {
    @Query("SELECT * FROM member_commission WHERE memberId = :memberId")
    fun observeByMember(memberId: String): Flow<List<MemberCommissionEntity>>

    @Query("SELECT * FROM member_commission WHERE commissionId = :commissionId")
    fun observeByCommission(commissionId: String): Flow<List<MemberCommissionEntity>>

    @Query(
        "SELECT mc.* FROM member_commission mc " +
            "INNER JOIN member m ON m.id = mc.memberId " +
            "WHERE m.projectId = :projectId",
    )
    fun observeForProject(projectId: String): Flow<List<MemberCommissionEntity>>

    @Upsert
    suspend fun upsert(link: MemberCommissionEntity)

    @Upsert
    suspend fun upsertAll(links: List<MemberCommissionEntity>)

    @Query("DELETE FROM member_commission WHERE memberId = :memberId AND commissionId = :commissionId")
    suspend fun delete(memberId: String, commissionId: String)

    @Query("DELETE FROM member_commission WHERE memberId = :memberId")
    suspend fun deleteAllForMember(memberId: String)
}
