package com.anthooop.colision.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.anthooop.colision.core.database.entity.PollCommissionEntity
import com.anthooop.colision.core.database.entity.PollEntity
import com.anthooop.colision.core.database.entity.PollOptionEntity
import com.anthooop.colision.core.database.entity.PollVoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PollDao {
    @Query("SELECT * FROM poll WHERE projectId = :projectId ORDER BY closesAt DESC")
    fun observeByProject(projectId: String): Flow<List<PollEntity>>

    @Query("SELECT * FROM poll WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<PollEntity?>

    @Query("SELECT * FROM poll_option WHERE pollId = :pollId ORDER BY position ASC")
    fun observeOptions(pollId: String): Flow<List<PollOptionEntity>>

    @Query(
        "SELECT o.* FROM poll_option o " +
            "INNER JOIN poll p ON p.id = o.pollId " +
            "WHERE p.projectId = :projectId ORDER BY o.position ASC",
    )
    fun observeOptionsForProject(projectId: String): Flow<List<PollOptionEntity>>

    @Query("SELECT * FROM poll_vote WHERE pollId = :pollId")
    fun observeVotes(pollId: String): Flow<List<PollVoteEntity>>

    @Query(
        "SELECT v.* FROM poll_vote v " +
            "INNER JOIN poll p ON p.id = v.pollId " +
            "WHERE p.projectId = :projectId",
    )
    fun observeVotesForProject(projectId: String): Flow<List<PollVoteEntity>>

    @Query("SELECT pc.commissionId FROM poll_commission pc WHERE pc.pollId = :pollId")
    fun observeCommissionIdsFor(pollId: String): Flow<List<String>>

    @Query(
        "SELECT pc.* FROM poll_commission pc " +
            "INNER JOIN poll p ON p.id = pc.pollId " +
            "WHERE p.projectId = :projectId",
    )
    fun observeCommissionLinksForProject(projectId: String): Flow<List<PollCommissionEntity>>

    @Upsert
    suspend fun upsertPolls(polls: List<PollEntity>)

    @Upsert
    suspend fun upsertOptions(options: List<PollOptionEntity>)

    @Upsert
    suspend fun upsertCommissions(links: List<PollCommissionEntity>)

    @Upsert
    suspend fun upsertVotes(votes: List<PollVoteEntity>)

    @Query("DELETE FROM poll_option WHERE pollId IN (:pollIds)")
    suspend fun deleteOptionsFor(pollIds: List<String>)

    @Query("DELETE FROM poll_commission WHERE pollId IN (:pollIds)")
    suspend fun deleteCommissionsFor(pollIds: List<String>)

    @Query("DELETE FROM poll_vote WHERE pollId IN (:pollIds)")
    suspend fun deleteVotesFor(pollIds: List<String>)

    @Query("DELETE FROM poll WHERE id IN (:pollIds)")
    suspend fun deletePollsByIds(pollIds: List<String>)

    @Query("SELECT id FROM poll WHERE projectId = :projectId")
    suspend fun idsForProject(projectId: String): List<String>

    private suspend fun deleteChildrenFor(pollIds: List<String>) {
        deleteOptionsFor(pollIds)
        deleteCommissionsFor(pollIds)
        deleteVotesFor(pollIds)
    }

    @Transaction
    suspend fun replaceForProject(
        projectId: String,
        polls: List<PollEntity>,
        options: List<PollOptionEntity>,
        commissions: List<PollCommissionEntity>,
        votes: List<PollVoteEntity>,
    ) {
        val keepIds = polls.map { it.id }.toSet()
        val existing = idsForProject(projectId)
        val toDelete = existing.filter { it !in keepIds }
        if (toDelete.isNotEmpty()) {
            deleteChildrenFor(toDelete)
            deletePollsByIds(toDelete)
        }
        upsertPolls(polls)
        if (polls.isNotEmpty()) {
            val ids = polls.map { it.id }
            deleteChildrenFor(ids)
            upsertOptions(options)
            upsertCommissions(commissions)
            upsertVotes(votes)
        }
    }
}
