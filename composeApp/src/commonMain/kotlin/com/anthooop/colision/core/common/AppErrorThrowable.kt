package com.anthooop.colision.core.common

class AppErrorThrowable(val error: AppError) : Throwable(error.toString())
