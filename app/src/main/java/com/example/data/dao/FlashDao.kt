package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BackupRecordEntity
import com.example.data.entity.DeviceProfileEntity
import com.example.data.entity.FlashHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashHistoryDao {
    @Query("SELECT * FROM flash_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<FlashHistoryEntity>>

    @Query("SELECT * FROM flash_history WHERE status = :status ORDER BY timestamp DESC")
    fun getHistoryByStatus(status: String): Flow<List<FlashHistoryEntity>>

    @Query("SELECT * FROM flash_history WHERE deviceModel LIKE '%' || :deviceModel || '%' ORDER BY timestamp DESC")
    fun getHistoryByDevice(deviceModel: String): Flow<List<FlashHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: FlashHistoryEntity): Long

    @Query("DELETE FROM flash_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM flash_history")
    suspend fun clearHistory()
}

@Dao
interface DeviceProfileDao {
    @Query("SELECT * FROM device_profiles ORDER BY lastSeenTimestamp DESC")
    fun getAllProfiles(): Flow<List<DeviceProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: DeviceProfileEntity)

    @Query("SELECT * FROM device_profiles WHERE serialNumber = :serial LIMIT 1")
    suspend fun getProfile(serial: String): DeviceProfileEntity?
}

@Dao
interface BackupRecordDao {
    @Query("SELECT * FROM backup_records ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<BackupRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupRecordEntity): Long

    @Query("DELETE FROM backup_records WHERE id = :id")
    suspend fun deleteBackup(id: Long)
}
