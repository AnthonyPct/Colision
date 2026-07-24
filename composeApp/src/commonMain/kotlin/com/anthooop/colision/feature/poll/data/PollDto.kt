package com.anthooop.colision.feature.poll.data

import com.anthooop.colision.core.database.entity.PollCommissionEntity
import com.anthooop.colision.core.database.entity.PollEntity
import com.anthooop.colision.core.database.entity.PollOptionEntity
import com.anthooop.colision.core.database.entity.PollVoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PollDto(
    val id: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("created_by_member_id") val createdByMemberId: String? = null,
    val question: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("closes_at") val closesAt: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class PollOptionDto(
    val id: String,
    @SerialName("poll_id") val pollId: String,
    val label: String,
    val position: Int,
)

@Serializable
data class PollCommissionLinkDto(
    @SerialName("poll_id") val pollId: String,
    @SerialName("commission_id") val commissionId: String,
)

@Serializable
data class PollVoteDto(
    @SerialName("poll_id") val pollId: String,
    @SerialName("member_id") val memberId: String,
    @SerialName("option_id") val optionId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

fun PollDto.toEntity(): PollEntity = PollEntity(
    id = id,
    projectId = projectId,
    createdByMemberId = createdByMemberId,
    question = question,
    targetType = targetType,
    closesAt = closesAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun PollOptionDto.toEntity(): PollOptionEntity = PollOptionEntity(
    id = id,
    pollId = pollId,
    label = label,
    position = position,
)

fun PollCommissionLinkDto.toEntity(): PollCommissionEntity = PollCommissionEntity(
    pollId = pollId,
    commissionId = commissionId,
)

fun PollVoteDto.toEntity(): PollVoteEntity = PollVoteEntity(
    pollId = pollId,
    memberId = memberId,
    optionId = optionId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
