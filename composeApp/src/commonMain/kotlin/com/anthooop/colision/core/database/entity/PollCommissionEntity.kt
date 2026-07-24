package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "poll_commission",
    primaryKeys = ["pollId", "commissionId"],
    foreignKeys = [
        ForeignKey(
            entity = PollEntity::class,
            parentColumns = ["id"],
            childColumns = ["pollId"],
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
data class PollCommissionEntity(
    val pollId: String,
    val commissionId: String,
)
