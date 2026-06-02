package com.anthooop.colision.feature.meeting.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConflictRow(
    @SerialName("member_id") val memberId: String,
    @SerialName("member_display_name") val memberDisplayName: String,
    @SerialName("meeting_id") val meetingId: String,
    @SerialName("meeting_title") val meetingTitle: String?,
    @SerialName("meeting_starts_at") val meetingStartsAt: String,
    @SerialName("meeting_ends_at") val meetingEndsAt: String,
    @SerialName("commission_id") val commissionId: String,
    @SerialName("commission_name") val commissionName: String,
)

@Serializable
data class DetectConflictsArgs(
    @SerialName("p_project_id") val projectId: String,
    @SerialName("p_commission_ids") val commissionIds: List<String>,
    @SerialName("p_start") val start: String,
    @SerialName("p_end") val end: String,
)
