package com.anthooop.colision.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.anthooop.colision.core.database.dao.CommissionDao
import com.anthooop.colision.core.database.dao.MeetingDao
import com.anthooop.colision.core.database.dao.MemberCommissionDao
import com.anthooop.colision.core.database.dao.MemberDao
import com.anthooop.colision.core.database.dao.ProjectDao
import com.anthooop.colision.core.database.entity.CommissionEntity
import com.anthooop.colision.core.database.entity.MeetingCommissionEntity
import com.anthooop.colision.core.database.entity.MeetingEntity
import com.anthooop.colision.core.database.entity.MemberCommissionEntity
import com.anthooop.colision.core.database.entity.MemberEntity
import com.anthooop.colision.core.database.entity.ProjectEntity

@Database(
    entities = [
        ProjectEntity::class,
        CommissionEntity::class,
        MemberEntity::class,
        MemberCommissionEntity::class,
        MeetingEntity::class,
        MeetingCommissionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(ColisionDatabaseConstructor::class)
abstract class ColisionDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun commissionDao(): CommissionDao
    abstract fun memberDao(): MemberDao
    abstract fun memberCommissionDao(): MemberCommissionDao
    abstract fun meetingDao(): MeetingDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ColisionDatabaseConstructor : RoomDatabaseConstructor<ColisionDatabase> {
    override fun initialize(): ColisionDatabase
}

const val COLISION_DB_FILE = "colision.db"
