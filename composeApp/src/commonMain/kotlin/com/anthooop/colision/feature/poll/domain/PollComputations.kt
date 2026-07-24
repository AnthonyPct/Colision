package com.anthooop.colision.feature.poll.domain

import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Pure derivations for the poll feature — kept out of the ViewModels so the
 * open/closed rule, the day-count, and the eligibility logic stay unit-testable
 * without standing up Compose or the data layer.
 *
 * A poll is never stored with an explicit open/closed flag (migration 015): the
 * status is derived from `closes_at` vs now, and the server rejects votes past
 * the deadline. The UI mirrors that derivation here.
 */
object PollComputations {

    const val TARGET_PUBLIC = "public"
    const val TARGET_COMMISSIONS = "commissions"

    @OptIn(ExperimentalTime::class)
    fun isOpen(closesAtIso: String, now: Instant): Boolean {
        val closesAt = runCatching { Instant.parse(closesAtIso) }.getOrNull() ?: return true
        return closesAt > now
    }

    /**
     * Whole days from [now] to the poll's closing date, in the device timezone.
     * 0 means "closes today" (dernier jour). Negative values mean already
     * closed and are clamped to 0 by callers that only show it while open.
     */
    @OptIn(ExperimentalTime::class)
    fun daysLeft(closesAtIso: String, now: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): Int {
        val closesAt = runCatching { Instant.parse(closesAtIso) }.getOrNull() ?: return 0
        val today = now.toLocalDateTime(tz).date
        val closeDay = closesAt.toLocalDateTime(tz).date
        return today.daysUntil(closeDay)
    }

    /**
     * Whether [myCommissionIds] make the current member eligible to vote on a
     * poll with the given [targetType] / [scopeCommissionIds].
     * Public polls are open to any project member.
     */
    fun canVote(
        targetType: String,
        scopeCommissionIds: Set<String>,
        myCommissionIds: Set<String>,
    ): Boolean = when (targetType) {
        TARGET_PUBLIC -> true
        else -> scopeCommissionIds.any { it in myCommissionIds }
    }

    /**
     * Number of members eligible to vote. Public → the whole project;
     * commission-scoped → distinct members belonging to at least one targeted
     * commission (via the member_commission junction).
     */
    fun eligibleCount(
        targetType: String,
        scopeCommissionIds: Set<String>,
        projectMemberIds: Set<String>,
        assignments: List<MemberCommissionEntity>,
    ): Int = when (targetType) {
        TARGET_PUBLIC -> projectMemberIds.size
        else -> assignments
            .asSequence()
            .filter { it.commissionId in scopeCommissionIds && it.memberId in projectMemberIds }
            .map { it.memberId }
            .toSet()
            .size
    }
}
