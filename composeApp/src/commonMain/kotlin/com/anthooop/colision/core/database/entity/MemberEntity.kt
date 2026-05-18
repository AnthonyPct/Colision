package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "member",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId"), Index("deviceId")],
)
data class MemberEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val deviceId: String?,
    val displayName: String,
    val createdAt: String,
    val updatedAt: String,
)
