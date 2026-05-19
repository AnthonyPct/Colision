package com.anthooop.colision.core.common

import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.core.database.entity.MemberEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Resolves the local member row that belongs to the current Supabase auth user
 * on this device. The MVP convention (cf. ActiveProjectProvider) is that a
 * device has one active project at a time, so a single member row matches
 * `member.deviceId = auth.user.id`.
 *
 * The device id is sourced from `supabase.auth.sessionStatus` so the flow
 * re-emits when the anonymous sign-in completes after app start.
 */
interface CurrentMemberProvider {
    fun observe(): Flow<MemberEntity?>
    suspend fun current(): MemberEntity?
}

class DefaultCurrentMemberProvider(
    private val supabase: SupabaseClient,
    private val memberDao: MemberDao,
) : CurrentMemberProvider {

    private val deviceIdFlow: Flow<String?> = supabase.auth.sessionStatus
        .map { supabase.auth.currentUserOrNull()?.id }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<MemberEntity?> =
        deviceIdFlow.flatMapLatest { id ->
            if (id == null) flowOf(null) else memberDao.observeOwnMember(id)
        }

    override suspend fun current(): MemberEntity? {
        val id = deviceIdFlow.first() ?: return null
        return memberDao.observeOwnMember(id).first()
    }
}
