package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poll_option",
    foreignKeys = [
        ForeignKey(
            entity = PollEntity::class,
            parentColumns = ["id"],
            childColumns = ["pollId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("pollId")],
)
data class PollOptionEntity(
    @PrimaryKey val id: String,
    val pollId: String,
    val label: String,
    val position: Int,
)
