package com.anthooop.colision.feature.poll.pollslist

import androidx.compose.runtime.Composable
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.month_april
import colision.composeapp.generated.resources.month_august
import colision.composeapp.generated.resources.month_december
import colision.composeapp.generated.resources.month_february
import colision.composeapp.generated.resources.month_january
import colision.composeapp.generated.resources.month_july
import colision.composeapp.generated.resources.month_june
import colision.composeapp.generated.resources.month_march
import colision.composeapp.generated.resources.month_may
import colision.composeapp.generated.resources.month_november
import colision.composeapp.generated.resources.month_october
import colision.composeapp.generated.resources.month_september
import colision.composeapp.generated.resources.weekday_friday
import colision.composeapp.generated.resources.weekday_monday
import colision.composeapp.generated.resources.weekday_saturday
import colision.composeapp.generated.resources.weekday_sunday
import colision.composeapp.generated.resources.weekday_thursday
import colision.composeapp.generated.resources.weekday_tuesday
import colision.composeapp.generated.resources.weekday_wednesday
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
internal fun rememberPollMonthNames(): List<String> = listOf(
    stringResource(Res.string.month_january),
    stringResource(Res.string.month_february),
    stringResource(Res.string.month_march),
    stringResource(Res.string.month_april),
    stringResource(Res.string.month_may),
    stringResource(Res.string.month_june),
    stringResource(Res.string.month_july),
    stringResource(Res.string.month_august),
    stringResource(Res.string.month_september),
    stringResource(Res.string.month_october),
    stringResource(Res.string.month_november),
    stringResource(Res.string.month_december),
)

@Composable
internal fun rememberPollWeekdayNames(): List<String> = listOf(
    stringResource(Res.string.weekday_monday),
    stringResource(Res.string.weekday_tuesday),
    stringResource(Res.string.weekday_wednesday),
    stringResource(Res.string.weekday_thursday),
    stringResource(Res.string.weekday_friday),
    stringResource(Res.string.weekday_saturday),
    stringResource(Res.string.weekday_sunday),
)

/** "24 mai" — day + month, from an ISO instant (or bare yyyy-MM-dd). */
@OptIn(ExperimentalTime::class)
internal fun pollShortDate(iso: String, months: List<String>): String {
    val date = localDate(iso) ?: return iso.substringBefore('T')
    val month = months.getOrNull(date.month.ordinal) ?: (date.month.ordinal + 1).toString()
    return "${date.day} $month"
}

/** "samedi 24 mai" — weekday + day + month. */
@OptIn(ExperimentalTime::class)
internal fun pollFullDate(iso: String, months: List<String>, weekdays: List<String>): String {
    val date = localDate(iso) ?: return iso.substringBefore('T')
    val month = months.getOrNull(date.month.ordinal) ?: (date.month.ordinal + 1).toString()
    val weekday = weekdays.getOrNull(date.dayOfWeek.isoDayNumber - 1) ?: ""
    return "$weekday ${date.day} $month".trim()
}

@OptIn(ExperimentalTime::class)
private fun localDate(iso: String): kotlinx.datetime.LocalDate? {
    val instant = runCatching { Instant.parse(iso) }.getOrNull()
    if (instant != null) {
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    return runCatching { kotlinx.datetime.LocalDate.parse(iso.substringBefore('T')) }.getOrNull()
}
