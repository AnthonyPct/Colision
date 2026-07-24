package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poll",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByMemberId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("projectId"), Index("createdByMemberId")],
)
data class PollEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val createdByMemberId: String?,
    val question: String,
    // "public" | "commissions"
    val targetType: String,
    val closesAt: String,
    val createdAt: String,
    val updatedAt: String,
)
