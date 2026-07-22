package com.anthooop.colision.feature.meeting.createmeeting

import kotlin.test.Test
import kotlin.test.assertEquals

class DurationOptionTest {

    @Test
    fun `duration options cover 1h to 3h in 30-minute steps`() {
        assertEquals(
            listOf(60, 90, 120, 150, 180),
            DurationOption.entries.map { it.minutes },
        )
    }
}
