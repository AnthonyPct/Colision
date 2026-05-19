package com.anthooop.colision.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anthooop.colision.core.database.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM member WHERE projectId = :projectId ORDER BY displayName ASC")
    fun observeByProject(projectId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM member WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): MemberEntity?

    @Query("SELECT * FROM member WHERE projectId = :projectId AND deviceId = :deviceId LIMIT 1")
    suspend fun findOwnByProject(projectId: String, deviceId: String): MemberEntity?

    @Query("SELECT * FROM member WHERE deviceId = :deviceId LIMIT 1")
    fun observeOwnMember(deviceId: String): Flow<MemberEntity?>

    @Query(
        "SELECT DISTINCT m.* FROM member m " +
            "INNER JOIN member_commission mc ON mc.memberId = m.id " +
            "INNER JOIN meeting_commission mtc ON mtc.commissionId = mc.commissionId " +
            "WHERE mtc.meetingId = :meetingId " +
            "ORDER BY m.displayName ASC",
    )
    fun observeAttendingMeeting(meetingId: String): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(members: List<MemberEntity>)

    @Update
    suspend fun update(member: MemberEntity)

    @Query("DELETE FROM member WHERE id = :id")
    suspend fun deleteById(id: String)
}
