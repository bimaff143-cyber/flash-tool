package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BackupRecordDao
import com.example.data.dao.DeviceProfileDao
import com.example.data.dao.FlashHistoryDao
import com.example.data.entity.BackupRecordEntity
import com.example.data.entity.DeviceProfileEntity
import com.example.data.entity.FlashHistoryEntity

@Database(
    entities = [
        FlashHistoryEntity::class,
        DeviceProfileEntity::class,
        BackupRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashHistoryDao(): FlashHistoryDao
    abstract fun deviceProfileDao(): DeviceProfileDao
    abstract fun backupRecordDao(): BackupRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rom_flash_tool.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
