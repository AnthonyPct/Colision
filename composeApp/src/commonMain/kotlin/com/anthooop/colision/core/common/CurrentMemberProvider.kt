package com.anthooop.colision.core.common

import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.core.database.entity.MemberEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * Resolves the local member row that belongs to the current Supabase auth user
 * on this device. The MVP convention (cf. ActiveProjectProvider) is that a
 * device has one active project at a time, so a single member row matches
 * `member.deviceId = current device row's primary key`.
 *
 * `deviceId()` returns the **`public.device.id`** uuid, **not** `auth.users.id`.
 * The server-side `current_device_id()` RLS helper resolves to the former (it
 * looks up `device` by `auth_user_id = auth.uid()` and returns `device.id`);
 * the Kotlin client used to send `auth.user.id` as `member.device_id` and that
 * mismatch was rejected by the `member claim-an-unclaimed-row` policy with a
 * 42501.
 */
interface CurrentMemberProvider {
    fun observe(): Flow<MemberEntity?>
    suspend fun current(): MemberEntity?

    /** Returns `public.device.id` for the current auth session, or null. */
    suspend fun deviceId(): String?
}

@Serializable
private data class DeviceRowDto(val id: String)

class DefaultCurrentMemberProvider(
    private val supabase: SupabaseClient,
    private val memberDao: MemberDao,
) : CurrentMemberProvider {

    private val deviceIdMutex = Mutex()

    /**
     * Cached `device.id` for the current auth session. Reset whenever
     * `currentUserOrNull()?.id` changes (e.g. on sign-out / re-sign-in).
     */
    private var cachedAuthUserId: String? = null
    private var cachedDeviceId: String? = null

    private val deviceIdFlow: Flow<String?> = supabase.auth.sessionStatus
        .map { deviceId() }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<MemberEntity?> =
        deviceIdFlow.flatMapLatest { id ->
            if (id == null) flowOf(null) else memberDao.observeOwnMember(id)
        }

    override suspend fun current(): MemberEntity? {
        val id = deviceId() ?: return null
        return memberDao.observeOwnMember(id).first()
    }

    override suspend fun deviceId(): String? {
        val authUserId = supabase.auth.currentUserOrNull()?.id ?: run {
            // No session — invalidate any stale cache and bail.
            deviceIdMutex.withLock {
                cachedAuthUserId = null
                cachedDeviceId = null
            }
            return null
        }
        deviceIdMutex.withLock {
            if (cachedAuthUserId == authUserId && cachedDeviceId != null) {
                return cachedDeviceId
            }
            // The RLS policy `device sees its own row` permits this read
            // for the current auth user. The device table has a UNIQUE
            // constraint on auth_user_id so at most one row matches.
            val resolved = runCatching {
                supabase.from("device")
                    .select(Columns.list("id"))
                    .decodeSingleOrNull<DeviceRowDto>()
                    ?.id
            }.getOrNull()
            cachedAuthUserId = authUserId
            cachedDeviceId = resolved
            return resolved
        }
    }
}
