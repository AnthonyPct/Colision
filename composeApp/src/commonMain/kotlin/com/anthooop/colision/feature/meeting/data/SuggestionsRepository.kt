package com.anthooop.colision.feature.meeting.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class SuggestedSlot(
    @SerialName("slot_starts_at") val startsAt: String,
    @SerialName("slot_ends_at") val endsAt: String,
)

interface SuggestionsRepository {
    /** Asks the server for [limit] conflict-free slots around [anchor] (FR23). */
    suspend fun suggest(
        projectId: String,
        commissionIds: List<String>,
        anchor: String,
        durationMinutes: Int,
        windowDays: Int = 7,
        limit: Int = 5,
    ): Result<List<SuggestedSlot>>
}

class DefaultSuggestionsRepository(
    private val supabase: SupabaseClient,
) : SuggestionsRepository {
    override suspend fun suggest(
        projectId: String,
        commissionIds: List<String>,
        anchor: String,
        durationMinutes: Int,
        windowDays: Int,
        limit: Int,
    ): Result<List<SuggestedSlot>> = runCatching {
        val params = buildJsonObject {
            put("p_project_id", projectId)
            put("p_commission_ids", buildJsonArray { commissionIds.forEach { add(it) } })
            put("p_anchor", anchor)
            put("p_duration_min", durationMinutes)
            put("p_window_days", windowDays)
            put("p_limit", limit)
        }
        supabase.postgrest
            .rpc(function = "suggest_free_slots", parameters = params)
            .decodeList<SuggestedSlot>()
    }
}
