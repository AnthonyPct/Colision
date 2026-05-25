package com.anthooop.colision.feature.meeting.data

import com.anthooop.colision.core.database.dao.ArbitrationDao
import com.anthooop.colision.core.database.entity.ArbitrationEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class ArbitrationDto(
    val id: String,
    @SerialName("member_id") val memberId: String,
    @SerialName("meeting_id") val meetingId: String,
    @SerialName("conflicting_meeting_id") val conflictingMeetingId: String,
    @SerialName("decided_at") val decidedAt: String,
)

@Serializable
private data class ArbitrationInsertDto(
    @SerialName("member_id") val memberId: String,
    @SerialName("meeting_id") val meetingId: String,
    @SerialName("conflicting_meeting_id") val conflictingMeetingId: String,
)

private fun ArbitrationDto.toEntity() = ArbitrationEntity(
    id = id,
    memberId = memberId,
    meetingId = meetingId,
    conflictingMeetingId = conflictingMeetingId,
    decidedAt = decidedAt,
)

interface ArbitrationsRepository {
    fun observeSkippingMeeting(meetingId: String): Flow<List<ArbitrationEntity>>
    fun observeChoosingMeeting(meetingId: String): Flow<List<ArbitrationEntity>>
    suspend fun refresh(projectId: String): Result<Unit>

    /**
     * Records that [memberId] chose [chosenMeetingId] over [skippedMeetingId].
     * Any earlier decision on the same pair (in either direction) is removed
     * first so the unique `(member, meeting, conflicting_meeting)` constraint
     * does not reject the new row when the member changes their mind.
     */
    suspend fun choose(
        memberId: String,
        skippedMeetingId: String,
        chosenMeetingId: String,
    ): Result<ArbitrationEntity>

    /** Removes any arbitration row for the (A, B) pair on [memberId]. */
    suspend fun postpone(
        memberId: String,
        meetingAId: String,
        meetingBId: String,
    ): Result<Unit>
}

class DefaultArbitrationsRepository(
    private val supabase: SupabaseClient,
    private val arbitrationDao: ArbitrationDao,
) : ArbitrationsRepository {

    override fun observeSkippingMeeting(meetingId: String): Flow<List<ArbitrationEntity>> =
        arbitrationDao.observeSkippingMeeting(meetingId)

    override fun observeChoosingMeeting(meetingId: String): Flow<List<ArbitrationEntity>> =
        arbitrationDao.observeChoosingMeeting(meetingId)

    override suspend fun refresh(projectId: String): Result<Unit> = runCatching {
        // arbitration rows are scoped per (member, meeting) — we fetch the
        // ones whose member belongs to the active project via an inner join
        // expressed through PostgREST embedding. RLS narrows server-side to
        // the caller's project rows.
        val all = supabase.from("arbitration")
            .select(Columns.ALL)
            .decodeList<ArbitrationDto>()
        arbitrationDao.upsertAll(all.map { it.toEntity() })
        arbitrationDao.deleteOthers(all.map { it.id })
    }

    override suspend fun choose(
        memberId: String,
        skippedMeetingId: String,
        chosenMeetingId: String,
    ): Result<ArbitrationEntity> = runCatching {
        clearPair(memberId, skippedMeetingId, chosenMeetingId)
        val inserted = supabase.from("arbitration")
            .insert(
                ArbitrationInsertDto(
                    memberId = memberId,
                    meetingId = skippedMeetingId,
                    conflictingMeetingId = chosenMeetingId,
                ),
            ) {
                select()
            }
            .decodeSingle<ArbitrationDto>()
        val entity = inserted.toEntity()
        arbitrationDao.upsert(entity)
        entity
    }

    override suspend fun postpone(
        memberId: String,
        meetingAId: String,
        meetingBId: String,
    ): Result<Unit> = runCatching {
        clearPair(memberId, meetingAId, meetingBId)
    }

    /**
     * Removes any persisted arbitration row for the (A, B) pair on this
     * member, in either ordering of `meeting_id` / `conflicting_meeting_id`.
     * Filtering on `meeting_id IN (A, B) AND conflicting_meeting_id IN (A, B)`
     * matches both orderings without resorting to a postgrest `or` block.
     */
    private suspend fun clearPair(
        memberId: String,
        meetingAId: String,
        meetingBId: String,
    ) {
        val pair = listOf(meetingAId, meetingBId)
        supabase.from("arbitration").delete {
            filter {
                eq("member_id", memberId)
                isIn("meeting_id", pair)
                isIn("conflicting_meeting_id", pair)
            }
        }
        arbitrationDao.deletePair(memberId, meetingAId, meetingBId)
    }
}
