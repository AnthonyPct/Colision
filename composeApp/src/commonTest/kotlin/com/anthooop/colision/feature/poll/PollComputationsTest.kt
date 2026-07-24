package com.anthooop.colision.feature.poll

import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import com.anthooop.colision.feature.poll.domain.PollComputations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class PollComputationsTest {

    private val now = Instant.parse("2026-05-20T12:00:00Z")

    @Test
    fun `poll open when closes_at is in the future`() {
        assertTrue(PollComputations.isOpen("2026-05-24T23:59:00Z", now))
    }

    @Test
    fun `poll closed once closes_at has passed`() {
        assertFalse(PollComputations.isOpen("2026-05-16T23:59:00Z", now))
    }

    @Test
    fun `days left counts whole calendar days to the deadline`() {
        assertEquals(4, PollComputations.daysLeft("2026-05-24T23:59:00Z", now, kotlinx.datetime.TimeZone.UTC))
        assertEquals(0, PollComputations.daysLeft("2026-05-20T23:59:00Z", now, kotlinx.datetime.TimeZone.UTC))
    }

    @Test
    fun `public polls let any project member vote`() {
        assertTrue(
            PollComputations.canVote(
                targetType = PollComputations.TARGET_PUBLIC,
                scopeCommissionIds = emptySet(),
                myCommissionIds = emptySet(),
            ),
        )
    }

    @Test
    fun `commission polls require membership in a targeted commission`() {
        assertTrue(
            PollComputations.canVote(
                targetType = PollComputations.TARGET_COMMISSIONS,
                scopeCommissionIds = setOf("c1", "c2"),
                myCommissionIds = setOf("c2"),
            ),
        )
        assertFalse(
            PollComputations.canVote(
                targetType = PollComputations.TARGET_COMMISSIONS,
                scopeCommissionIds = setOf("c1"),
                myCommissionIds = setOf("c3"),
            ),
        )
    }

    @Test
    fun `eligible count is the whole project for a public poll`() {
        assertEquals(
            3,
            PollComputations.eligibleCount(
                targetType = PollComputations.TARGET_PUBLIC,
                scopeCommissionIds = emptySet(),
                projectMemberIds = setOf("m1", "m2", "m3"),
                assignments = emptyList(),
            ),
        )
    }

    @Test
    fun `eligible count dedupes members across targeted commissions`() {
        val assignments = listOf(
            MemberCommissionEntity("m1", "c1"),
            MemberCommissionEntity("m1", "c2"),
            MemberCommissionEntity("m2", "c2"),
            MemberCommissionEntity("m3", "c3"),
        )
        assertEquals(
            2,
            PollComputations.eligibleCount(
                targetType = PollComputations.TARGET_COMMISSIONS,
                scopeCommissionIds = setOf("c1", "c2"),
                projectMemberIds = setOf("m1", "m2", "m3"),
                assignments = assignments,
            ),
        )
    }
}
