package com.anthooop.colision.feature.meeting.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface ConflictsRepository {
    /** Authoritative server-side conflict detection (FR20). */
    suspend fun detect(args: DetectConflictsArgs): Result<List<ConflictRow>>
}

class DefaultConflictsRepository(
    private val supabase: SupabaseClient,
) : ConflictsRepository {
    override suspend fun detect(args: DetectConflictsArgs): Result<List<ConflictRow>> = runCatching {
        val params = buildJsonObject {
            put("p_project_id", args.projectId)
            put("p_commission_ids", buildJsonArray { args.commissionIds.forEach { add(it) } })
            put("p_start", args.start)
            put("p_end", args.end)
        }
        supabase.postgrest
            .rpc(function = "detect_conflicts", parameters = params)
            .decodeList<ConflictRow>()
    }
}
