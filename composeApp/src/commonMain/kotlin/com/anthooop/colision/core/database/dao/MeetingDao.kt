package com.anthooop.colision.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.anthooop.colision.core.database.entity.MeetingCommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meeting WHERE projectId = :projectId ORDER BY startsAt ASC")
    fun observeByProject(projectId: String): Flow<List<MeetingEntity>>

    @Query(
        "SELECT m.* FROM meeting m " +
            "INNER JOIN meeting_commission mc ON mc.meetingId = m.id " +
            "WHERE mc.commissionId = :commissionId " +
            "ORDER BY m.startsAt ASC",
    )
    fun observeByCommission(commissionId: String): Flow<List<MeetingEntity>>

    @Query(
        "SELECT DISTINCT m.* FROM meeting m " +
            "INNER JOIN meeting_commission mc ON mc.meetingId = m.id " +
            "INNER JOIN member_commission mbc ON mbc.commissionId = mc.commissionId " +
            "WHERE mbc.memberId = :memberId " +
            "ORDER BY m.startsAt ASC",
    )
    fun observeForMember(memberId: String): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meeting WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): MeetingEntity?

    @Query("SELECT * FROM meeting WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<MeetingEntity?>

    @Query("SELECT mc.commissionId FROM meeting_commission mc WHERE mc.meetingId = :meetingId")
    fun observeCommissionIdsFor(meetingId: String): Flow<List<String>>

    @Query(
        "SELECT mc.* FROM meeting_commission mc " +
            "INNER JOIN meeting m ON m.id = mc.meetingId " +
            "WHERE m.projectId = :projectId",
    )
    fun observeLinksForProject(projectId: String): Flow<List<MeetingCommissionEntity>>

    @Upsert
    suspend fun upsertAll(meetings: List<MeetingEntity>)

    @Upsert
    suspend fun upsertLinks(links: List<MeetingCommissionEntity>)

    @Query("DELETE FROM meeting_commission WHERE meetingId IN (:meetingIds)")
    suspend fun deleteLinksFor(meetingIds: List<String>)

    @Query("DELETE FROM meeting WHERE id IN (:meetingIds)")
    suspend fun deleteByIds(meetingIds: List<String>)

    @Query("SELECT id FROM meeting WHERE projectId = :projectId")
    suspend fun idsForProject(projectId: String): List<String>

    @Query(
        "SELECT DISTINCT mb.id FROM member mb " +
            "INNER JOIN member_commission mbc ON mbc.memberId = mb.id " +
            "INNER JOIN commission c ON c.id = mbc.commissionId " +
            "INNER JOIN meeting_commission mtc ON mtc.commissionId = c.id " +
            "INNER JOIN meeting m ON m.id = mtc.meetingId " +
            "WHERE m.projectId = :projectId " +
            "AND m.startsAt < :endIso " +
            "AND m.endsAt > :startIso " +
            "AND mb.id IN (" +
            "  SELECT mbc2.memberId FROM member_commission mbc2 " +
            "  WHERE mbc2.commissionId IN (:commissionIds)" +
            ")",
    )
    suspend fun findLocalConflictMemberIds(
        projectId: String,
        commissionIds: List<String>,
        startIso: String,
        endIso: String,
    ): List<String>

    @Transaction
    suspend fun replaceForProject(
        projectId: String,
        meetings: List<MeetingEntity>,
        links: List<MeetingCommissionEntity>,
    ) {
        val keepIds = meetings.map { it.id }.toSet()
        val existing = idsForProject(projectId)
        val toDelete = existing.filter { it !in keepIds }
        if (toDelete.isNotEmpty()) {
            deleteLinksFor(toDelete)
            deleteByIds(toDelete)
        }
        upsertAll(meetings)
        if (meetings.isNotEmpty()) {
            deleteLinksFor(meetings.map { it.id })
            upsertLinks(links)
        }
    }
}
