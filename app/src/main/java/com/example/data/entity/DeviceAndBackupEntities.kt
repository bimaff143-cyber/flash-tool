package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_profiles")
data class DeviceProfileEntity(
    @PrimaryKey
    val serialNumber: String,
    val productName: String,
    val isUnlocked: Boolean,
    val currentSlot: String,
    val maxDownloadSize: Long,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "backup_records")
data class BackupRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partitionName: String,
    val deviceProduct: String,
    val filePath: String,
    val sizeBytes: Long,
    val sha256Checksum: String,
    val timestamp: Long = System.currentTimeMillis()
)
