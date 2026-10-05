package com.example.model

enum class PartitionDangerLevel {
    SAFE,       // e.g. boot, recovery, dtbo
    MODERATE,   // e.g. vendor_boot, init_boot, super, system
    CRITICAL    // e.g. vbmeta, persist, efs, abl, xbl, modem, radio
}

data class PartitionSpec(
    val name: String,
    val displayName: String,
    val description: String,
    val dangerLevel: PartitionDangerLevel,
    val dangerWarning: String? = null,
    val recommendedBackup: Boolean = true
) {
    val isDangerous: Boolean
        get() = dangerLevel == PartitionDangerLevel.CRITICAL
}

object SupportedPartitions {
    val ALL = listOf(
        PartitionSpec(
            name = "boot",
            displayName = "Boot Kernel (boot.img)",
            description = "Linux kernel, dtb, and initial ramdisk image",
            dangerLevel = PartitionDangerLevel.SAFE,
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "init_boot",
            displayName = "Generic Ramdisk (init_boot.img)",
            description = "Android 13+ GKI 2.0 generic init ramdisk",
            dangerLevel = PartitionDangerLevel.SAFE,
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "vendor_boot",
            displayName = "Vendor Boot (vendor_boot.img)",
            description = "Device-specific hardware modules & fastbootd ramdisk",
            dangerLevel = PartitionDangerLevel.MODERATE,
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "recovery",
            displayName = "Recovery (recovery.img)",
            description = "AOSP/TWRP recovery system environment",
            dangerLevel = PartitionDangerLevel.SAFE,
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "dtbo",
            displayName = "Device Tree Blob Overlay (dtbo.img)",
            description = "SoC hardware pinout & board configuration overlay",
            dangerLevel = PartitionDangerLevel.SAFE,
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "vbmeta",
            displayName = "AVB Verified Boot (vbmeta.img)",
            description = "Android Verified Boot cryptographic digest & hash tree",
            dangerLevel = PartitionDangerLevel.CRITICAL,
            dangerWarning = "Flashing a corrupt or incorrectly signed vbmeta can trigger dm-verity bootloops or trigger bootloader security lockout.",
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "vbmeta_system",
            displayName = "AVB System Hash (vbmeta_system.img)",
            description = "Verified Boot hash tree for system/product partitions",
            dangerLevel = PartitionDangerLevel.MODERATE,
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "vbmeta_vendor",
            displayName = "AVB Vendor Hash (vbmeta_vendor.img)",
            description = "Verified Boot hash tree for vendor/odm partitions",
            dangerLevel = PartitionDangerLevel.MODERATE,
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "super",
            displayName = "Dynamic Partitions (super.img)",
            description = "Container for system, vendor, product, odm logical images",
            dangerLevel = PartitionDangerLevel.MODERATE,
            recommendedBackup = false
        ),
        PartitionSpec(
            name = "persist",
            displayName = "Hardware Calibration (persist.img)",
            description = "Touchscreen, camera calibration & Bluetooth/Wi-Fi MAC",
            dangerLevel = PartitionDangerLevel.CRITICAL,
            dangerWarning = "Overwriting persist with another device's dump permanently damages hardware calibration parameters.",
            recommendedBackup = true
        ),
        PartitionSpec(
            name = "efs",
            displayName = "Modem NVRAM / EFS (efs.img)",
            description = "Baseband calibration & IMEI security partitions",
            dangerLevel = PartitionDangerLevel.CRITICAL,
            dangerWarning = "Loss of EFS leads to null IMEI and inability to connect to cellular networks.",
            recommendedBackup = true
        )
    )

    fun find(partitionName: String): PartitionSpec {
        val clean = partitionName.lowercase().removeSuffix("_a").removeSuffix("_b")
        return ALL.firstOrNull { it.name == clean }
            ?: PartitionSpec(
                name = clean,
                displayName = "$clean.img",
                description = "Custom or vendor-specific partition image",
                dangerLevel = PartitionDangerLevel.MODERATE,
                recommendedBackup = false
            )
    }
}
