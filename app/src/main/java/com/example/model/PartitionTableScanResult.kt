package com.example.model

enum class PartitionCategory(val displayName: String, val colorHex: Long) {
    BOOT_KERNEL("Boot & Kernel", 0xFF00E5FF),
    DYNAMIC_SUPER("Dynamic Super OS", 0xFF818CF8),
    SECURITY_AVB("Security & AVB", 0xFFFFB300),
    FIRMWARE("Firmware & Radio", 0xFF10B981),
    USERDATA("User Storage", 0xFFFF7043)
}

data class PartitionBlockInfo(
    val name: String,
    val sizeBytes: Long,
    val category: PartitionCategory,
    val slot: String? = null,
    val isCritical: Boolean = false
) {
    val formattedSize: String
        get() {
            return when {
                sizeBytes >= 1024L * 1024L * 1024L -> String.format(java.util.Locale.US, "%.1f GB", sizeBytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
                sizeBytes >= 1024L * 1024L -> String.format(java.util.Locale.US, "%.1f MB", sizeBytes.toDouble() / (1024.0 * 1024.0))
                sizeBytes >= 1024L -> String.format(java.util.Locale.US, "%.1f KB", sizeBytes.toDouble() / 1024.0)
                else -> "$sizeBytes B"
            }
        }
}

data class StorageLayoutScanResult(
    val deviceName: String,
    val totalCapacityBytes: Long,
    val partitions: List<PartitionBlockInfo>,
    val scanTimestamp: Long = System.currentTimeMillis()
) {
    val formattedTotalCapacity: String
        get() = String.format(java.util.Locale.US, "%.1f GB", totalCapacityBytes.toDouble() / (1024.0 * 1024.0 * 1024.0))

    companion object {
        fun defaultSampleLayout(deviceProduct: String = "cheetah"): StorageLayoutScanResult {
            val list = listOf(
                PartitionBlockInfo("boot_a", 64L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "a", false),
                PartitionBlockInfo("boot_b", 64L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "b", false),
                PartitionBlockInfo("init_boot_a", 8L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "a", false),
                PartitionBlockInfo("init_boot_b", 8L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "b", false),
                PartitionBlockInfo("vendor_boot_a", 64L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "a", false),
                PartitionBlockInfo("vendor_boot_b", 64L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "b", false),
                PartitionBlockInfo("dtbo_a", 8L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "a", false),
                PartitionBlockInfo("dtbo_b", 8L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "b", false),
                PartitionBlockInfo("vbmeta_a", 64L * 1024, PartitionCategory.SECURITY_AVB, "a", true),
                PartitionBlockInfo("vbmeta_b", 64L * 1024, PartitionCategory.SECURITY_AVB, "b", true),
                PartitionBlockInfo("vbmeta_system_a", 64L * 1024, PartitionCategory.SECURITY_AVB, "a", true),
                PartitionBlockInfo("vbmeta_system_b", 64L * 1024, PartitionCategory.SECURITY_AVB, "b", true),
                PartitionBlockInfo("recovery_a", 64L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "a", false),
                PartitionBlockInfo("recovery_b", 64L * 1024 * 1024, PartitionCategory.BOOT_KERNEL, "b", false),
                PartitionBlockInfo("modem_a", 128L * 1024 * 1024, PartitionCategory.FIRMWARE, "a", false),
                PartitionBlockInfo("modem_b", 128L * 1024 * 1024, PartitionCategory.FIRMWARE, "b", false),
                PartitionBlockInfo("super", 9500L * 1024 * 1024, PartitionCategory.DYNAMIC_SUPER, null, true),
                PartitionBlockInfo("metadata", 16L * 1024 * 1024, PartitionCategory.USERDATA, null, false),
                PartitionBlockInfo("userdata", 112L * 1024 * 1024 * 1024, PartitionCategory.USERDATA, null, true)
            )
            val total = list.sumOf { it.sizeBytes }
            return StorageLayoutScanResult(
                deviceName = deviceProduct,
                totalCapacityBytes = total,
                partitions = list
            )
        }
    }
}
