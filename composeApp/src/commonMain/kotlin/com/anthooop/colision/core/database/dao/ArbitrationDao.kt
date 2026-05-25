package com.anthooop.colision.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.anthooop.colision.core.database.entity.ArbitrationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArbitrationDao {
    /** Arbitrations where the member is skipping [meetingId] for another meeting. */
    @Query("SELECT * FROM arbitration WHERE meetingId = :meetingId")
    fun observeSkippingMeeting(meetingId: String): Flow<List<ArbitrationEntity>>

    /** Arbitrations where the member chose [meetingId] over a conflicting one. */
    @Query("SELECT * FROM arbitration WHERE conflictingMeetingId = :meetingId")
    fun observeChoosingMeeting(meetingId: String): Flow<List<ArbitrationEntity>>

    @Upsert
    suspend fun upsertAll(items: List<ArbitrationEntity>)

    @Query("DELETE FROM arbitration WHERE id NOT IN (:keepIds)")
    suspend fun deleteOthers(keepIds: List<String>)

    @Query("DELETE FROM arbitration")
    suspend fun deleteAll()

    @Query("SELECT id FROM arbitration")
    suspend fun allIds(): List<String>
}
