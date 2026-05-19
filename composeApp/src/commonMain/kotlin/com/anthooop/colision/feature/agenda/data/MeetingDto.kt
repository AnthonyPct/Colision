package com.anthooop.colision.feature.agenda.data

import com.anthooop.colision.core.database.entity.MeetingCommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeetingDto(
    val id: String,
    @SerialName("project_id") val projectId: String,
    val title: String? = null,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("created_by_member_id") val createdByMemberId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class MeetingCommissionLinkDto(
    @SerialName("meeting_id") val meetingId: String,
    @SerialName("commission_id") val commissionId: String,
)

fun MeetingDto.toEntity(): MeetingEntity = MeetingEntity(
    id = id,
    projectId = projectId,
    title = title,
    startsAt = startsAt,
    endsAt = endsAt,
    createdByMemberId = createdByMemberId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun MeetingCommissionLinkDto.toEntity(): MeetingCommissionEntity = MeetingCommissionEntity(
    meetingId = meetingId,
    commissionId = commissionId,
)
