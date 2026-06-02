package com.anthooop.colision.feature.projecthub.data

/**
 * Operations that change the relationship between the device and a project:
 * leaving (story 2.8) and deleting (story 2.9). Implementations live in
 * stories 2.8 / 2.9 — story 2.3 only references the type so the
 * ProjectSettingsViewModel can be wired through Koin from the start.
 */
interface ProjectLifecycleRepository {
    suspend fun leaveProject(projectId: String): Result<Unit>
    suspend fun deleteProject(projectId: String): Result<Unit>
}
