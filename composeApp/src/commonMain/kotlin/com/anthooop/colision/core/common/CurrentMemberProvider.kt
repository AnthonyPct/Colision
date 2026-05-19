package com.anthooop.colision.core.common

import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.core.database.entity.MemberEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Resolves the local member row that belongs to the current Supabase auth user
 * on this device. The MVP convention (cf. ActiveProjectProvider) is that a
 * device has one active project at a time, so a single member row matches
 * `member.deviceId = auth.user.id`.
 */
interface CurrentMemberProvider {
    fun observe(): Flow<MemberEntity?>
    suspend fun current(): MemberEntity?
}

class DefaultCurrentMemberProvider(
    private val supabase: SupabaseClient,
    private val memberDao: MemberDao,
) : CurrentMemberProvider {

    private val deviceId: MutableStateFlow<String?> = MutableStateFlow(
        supabase.auth.currentUserOrNull()?.id,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<MemberEntity?> {
        val id = supabase.auth.currentUserOrNull()?.id
        if (id != deviceId.value) deviceId.value = id
        return deviceId.flatMapLatest { current ->
            if (current == null) flowOf(null) else memberDao.observeOwnMember(current)
        }
    }

    override suspend fun current(): MemberEntity? {
        val id = supabase.auth.currentUserOrNull()?.id ?: return null
        return memberDao.observeOwnMember(id).first()
    }
}
