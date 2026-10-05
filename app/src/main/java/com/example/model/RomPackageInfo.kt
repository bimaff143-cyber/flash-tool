package com.example.model

enum class RomPackageType {
    FASTBOOT_PACKAGE,
    RECOVERY_OTA_ZIP,
    PAYLOAD_BIN,
    STANDALONE_IMAGE,
    UNKNOWN
}

data class RomPartitionItem(
    val partitionName: String,
    val fileName: String,
    val sizeBytes: Long,
    val isDangerous: Boolean,
    val checksum: String? = null,
    val md5Checksum: String? = null,
    val isChecksumVerified: Boolean = true
)

data class RomPackageInfo(
    val fileName: String,
    val sizeBytes: Long,
    val type: RomPackageType,
    val requiredProduct: String? = null,
    val isCompatible: Boolean = true,
    val compatibilityMessage: String = "Ready to inspect",
    val partitionItems: List<RomPartitionItem> = emptyList(),
    val hasAndroidInfo: Boolean = false,
    val hasFlashAllScript: Boolean = false,
    val manifestChecksumsVerified: Boolean = true
)

data class ImageInspectionResult(
    val fileName: String,
    val targetPartition: String,
    val sizeBytes: Long,
    val sha256: String,
    val md5: String = "N/A",
    val headerMagic: String,
    val headerVersion: Int = 0,
    val kernelSize: Long = 0,
    val ramdiskSize: Long = 0,
    val osVersion: String = "N/A",
    val isAvb: Boolean = false,
    val verityDisabled: Boolean = false,
    val verificationDisabled: Boolean = false,
    val isDangerous: Boolean = false,
    val dangerWarning: String? = null,
    val isChecksumVerified: Boolean = true,
    val checksumVerificationMessage: String = "Verified against manifest"
)
