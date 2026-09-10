package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.FzEngineDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.LicenseEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.SellerEntity
import com.example.data.local.entity.SessionEntity

@Database(
    entities = [
        LicenseEntity::class,
        SellerEntity::class,
        SessionEntity::class,
        AuditLogEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FzDatabase : RoomDatabase() {
    abstract fun fzEngineDao(): FzEngineDao

    companion object {
        @Volatile
        private var INSTANCE: FzDatabase? = null

        fun getDatabase(context: Context): FzDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FzDatabase::class.java,
                    "fz_engine_secure.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
