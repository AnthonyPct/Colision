package com.anthooop.colision.feature.projecthub.data

import com.anthooop.colision.core.database.entity.MemberEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberDto(
    val id: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("display_name") val displayName: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class MemberInsertDto(
    @SerialName("project_id") val projectId: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
data class MemberCommissionLinkDto(
    @SerialName("member_id") val memberId: String,
    @SerialName("commission_id") val commissionId: String,
)

@Serializable
data class MemberDeviceUpdateDto(
    @SerialName("device_id") val deviceId: String?,
)

fun MemberDto.toEntity(): MemberEntity = MemberEntity(
    id = id,
    projectId = projectId,
    deviceId = deviceId,
    displayName = displayName,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
