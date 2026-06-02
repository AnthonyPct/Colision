package com.anthooop.colision.core.common

fun <T> appErrorResult(error: AppError): Result<T> =
    Result.failure(AppErrorThrowable(error))

inline fun <T, R> Result<T>.foldAppError(
    onSuccess: (T) -> R,
    onError: (AppError) -> R,
): R = fold(
    onSuccess = onSuccess,
    onFailure = { t -> onError((t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)) },
)
