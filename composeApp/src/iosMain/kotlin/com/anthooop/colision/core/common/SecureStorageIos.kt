package com.anthooop.colision.core.common

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.OSStatus

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
class SecureStorageIos(
    private val service: String = "com.anthooop.colision",
) : SecureStorage {

    override fun get(key: String): String? = memScoped {
        val query = baseQuery(key)
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status: OSStatus = SecItemCopyMatching(query, result.ptr)
        if (status == errSecItemNotFound) return null
        if (status != errSecSuccess) return null

        val data = result.value as CFDataRef? ?: return null
        val nsData = CFBridgingRelease(data) as NSData
        val ns = NSString.create(nsData, NSUTF8StringEncoding) ?: return null
        @Suppress("CAST_NEVER_SUCCEEDS")
        return (ns as String)
    }

    override fun put(key: String, value: String) {
        remove(key)
        memScoped {
            val query = baseQuery(key)
            val ns = NSString.create(string = value)
            val data = ns.dataUsingEncoding(NSUTF8StringEncoding) ?: return
            CFDictionaryAddValue(query, kSecValueData, CFBridgingRetain(data))
            CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
            SecItemAdd(query, null)
        }
    }

    override fun remove(key: String) {
        memScoped {
            val query = baseQuery(key)
            SecItemDelete(query)
        }
    }

    override fun clear() {
        // Keychain Services does not expose a "delete all items for this
        // service" primitive cleanly; explicit remove() on known keys is
        // the convention. Story 1.8 only persists JWT + refresh token,
        // so callers explicitly remove those.
    }

    private fun baseQuery(key: String) = CFDictionaryCreateMutable(
        kCFAllocatorDefault,
        0,
        kCFTypeDictionaryKeyCallBacks.ptr,
        kCFTypeDictionaryValueCallBacks.ptr,
    ).apply {
        CFDictionaryAddValue(this, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(this, kSecAttrService, CFBridgingRetain(service))
        CFDictionaryAddValue(this, kSecAttrAccount, CFBridgingRetain(key))
    }

    private fun CFBridgingRetain(value: Any): CFTypeRef? =
        platform.Foundation.CFBridgingRetain(value) as CFTypeRef?
}
