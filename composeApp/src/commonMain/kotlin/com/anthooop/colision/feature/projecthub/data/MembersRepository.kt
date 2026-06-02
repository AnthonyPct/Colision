package com.anthooop.colision.feature.projecthub.data

import com.anthooop.colision.core.database.dao.MemberCommissionDao
import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import com.anthooop.colision.core.database.entity.MemberEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow

interface MembersRepository {
    fun observeByProject(projectId: String): Flow<List<MemberEntity>>
    fun observeByCommission(commissionId: String): Flow<List<MemberEntity>>
    fun observeAttendingMeeting(meetingId: String): Flow<List<MemberEntity>>
    fun observeAssignments(memberId: String): Flow<List<MemberCommissionEntity>>
    fun observeAssignmentsByCommission(commissionId: String): Flow<List<MemberCommissionEntity>>
    fun observeAssignmentsForProject(projectId: String): Flow<List<MemberCommissionEntity>>
    suspend fun findById(memberId: String): MemberEntity?
    suspend fun refresh(projectId: String): Result<Unit>
    suspend fun addMember(
        projectId: String,
        displayName: String,
        deviceId: String? = null,
    ): Result<MemberEntity>
    suspend fun setAssignment(memberId: String, commissionId: String, assigned: Boolean): Result<Unit>
    suspend fun claimIdentity(memberId: String, deviceId: String): Result<Unit>
    suspend fun releaseIdentity(memberId: String): Result<Unit>
}

class DefaultMembersRepository(
    private val supabase: SupabaseClient,
    private val memberDao: MemberDao,
    private val memberCommissionDao: MemberCommissionDao,
) : MembersRepository {

    override fun observeByProject(projectId: String): Flow<List<MemberEntity>> =
        memberDao.observeByProject(projectId)

    override fun observeByCommission(commissionId: String): Flow<List<MemberEntity>> =
        memberDao.observeByCommission(commissionId)

    override fun observeAttendingMeeting(meetingId: String): Flow<List<MemberEntity>> =
        memberDao.observeAttendingMeeting(meetingId)

    override fun observeAssignments(memberId: String): Flow<List<MemberCommissionEntity>> =
        memberCommissionDao.observeByMember(memberId)

    override fun observeAssignmentsByCommission(
        commissionId: String,
    ): Flow<List<MemberCommissionEntity>> =
        memberCommissionDao.observeByCommission(commissionId)

    override fun observeAssignmentsForProject(
        projectId: String,
    ): Flow<List<MemberCommissionEntity>> =
        memberCommissionDao.observeForProject(projectId)

    override suspend fun findById(memberId: String): MemberEntity? =
        memberDao.findById(memberId)

    override suspend fun refresh(projectId: String): Result<Unit> = runCatching {
        val members = supabase.from("member")
            .select(Columns.ALL) {
                filter { eq("project_id", projectId) }
            }
            .decodeList<MemberDto>()
        memberDao.upsertAll(members.map { it.toEntity() })

        val links = supabase.from("member_commission")
            .select(Columns.ALL) {
                filter {
                    isIn("member_id", members.map { it.id })
                }
            }
            .decodeList<MemberCommissionLinkDto>()
        memberCommissionDao.upsertAll(
            links.map { MemberCommissionEntity(memberId = it.memberId, commissionId = it.commissionId) },
        )
    }

    override suspend fun addMember(
        projectId: String,
        displayName: String,
        deviceId: String?,
    ): Result<MemberEntity> = runCatching {
        val dto = supabase.from("member")
            .insert(
                MemberInsertDto(
                    projectId = projectId,
                    displayName = displayName,
                    deviceId = deviceId,
                ),
            ) {
                select()
            }
            .decodeSingle<MemberDto>()
        val entity = dto.toEntity()
        memberDao.upsert(entity)
        entity
    }

    override suspend fun setAssignment(
        memberId: String,
        commissionId: String,
        assigned: Boolean,
    ): Result<Unit> = runCatching {
        if (assigned) {
            // upsert (Prefer: resolution=merge-duplicates) instead of plain
            // insert: the row's primary key is (member_id, commission_id) so
            // a re-toggle or a sync race used to raise 23505. The semantic
            // we want is "ensure the assignment exists" which is naturally
            // idempotent.
            supabase.from("member_commission")
                .upsert(
                    MemberCommissionLinkDto(memberId = memberId, commissionId = commissionId),
                )
            memberCommissionDao.upsert(
                MemberCommissionEntity(memberId = memberId, commissionId = commissionId),
            )
        } else {
            supabase.from("member_commission").delete {
                filter {
                    eq("member_id", memberId)
                    eq("commission_id", commissionId)
                }
            }
            memberCommissionDao.delete(memberId = memberId, commissionId = commissionId)
        }
    }

    override suspend fun claimIdentity(memberId: String, deviceId: String): Result<Unit> = runCatching {
        supabase.from("member").update(MemberDeviceUpdateDto(deviceId = deviceId)) {
            filter { eq("id", memberId) }
        }
        val existing = memberDao.findById(memberId)
        if (existing != null) memberDao.update(existing.copy(deviceId = deviceId))
    }

    override suspend fun releaseIdentity(memberId: String): Result<Unit> = runCatching {
        supabase.from("member").update(MemberDeviceUpdateDto(deviceId = null)) {
            filter { eq("id", memberId) }
        }
        val existing = memberDao.findById(memberId)
        if (existing != null) memberDao.update(existing.copy(deviceId = null))
    }
}
