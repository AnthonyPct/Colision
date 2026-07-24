package com.anthooop.colision.core.common

enum class MobilePlatform { Android, Ios }

/** Which mobile OS the app is running on. Used to pick the right store URL. */
expect val currentPlatform: MobilePlatform
