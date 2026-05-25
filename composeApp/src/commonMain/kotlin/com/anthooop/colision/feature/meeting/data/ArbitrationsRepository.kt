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
}
