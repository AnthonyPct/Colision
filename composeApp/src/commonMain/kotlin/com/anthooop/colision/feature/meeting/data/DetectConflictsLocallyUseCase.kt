package com.anthooop.colision.feature.meeting.data

import com.anthooop.colision.core.database.dao.MeetingDao

/**
 * Cheap local pre-check that runs against the Room cache while the user is
 * still editing the meeting creation form. Returns the count of members
 * already booked on the chosen [startIso, endIso] window in any commission.
 *
 * The authoritative answer always comes from the server `detect_conflicts`
 * RPC (story 4.2 AC4) — this is only for the discreet live badge.
 */
class DetectConflictsLocallyUseCase(
    private val meetingDao: MeetingDao,
) {
    suspend operator fun invoke(
        projectId: String,
        commissionIds: List<String>,
        startIso: String,
        endIso: String,
    ): Int {
        if (commissionIds.isEmpty()) return 0
        return meetingDao
            .findLocalConflictMemberIds(projectId, commissionIds, startIso, endIso)
            .size
    }
}
