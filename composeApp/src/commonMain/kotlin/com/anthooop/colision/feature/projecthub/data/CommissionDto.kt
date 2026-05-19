package com.anthooop.colision.feature.projecthub.data

import com.anthooop.colision.core.database.entity.CommissionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommissionDto(
    val id: String,
    @SerialName("project_id") val projectId: String,
    val name: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class CommissionInsertDto(
    @SerialName("project_id") val projectId: String,
    val name: String,
)

@Serializable
data class CommissionUpdateDto(
    val name: String,
)

fun CommissionDto.toEntity(): CommissionEntity = CommissionEntity(
    id = id,
    projectId = projectId,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
