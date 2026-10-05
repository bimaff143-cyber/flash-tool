package com.example.model

enum class LogLevel {
    INFO,
    FASTBOOT,
    ADB,
    PYTHON,
    WARN,
    ERROR,
    SUCCESS
}

data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val tag: String = "Engine",
    val message: String
)

enum class FlashOperationType {
    FASTBOOT_FULL_ROM,
    CUSTOM_BOOT_IMAGE,
    RECOVERY_FLASH,
    ADB_SIDELOAD_ZIP,
    PARTITION_BACKUP,
    PARTITION_RESTORE,
    SYSTEM_REBOOT
}

data class FlashProgressState(
    val isRunning: Boolean = false,
    val isSuccess: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val currentAction: String = "Idle",
    val currentPartition: String? = null,
    val bytesProcessed: Long = 0,
    val totalBytes: Long = 0,
    val speedMbPerSec: Double = 0.0,
    val percentage: Float = 0f
)
