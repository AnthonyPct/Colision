package com.anthooop.colision.feature.projecthub.data

import com.anthooop.colision.core.database.dao.ProjectDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class DefaultProjectLifecycleRepository(
    private val supabase: SupabaseClient,
    private val projectDao: ProjectDao,
) : ProjectLifecycleRepository {

    /**
     * Implemented in story 2.8 — for now this only purges the local cache so
     * the device returns to the Welcome screen. The server-side `member`
     * row's device_id reset is wired alongside the device id story.
     */
    override suspend fun leaveProject(projectId: String): Result<Unit> = runCatching {
        // TODO(story 2.8): null out member.device_id, remove member_commission, delete arbitration.
        projectDao.deleteById(projectId)
    }

    /**
     * Implemented in story 2.9. The single Postgres `delete project where id`
     * call cascades to commissions/members/meetings/arbitrations.
     */
    override suspend fun deleteProject(projectId: String): Result<Unit> = runCatching {
        supabase.from("project").delete {
            filter { eq("id", projectId) }
        }
        projectDao.deleteById(projectId)
    }
}
