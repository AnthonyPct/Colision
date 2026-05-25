package com.anthooop.colision.feature.agenda.data

import com.anthooop.colision.core.database.dao.MeetingDao
import com.anthooop.colision.core.database.entity.MeetingCommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow

data class CreateMeetingInput(
    val projectId: String,
    val title: String?,
    val startsAt: String,
    val endsAt: String,
    val commissionIds: List<String>,
    val createdByMemberId: String?,
)

interface MeetingsRepository {
    fun observeForMember(memberId: String): Flow<List<MeetingEntity>>
    fun observeByCommission(commissionId: String): Flow<List<MeetingEntity>>
    fun observeById(meetingId: String): Flow<MeetingEntity?>
    fun observeCommissionIds(meetingId: String): Flow<List<String>>
    fun observeLinksForProject(projectId: String): Flow<List<MeetingCommissionEntity>>
    suspend fun refresh(projectId: String): Result<Unit>
    suspend fun create(input: CreateMeetingInput): Result<MeetingEntity>
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
        val dto = supabase.from("meeting")
            .insert(
                MeetingInsertDto(
                    projectId = input.projectId,
                    title = input.title?.takeIf { it.isNotBlank() },
                    startsAt = input.startsAt,
                    endsAt = input.endsAt,
                    createdByMemberId = input.createdByMemberId,
                ),
            ) { select() }
            .decodeSingle<MeetingDto>()
        val links = input.commissionIds.map { commissionId ->
            MeetingCommissionLinkDto(meetingId = dto.id, commissionId = commissionId)
        }
        supabase.from("meeting_commission").insert(links)
        val meeting = dto.toEntity()
        meetingDao.upsertAll(listOf(meeting))
        meetingDao.upsertLinks(links.map { it.toEntity() })
        meeting
    }
}
