package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "member_commission",
    primaryKeys = ["memberId", "commissionId"],
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
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
data class MemberCommissionEntity(
    val memberId: String,
    val commissionId: String,
)
