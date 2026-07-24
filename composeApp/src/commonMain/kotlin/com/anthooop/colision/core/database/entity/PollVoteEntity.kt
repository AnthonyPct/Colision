package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "poll_vote",
    primaryKeys = ["pollId", "memberId"],
    foreignKeys = [
        ForeignKey(
            entity = PollEntity::class,
            parentColumns = ["id"],
            childColumns = ["pollId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PollOptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["optionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("memberId"), Index("optionId")],
)
data class PollVoteEntity(
    val pollId: String,
    val memberId: String,
    val optionId: String,
    val createdAt: String,
    val updatedAt: String,
)
