package com.anthooop.colision.core.common

import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the "never mint a new anonymous identity while one is already
 * persisted" contract of [SupabaseAnonymousAuthManager]. The regression these
 * tests lock down is the ghost lockout (incidents 2026-07-16 / 2026-07-22):
 * a failed refresh on flaky connectivity used to fall through to
 * signInAnonymously(), orphaning the claimed member.device_id.
 */
class SupabaseAnonymousAuthManagerTest {

    private fun manager(gateway: AuthSessionGateway) =
        SupabaseAnonymousAuthManager(
            gateway = gateway,
            logger = NoopLogger,
            crashReporter = NoopCrashReporter(),
        )

    @Test
    fun `authenticated session is a no-op`() = runTest {
        val gateway = FakeAuthSessionGateway(AuthStatus.Authenticated, storedSession = true)

        val result = manager(gateway).ensureSession()

        assertTrue(result.isSuccess)
        assertEquals(0, gateway.signInCount)
        assertEquals(0, gateway.recoverCount)
        assertEquals(0, gateway.refreshCount)
    }

    @Test
    fun `genuine first run signs in anonymously exactly once`() = runTest {
        val gateway = FakeAuthSessionGateway(AuthStatus.NoSession, storedSession = false)

        val result = manager(gateway).ensureSession()

        assertTrue(result.isSuccess)
        assertEquals(1, gateway.signInCount)
        assertEquals(0, gateway.recoverCount)
    }

    @Test
    fun `no active session but a stored identity is recovered, never re-signed`() = runTest {
        val gateway = FakeAuthSessionGateway(AuthStatus.NoSession, storedSession = true)

        val result = manager(gateway).ensureSession()

        assertTrue(result.isSuccess)
        assertEquals(0, gateway.signInCount) // <- the ghost-prevention guarantee
        assertEquals(1, gateway.recoverCount)
    }

    @Test
    fun `refresh failure recovers the identity and never signs in anonymously`() = runTest {
        val gateway = FakeAuthSessionGateway(AuthStatus.RefreshFailure, storedSession = true)

        val result = manager(gateway).ensureSession()

        assertTrue(result.isSuccess)
        assertEquals(0, gateway.signInCount) // <- core regression: flaky network must NOT drift
        assertEquals(1, gateway.refreshCount)
    }

    @Test
    fun `refresh failure that cannot recover keeps the identity and does not sign in`() = runTest {
        val gateway = FakeAuthSessionGateway(AuthStatus.RefreshFailure, storedSession = true).apply {
            refreshResult = Result.failure(IllegalStateException("offline"))
        }

        val result = manager(gateway).ensureSession()

        assertTrue(result.isFailure) // soft failure — caller retries next foreground
        assertEquals(0, gateway.signInCount) // identity preserved, no new ghost minted
        assertEquals(1, gateway.refreshCount)
    }

    @Test
    fun `two concurrent callers on first run yield a single anonymous sign-in`() = runTest {
        // Bug #66: AppViewModel.init and ProjectSyncManager both call ensureSession().
        val gateway = FakeAuthSessionGateway(AuthStatus.NoSession, storedSession = false)
        val mgr = manager(gateway)

        val a = async { mgr.ensureSession() }
        val b = async { mgr.ensureSession() }
        a.await()
        b.await()

        assertEquals(1, gateway.signInCount)
    }
}

/**
 * In-memory [AuthSessionGateway]. Successful auth operations flip [status] to
 * [AuthStatus.Authenticated] so the read-then-act path behaves like the real
 * one; each op result can be overridden to simulate failures.
 */
private class FakeAuthSessionGateway(
    initialStatus: AuthStatus,
    storedSession: Boolean,
) : AuthSessionGateway {

    private var currentStatus: AuthStatus = initialStatus
    private var storedSession: Boolean = storedSession

    var signInCount = 0
        private set
    var recoverCount = 0
        private set
    var refreshCount = 0
        private set

    var signInResult: Result<Unit> = Result.success(Unit)
    var recoverResult: Result<Unit> = Result.success(Unit)
    var refreshResult: Result<Unit> = Result.success(Unit)

    override suspend fun awaitInitialization() = Unit

    override fun status(): AuthStatus = currentStatus

    override suspend fun hasStoredSession(): Boolean = storedSession

    override suspend fun recoverStoredSession(): Result<Unit> {
        recoverCount++
        return recoverResult.onSuccess { currentStatus = AuthStatus.Authenticated }
    }

    override suspend fun signInAnonymously(): Result<Unit> {
        signInCount++
        return signInResult.onSuccess {
            currentStatus = AuthStatus.Authenticated
            storedSession = true
        }
    }

    override suspend fun refreshCurrentSession(): Result<Unit> {
        refreshCount++
        return refreshResult.onSuccess { currentStatus = AuthStatus.Authenticated }
    }
}

private object NoopLogger : Logger {
    override fun debug(tag: String, message: String, throwable: Throwable?) = Unit
    override fun info(tag: String, message: String) = Unit
    override fun warn(tag: String, message: String, throwable: Throwable?) = Unit
    override fun error(tag: String, message: String, throwable: Throwable?) = Unit
}
