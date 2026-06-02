package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "meeting_commission",
    primaryKeys = ["meetingId", "commissionId"],
    foreignKeys = [
        ForeignKey(
            entity = MeetingEntity::class,
            parentColumns = ["id"],
            childColumns = ["meetingId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CommissionEntity::class,
            parentColumns = ["id"],
            childColumns = ["commissionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("commissionId")],
)
data class MeetingCommissionEntity(
    val meetingId: String,
    val commissionId: String,
)
