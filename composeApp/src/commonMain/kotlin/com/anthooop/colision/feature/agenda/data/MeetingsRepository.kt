package com.anthooop.colision.feature.agenda.data

import com.anthooop.colision.core.database.dao.MeetingDao
import com.anthooop.colision.core.database.entity.MeetingCommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class CreateMeetingInput(
    val projectId: String,
    val title: String?,
    val startsAt: String,
    val endsAt: String,
    val commissionIds: List<String>,
    val createdByMemberId: String?,
)

data class UpdateMeetingInput(
    val meetingId: String,
    val title: String?,
    val startsAt: String,
    val endsAt: String,
    val commissionIds: List<String>,
)

interface MeetingsRepository {
    fun observeForMember(memberId: String): Flow<List<MeetingEntity>>
    fun observeByCommission(commissionId: String): Flow<List<MeetingEntity>>
    fun observeById(meetingId: String): Flow<MeetingEntity?>
    fun observeCommissionIds(meetingId: String): Flow<List<String>>
    fun observeLinksForProject(projectId: String): Flow<List<MeetingCommissionEntity>>
    suspend fun refresh(projectId: String): Result<Unit>
    suspend fun create(input: CreateMeetingInput): Result<MeetingEntity>
    suspend fun update(input: UpdateMeetingInput): Result<MeetingEntity>
    suspend fun delete(meetingId: String): Result<Unit>
}

class DefaultMeetingsRepository(
    private val supabase: SupabaseClient,
    private val meetingDao: MeetingDao,
) : MeetingsRepository {

    override fun observeForMember(memberId: String): Flow<List<MeetingEntity>> =
        meetingDao.observeForMember(memberId)

    override fun observeByCommission(commissionId: String): Flow<List<MeetingEntity>> =
        meetingDao.observeByCommission(commissionId)

    override fun observeById(meetingId: String): Flow<MeetingEntity?> =
        meetingDao.observeById(meetingId)

    override fun observeCommissionIds(meetingId: String): Flow<List<String>> =
        meetingDao.observeCommissionIdsFor(meetingId)

    override fun observeLinksForProject(projectId: String): Flow<List<MeetingCommissionEntity>> =
        meetingDao.observeLinksForProject(projectId)

    override suspend fun refresh(projectId: String): Result<Unit> = runCatching {
        val meetings = supabase.from("meeting")
            .select(Columns.ALL) {
                filter { eq("project_id", projectId) }
            }
            .decodeList<MeetingDto>()
        val meetingIds = meetings.map { it.id }
        val links = if (meetingIds.isEmpty()) {
            emptyList()
        } else {
            supabase.from("meeting_commission")
                .select(Columns.ALL) {
                    filter { isIn("meeting_id", meetingIds) }
                }
                .decodeList<MeetingCommissionLinkDto>()
        }
        meetingDao.replaceForProject(
            projectId = projectId,
            meetings = meetings.map { it.toEntity() },
            links = links.map { it.toEntity() },
        )
    }

    override suspend fun create(input: CreateMeetingInput): Result<MeetingEntity> = runCatching {
        require(input.commissionIds.isNotEmpty()) { "commissionIds must not be empty" }
        // Atomic RPC : the meeting row + its meeting_commission link rows are
        // inserted in a single transaction. This lets the deferred trigger
        // `trg_dispatch_meeting_push` see the full link set at COMMIT time
        // (cf. migration 012) so the dispatch routes between
        // dispatch_meeting_push and dispatch_conflict_push correctly.
        val params = buildJsonObject {
            put("p_project_id", input.projectId)
            put("p_title", input.title.orEmpty())
            put("p_starts_at", input.startsAt)
            put("p_ends_at", input.endsAt)
            put("p_commission_ids", buildJsonArray { input.commissionIds.forEach { add(it) } })
            put(
                "p_created_by_member_id",
                input.createdByMemberId?.let(::JsonPrimitive) ?: JsonNull,
            )
        }
        val dto = supabase.postgrest
            .rpc(function = "create_meeting_with_commissions", parameters = params)
            .decodeAs<MeetingDto>()
        val links = input.commissionIds.map { commissionId ->
            MeetingCommissionLinkDto(meetingId = dto.id, commissionId = commissionId)
        }
        val meeting = dto.toEntity()
        meetingDao.upsertAll(listOf(meeting))
        meetingDao.upsertLinks(links.map { it.toEntity() })
        meeting
    }

    override suspend fun update(input: UpdateMeetingInput): Result<MeetingEntity> = runCatching {
        require(input.commissionIds.isNotEmpty()) { "commissionIds must not be empty" }
        val params = buildJsonObject {
            put("p_meeting_id", input.meetingId)
            put("p_title", input.title.orEmpty())
            put("p_starts_at", input.startsAt)
            put("p_ends_at", input.endsAt)
            put("p_commission_ids", buildJsonArray { input.commissionIds.forEach { add(it) } })
        }
        val dto = supabase.postgrest
            .rpc(function = "update_meeting_with_commissions", parameters = params)
            .decodeAs<MeetingDto>()
        val meeting = dto.toEntity()
        meetingDao.upsertAll(listOf(meeting))
        // Replace link set in Room to match server.
        meetingDao.deleteLinksFor(listOf(input.meetingId))
        meetingDao.upsertLinks(
            input.commissionIds.map { MeetingCommissionEntity(input.meetingId, it) },
        )
        meeting
    }

    override suspend fun delete(meetingId: String): Result<Unit> = runCatching {
        val params = buildJsonObject {
            put("p_meeting_id", meetingId)
        }
        supabase.postgrest.rpc(function = "delete_meeting_with_dispatch", parameters = params)
        meetingDao.deleteLinksFor(listOf(meetingId))
        meetingDao.deleteByIds(listOf(meetingId))
    }
}
