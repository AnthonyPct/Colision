package com.anthooop.colision.feature.meeting.createmeeting

import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.foldAppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the meeting-creation guard: a meeting must never be built without a
 * resolved creator, which is what produced orphan `created_by_member_id = null`
 * meetings after the 2026-07-22 re-claim.
 */
class BuildCreateMeetingInputTest {

    private fun build(creatorMemberId: String?, title: String = "Réunion") =
        buildCreateMeetingInput(
            projectId = "proj-1",
            title = title,
            startsAt = "2026-07-22T18:00:00Z",
            endsAt = "2026-07-22T19:30:00Z",
            commissionIds = listOf("com-1"),
            creatorMemberId = creatorMemberId,
        )

    @Test
    fun `null creator is rejected as AnonymousSessionExpired`() {
        val result = build(creatorMemberId = null)

        assertTrue(result.isFailure)
        val error = result.foldAppError(onSuccess = { null }, onError = { it })
        assertEquals(AppError.AnonymousSessionExpired, error)
    }

    @Test
    fun `blank creator is rejected`() {
        assertTrue(build(creatorMemberId = "   ").isFailure)
    }

    @Test
    fun `resolved creator produces an input carrying that member id`() {
        val result = build(creatorMemberId = "member-42")

        assertTrue(result.isSuccess)
        assertEquals("member-42", result.getOrThrow().createdByMemberId)
    }

    @Test
    fun `blank title is normalized to null, non-blank is trimmed`() {
        assertNull(build(creatorMemberId = "member-42", title = "   ").getOrThrow().title)
        assertEquals(
            "Conseil",
            build(creatorMemberId = "member-42", title = "  Conseil  ").getOrThrow().title,
        )
    }
}
