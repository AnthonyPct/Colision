package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meeting",
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
    indices = [Index("projectId"), Index("startsAt"), Index("createdByMemberId")],
)
data class MeetingEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String?,
    val startsAt: String,
    val endsAt: String,
    val createdByMemberId: String?,
    val createdAt: String,
    val updatedAt: String,
)
