package com.anthooop.colision.app

import com.anthooop.colision.core.common.AppConfig
import com.anthooop.colision.core.common.VersionCompare
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppUpdateTest {

    private fun config(min: String, latest: String) = AppConfig(
        minSupportedVersion = min,
        latestVersion = latest,
        androidStoreUrl = "https://play",
        iosStoreUrl = "https://apps",
        updateMessage = null,
    )

    @Test
    fun `version compare handles numeric segments and lengths`() {
        assertTrue(VersionCompare.compare("1.2.10", "1.2.9") > 0)
        assertTrue(VersionCompare.compare("1.2", "1.2.0") == 0)
        assertTrue(VersionCompare.isOutdated("1.2.0", "1.2.1"))
        assertFalse(VersionCompare.isOutdated("1.2.1", "1.2.1"))
    }

    @Test
    fun `dev suffix is ignored per segment`() {
        assertEquals(0, VersionCompare.compare("1.2.0-dev", "1.2.0"))
    }

    @Test
    fun `no prompt when up to date`() {
        assertEquals(
            AppUpdateState.None,
            resolveUpdateState("1.2.1", config(min = "1.0.0", latest = "1.2.1")),
        )
    }

    @Test
    fun `optional update when behind latest but above minimum`() {
        val state = resolveUpdateState("1.2.0", config(min = "1.0.0", latest = "1.2.1"))
        assertTrue(state is AppUpdateState.Available && !state.forced)
    }

    @Test
    fun `forced update when below minimum supported`() {
        val state = resolveUpdateState("1.0.0", config(min = "1.2.0", latest = "1.2.1"))
        assertTrue(state is AppUpdateState.Available && state.forced)
    }
}
