package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flash_history")
data class FlashHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationType: String, // FASTBOOT_ROM, CUSTOM_BOOT_IMAGE, RECOVERY_ROM, ADB_SIDELOAD, RESTORE_PARTITION
    val targetName: String, // e.g. "boot.img", "lineage-21.0-fuxi.zip"
    val deviceModel: String, // e.g. "Pixel 7 Pro (cheetah)"
    val filePath: String, // e.g. "/sdcard/Download/boot.img"
    val timestamp: Long = System.currentTimeMillis(), // Audit timestamp in epoch ms
    val status: String, // "SUCCESS" or "FAILED"
    val durationMs: Long = 0,
    val bytesProcessed: Long = 0,
    val summary: String = "",
    val errorMessage: String? = null,
    val auditChecksum: String? = null // SHA-256 / MD5 digest for cryptographic audit
)
