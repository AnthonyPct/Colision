package com.anthooop.colision.core.common

/**
 * Native push transport for the current device. The value is written to
 * `device.platform` in Supabase so the dispatch Edge Functions know which
 * column (`fcm_token` vs `apns_token`) and which provider (FCM vs APNs)
 * to use.
 */
enum class PushPlatform(val wire: String) {
    Android("android"),
    Ios("ios"),
}
