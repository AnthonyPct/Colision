package com.anthooop.colision.feature.poll.data

import com.anthooop.colision.core.database.dao.PollDao
import com.anthooop.colision.core.database.entity.PollCommissionEntity
import com.anthooop.colision.core.database.entity.PollEntity
import com.anthooop.colision.core.database.entity.PollOptionEntity
import com.anthooop.colision.core.database.entity.PollVoteEntity
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

/** Target of a poll — who is allowed to vote. */
enum class PollTarget(val wire: String) {
    Public("public"),
    Commissions("commissions"),
}

data class CreatePollInput(
    val projectId: String,
    val question: String,
    val target: PollTarget,
    val commissionIds: List<String>,
    val optionLabels: List<String>,
    val closesAt: String,
    val createdByMemberId: String?,
)

interface PollsRepository {
    fun observeByProject(projectId: String): Flow<List<PollEntity>>
    fun observeById(pollId: String): Flow<PollEntity?>
    fun observeOptions(pollId: String): Flow<List<PollOptionEntity>>
    fun observeOptionsForProject(projectId: String): Flow<List<PollOptionEntity>>
    fun observeVotes(pollId: String): Flow<List<PollVoteEntity>>
    fun observeVotesForProject(projectId: String): Flow<List<PollVoteEntity>>
    fun observeCommissionIds(pollId: String): Flow<List<String>>
    fun observeCommissionLinksForProject(projectId: String): Flow<List<PollCommissionEntity>>
    suspend fun refresh(projectId: String): Result<Unit>
    suspend fun create(input: CreatePollInput): Result<PollEntity>
    suspend fun vote(pollId: String, optionId: String, memberId: String): Result<Unit>
    suspend fun delete(pollId: String): Result<Unit>
}

class DefaultPollsRepository(
    private val supabase: SupabaseClient,
    private val pollDao: PollDao,
) : PollsRepository {

    override fun observeByProject(projectId: String): Flow<List<PollEntity>> =
        pollDao.observeByProject(projectId)

    override fun observeById(pollId: String): Flow<PollEntity?> =
        pollDao.observeById(pollId)

    override fun observeOptions(pollId: String): Flow<List<PollOptionEntity>> =
        pollDao.observeOptions(pollId)

    override fun observeOptionsForProject(projectId: String): Flow<List<PollOptionEntity>> =
        pollDao.observeOptionsForProject(projectId)

    override fun observeVotes(pollId: String): Flow<List<PollVoteEntity>> =
        pollDao.observeVotes(pollId)

    override fun observeVotesForProject(projectId: String): Flow<List<PollVoteEntity>> =
        pollDao.observeVotesForProject(projectId)

    override fun observeCommissionIds(pollId: String): Flow<List<String>> =
        pollDao.observeCommissionIdsFor(pollId)

    override fun observeCommissionLinksForProject(projectId: String): Flow<List<PollCommissionEntity>> =
        pollDao.observeCommissionLinksForProject(projectId)

    override suspend fun refresh(projectId: String): Result<Unit> = runCatching {
        val polls = supabase.from("poll")
            .select(Columns.ALL) {
                filter { eq("project_id", projectId) }
            }
            .decodeList<PollDto>()
        val pollIds = polls.map { it.id }
        val (options, links, votes) = if (pollIds.isEmpty()) {
            Triple(
                emptyList<PollOptionDto>(),
                emptyList<PollCommissionLinkDto>(),
                emptyList<PollVoteDto>(),
            )
        } else {
            Triple(
                supabase.from("poll_option")
                    .select(Columns.ALL) { filter { isIn("poll_id", pollIds) } }
                    .decodeList<PollOptionDto>(),
                supabase.from("poll_commission")
                    .select(Columns.ALL) { filter { isIn("poll_id", pollIds) } }
                    .decodeList<PollCommissionLinkDto>(),
                supabase.from("poll_vote")
                    .select(Columns.ALL) { filter { isIn("poll_id", pollIds) } }
                    .decodeList<PollVoteDto>(),
            )
        }
        pollDao.replaceForProject(
            projectId = projectId,
            polls = polls.map { it.toEntity() },
            options = options.map { it.toEntity() },
            commissions = links.map { it.toEntity() },
            votes = votes.map { it.toEntity() },
        )
    }

    override suspend fun create(input: CreatePollInput): Result<PollEntity> = runCatching {
        require(input.optionLabels.count { it.isNotBlank() } >= 2) { "at least two options required" }
        if (input.target == PollTarget.Commissions) {
            require(input.commissionIds.isNotEmpty()) { "commissionIds must not be empty" }
        }
        // Atomic RPC : poll + options + poll_commission links inserted in a
        // single transaction so the deferred trigger trg_dispatch_poll_push
        // sees the full picture at COMMIT time (cf. migration 015).
        val params = buildJsonObject {
            put("p_project_id", input.projectId)
            put("p_question", input.question)
            put("p_target_type", input.target.wire)
            put(
                "p_commission_ids",
                buildJsonArray { input.commissionIds.forEach { add(it) } },
            )
            put(
                "p_option_labels",
                buildJsonArray { input.optionLabels.filter { it.isNotBlank() }.forEach { add(it) } },
            )
            put("p_closes_at", input.closesAt)
            put(
                "p_created_by_member_id",
                input.createdByMemberId?.let(::JsonPrimitive) ?: JsonNull,
            )
        }
        val dto = supabase.postgrest
            .rpc(function = "create_poll", parameters = params)
            .decodeAs<PollDto>()
        val poll = dto.toEntity()
        // Options got server-generated ids; fetch them + the commission links
        // so the cache is immediately consistent without a full refresh.
        val options = supabase.from("poll_option")
            .select(Columns.ALL) { filter { eq("poll_id", dto.id) } }
            .decodeList<PollOptionDto>()
        val links = if (input.target == PollTarget.Commissions) {
            supabase.from("poll_commission")
                .select(Columns.ALL) { filter { eq("poll_id", dto.id) } }
                .decodeList<PollCommissionLinkDto>()
        } else {
            emptyList()
        }
        pollDao.upsertPolls(listOf(poll))
        pollDao.upsertOptions(options.map { it.toEntity() })
        pollDao.upsertCommissions(links.map { it.toEntity() })
        poll
    }

    override suspend fun vote(
        pollId: String,
        optionId: String,
        memberId: String,
    ): Result<Unit> = runCatching {
        val params = buildJsonObject {
            put("p_poll_id", pollId)
            put("p_option_id", optionId)
            put("p_member_id", memberId)
        }
        val dto = supabase.postgrest
            .rpc(function = "cast_vote", parameters = params)
            .decodeAs<PollVoteDto>()
        pollDao.upsertVotes(listOf(dto.toEntity()))
    }

    override suspend fun delete(pollId: String): Result<Unit> = runCatching {
        val params = buildJsonObject { put("p_poll_id", pollId) }
        supabase.postgrest.rpc(function = "delete_poll", parameters = params)
        pollDao.deleteVotesFor(listOf(pollId))
        pollDao.deleteCommissionsFor(listOf(pollId))
        pollDao.deleteOptionsFor(listOf(pollId))
        pollDao.deletePollsByIds(listOf(pollId))
    }
}
