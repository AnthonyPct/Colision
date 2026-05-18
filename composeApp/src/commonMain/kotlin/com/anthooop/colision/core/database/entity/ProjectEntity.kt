package com.anthooop.colision.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shareCode: String,
    val createdAt: String,
    val updatedAt: String,
)
