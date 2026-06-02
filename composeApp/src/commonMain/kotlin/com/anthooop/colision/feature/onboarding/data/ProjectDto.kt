package com.anthooop.colision.feature.onboarding.data

import com.anthooop.colision.core.database.entity.ProjectEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    @SerialName("share_code") val shareCode: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

fun ProjectDto.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    name = name,
    shareCode = shareCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
