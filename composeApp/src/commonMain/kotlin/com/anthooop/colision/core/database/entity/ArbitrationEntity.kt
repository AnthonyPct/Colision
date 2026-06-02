package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Decision rendered by a conflicted member: "I'll attend [conflicting_meeting]
 * instead of [meeting]". Stored client-side as a mirror of the Supabase
 * `arbitration` table so the creator can see consolidated status (story 4.6)
 * without a network round-trip.
 */
@Entity(
    tableName = "arbitration",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MeetingEntity::class,
            parentColumns = ["id"],
            childColumns = ["meetingId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MeetingEntity::class,
            parentColumns = ["id"],
            childColumns = ["conflictingMeetingId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("memberId"),
        Index("meetingId"),
        Index("conflictingMeetingId"),
        Index(value = ["memberId", "meetingId", "conflictingMeetingId"], unique = true),
    ],
)
data class ArbitrationEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val meetingId: String,
    val conflictingMeetingId: String,
    val decidedAt: String,
)
