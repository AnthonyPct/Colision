package com.anthooop.colision.feature.projecthub.data

import com.anthooop.colision.core.database.dao.MemberCommissionDao
import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.core.database.dao.ProjectDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class DefaultProjectLifecycleRepository(
    private val supabase: SupabaseClient,
    private val projectDao: ProjectDao,
    private val memberDao: MemberDao,
    private val memberCommissionDao: MemberCommissionDao,
) : ProjectLifecycleRepository {

    /**
     * FR5 + FR42: the user releases their identity on the active project.
     * Server-side: null out `member.device_id` on the row that owns the
     * current Supabase auth.uid() so future syncs don't re-claim it; drop
     * the member's commission assignments (cascade from member is on
     * member_commission already, but we clear them explicitly so a future
     * "claim again" doesn't inherit stale picks). Arbitration rows cascade
     * server-side from the `device_id = null` update via FK rules.
     * Locally: delete the project so the AppViewModel sends the device
     * back to the Welcome screen.
     */
    override suspend fun leaveProject(projectId: String): Result<Unit> = runCatching {
        val deviceId = supabase.auth.currentUserOrNull()?.id
            ?: error("No anonymous session — cannot leave project")
        val ownMember = memberDao.findOwnByProject(projectId, deviceId)
        if (ownMember != null) {
            // 1. detach from commissions
            memberCommissionDao.deleteAllForMember(ownMember.id)
            supabase.from("member_commission").delete {
                filter { eq("member_id", ownMember.id) }
            }
            // 2. release the identity on the server so the row remains
            //    available for the historical attribution of past meetings
            //    but is no longer tied to this device.
            supabase.from("member").update(MemberDeviceUpdateDto(deviceId = null)) {
                filter { eq("id", ownMember.id) }
            }
            memberDao.update(ownMember.copy(deviceId = null))
        }
        // 3. drop the project from the local cache so the start-graph
        //    decision flips to onboarding.
        projectDao.deleteById(projectId)
    }

    /**
     * FR43: any member can delete the entire project. The single DELETE on
     * the `project` row cascades server-side (FKs on commission, member,
     * meeting, meeting_commission, arbitration are ON DELETE CASCADE), so
     * one call clears the whole graph for every member.
     */
    override suspend fun deleteProject(projectId: String): Result<Unit> = runCatching {
        supabase.from("project").delete {
            filter { eq("id", projectId) }
        }
        projectDao.deleteById(projectId)
    }
}
