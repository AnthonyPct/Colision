package com.anthooop.colision.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberCommissionDao {
    @Query("SELECT * FROM member_commission WHERE memberId = :memberId")
    fun observeByMember(memberId: String): Flow<List<MemberCommissionEntity>>

    @Query("SELECT * FROM member_commission WHERE commissionId = :commissionId")
    fun observeByCommission(commissionId: String): Flow<List<MemberCommissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(link: MemberCommissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(links: List<MemberCommissionEntity>)

    @Query("DELETE FROM member_commission WHERE memberId = :memberId AND commissionId = :commissionId")
    suspend fun delete(memberId: String, commissionId: String)

    @Query("DELETE FROM member_commission WHERE memberId = :memberId")
    suspend fun deleteAllForMember(memberId: String)
}
