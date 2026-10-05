package com.example.data

import com.example.data.entity.BackupRecordEntity
import com.example.data.entity.DeviceProfileEntity
import com.example.data.entity.FlashHistoryEntity
import kotlinx.coroutines.flow.Flow

class RomFlashRepository(private val database: AppDatabase) {

    val allHistory: Flow<List<FlashHistoryEntity>> = database.flashHistoryDao().getAllHistory()
    val allBackups: Flow<List<BackupRecordEntity>> = database.backupRecordDao().getAllBackups()
    val allProfiles: Flow<List<DeviceProfileEntity>> = database.deviceProfileDao().getAllProfiles()

    fun getHistoryByStatus(status: String): Flow<List<FlashHistoryEntity>> =
        database.flashHistoryDao().getHistoryByStatus(status)

    fun getHistoryByDevice(deviceModel: String): Flow<List<FlashHistoryEntity>> =
        database.flashHistoryDao().getHistoryByDevice(deviceModel)

    suspend fun recordFlashHistory(
        operationType: String,
        targetName: String,
        deviceModel: String,
        filePath: String,
        status: String,
        durationMs: Long = 0,
        bytesProcessed: Long = 0,
        summary: String = "",
        errorMessage: String? = null,
        auditChecksum: String? = null
    ): Long {
        return database.flashHistoryDao().insertHistory(
            FlashHistoryEntity(
                operationType = operationType,
                targetName = targetName,
                deviceModel = deviceModel,
                filePath = filePath,
                status = status,
                durationMs = durationMs,
                bytesProcessed = bytesProcessed,
                summary = summary,
                errorMessage = errorMessage,
                auditChecksum = auditChecksum
            )
        )
    }

    suspend fun deleteHistoryById(id: Long) {
        database.flashHistoryDao().deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        database.flashHistoryDao().clearHistory()
    }

    suspend fun saveDeviceProfile(profile: DeviceProfileEntity) {
        database.deviceProfileDao().saveProfile(profile)
    }

    suspend fun recordBackup(
        partitionName: String,
        deviceProduct: String,
        filePath: String,
        sizeBytes: Long,
        sha256: String
    ): Long {
        return database.backupRecordDao().insertBackup(
            BackupRecordEntity(
                partitionName = partitionName,
                deviceProduct = deviceProduct,
                filePath = filePath,
                sizeBytes = sizeBytes,
                sha256Checksum = sha256
            )
        )
    }

    suspend fun deleteBackup(id: Long) {
        database.backupRecordDao().deleteBackup(id)
    }
}
