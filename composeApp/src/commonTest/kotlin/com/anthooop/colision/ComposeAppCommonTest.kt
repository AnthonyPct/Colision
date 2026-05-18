package com.anthooop.colision

import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.common.appErrorResult
import com.anthooop.colision.core.common.foldAppError
import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppCommonTest {

    @Test
    fun `appErrorResult roundtrips through foldAppError`() {
        // Given a failing Result wrapping a typed AppError
        val result = appErrorResult<String>(AppError.NetworkUnavailable)

        // When folded
        val folded = result.foldAppError(
            onSuccess = { _: String -> "success" },
            onError = { e: AppError ->
                when (e) {
                    AppError.NetworkUnavailable -> "no-net"
                    AppError.ServerUnreachable -> "no-server"
                    AppError.ProjectCodeInvalid -> "bad-code"
                    AppError.ProjectCodeRateLimited -> "rate"
                    AppError.CommissionRequired -> "commission"
                    AppError.AnonymousSessionExpired -> "expired"
                    is AppError.MeetingTimeRangeInvalid -> "range"
                    is AppError.Unknown -> "unknown"
                }
            },
        )

        // Then the typed branch is hit
        assertEquals("no-net", folded)
    }

    @Test
    fun `foldAppError maps untyped Throwable to AppError-Unknown`() {
        val result: Result<String> = Result.failure(IllegalStateException("boom"))

        val folded = result.foldAppError(
            onSuccess = { _: String -> "ok" },
            onError = { e: AppError -> if (e is AppError.Unknown) "unknown" else "typed" },
        )

        assertEquals("unknown", folded)
    }
}
