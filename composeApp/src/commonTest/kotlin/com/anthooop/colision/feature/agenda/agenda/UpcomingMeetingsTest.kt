package com.anthooop.colision.feature.agenda.agenda

import com.anthooop.colision.core.database.entity.MeetingEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class UpcomingMeetingsTest {

    private val now = Instant.parse("2026-07-22T12:00:00Z")

    private fun meeting(id: String, startsAt: String, endsAt: String) = MeetingEntity(
        id = id,
        projectId = "proj",
        title = id,
        startsAt = startsAt,
        endsAt = endsAt,
        createdByMemberId = "m1",
        createdAt = startsAt,
        updatedAt = startsAt,
    )

    @Test
    fun `drops finished meetings, keeps ongoing and future, preserves order`() {
        val past = meeting("past", "2026-07-22T10:00:00Z", "2026-07-22T11:00:00Z")
        val ongoing = meeting("ongoing", "2026-07-22T11:30:00Z", "2026-07-22T12:30:00Z")
        val future = meeting("future", "2026-07-23T09:00:00Z", "2026-07-23T10:00:00Z")

        val result = upcomingMeetings(listOf(past, ongoing, future), now)

        assertEquals(listOf("ongoing", "future"), result.map { it.id })
    }

    @Test
    fun `a meeting ending exactly now is still shown`() {
        val endingNow = meeting("endingNow", "2026-07-22T11:00:00Z", "2026-07-22T12:00:00Z")

        assertEquals(listOf("endingNow"), upcomingMeetings(listOf(endingNow), now).map { it.id })
    }

    @Test
    fun `an unparseable endsAt is kept rather than silently dropped`() {
        val broken = meeting("broken", "2026-07-22T10:00:00Z", "not-a-date")

        assertEquals(listOf("broken"), upcomingMeetings(listOf(broken), now).map { it.id })
    }

    @Test
    fun `all-past list yields empty (dashboard shows its empty state)`() {
        val a = meeting("a", "2026-07-20T10:00:00Z", "2026-07-20T11:00:00Z")
        val b = meeting("b", "2026-07-21T10:00:00Z", "2026-07-21T11:00:00Z")

        assertEquals(emptyList(), upcomingMeetings(listOf(a, b), now).map { it.id })
    }
}
