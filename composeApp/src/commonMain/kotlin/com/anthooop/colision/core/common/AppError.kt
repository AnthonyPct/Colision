package com.anthooop.colision.core.common

// Sealed class kept separate from Throwable to preserve exhaustive `when`
// matching in ViewModels. Throwable interop goes through AppErrorThrowable.
sealed class AppError {
    data object NetworkUnavailable : AppError()
    data object ServerUnreachable : AppError()
    data object ProjectCodeInvalid : AppError()
    data object ProjectCodeRateLimited : AppError()
    data class MeetingTimeRangeInvalid(val reason: String) : AppError()
    data object CommissionRequired : AppError()
    data object AnonymousSessionExpired : AppError()
    data class Unknown(val cause: Throwable) : AppError()
}
