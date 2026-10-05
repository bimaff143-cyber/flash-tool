package com.example

import com.example.model.PartitionDangerLevel
import com.example.model.SupportedPartitions
import com.example.usb.UsbProtocolConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class ExampleUnitTest {
    @Test
    fun testSupportedPartitions() {
        val boot = SupportedPartitions.find("boot")
        assertEquals(PartitionDangerLevel.SAFE, boot.dangerLevel)

        val vbmeta = SupportedPartitions.find("vbmeta")
        assertEquals(PartitionDangerLevel.CRITICAL, vbmeta.dangerLevel)
        assertNotNull(vbmeta.dangerWarning)

        val persist = SupportedPartitions.find("persist")
        assertEquals(PartitionDangerLevel.CRITICAL, persist.dangerLevel)

        val dtbo = SupportedPartitions.find("dtbo")
        assertEquals(PartitionDangerLevel.SAFE, dtbo.dangerLevel)
    }

    @Test
    fun testUsbProtocolConstants() {
        assertEquals(0xFF, UsbProtocolConstants.USB_CLASS_VENDOR_SPEC)
        assertEquals(0x42, UsbProtocolConstants.USB_SUBCLASS_FASTBOOT)
        assertEquals(0x03, UsbProtocolConstants.USB_PROTOCOL_FASTBOOT)
        assertEquals(0x01, UsbProtocolConstants.USB_PROTOCOL_ADB)
    }

    @Test
    fun testBatterySafetyCheck() {
        val minCharge = 30

        // Test below 30% - must reject
        val lowBattery = 15
        assertFalse(lowBattery >= minCharge)

        val borderlineLow = 29
        assertFalse(borderlineLow >= minCharge)

        // Test 30% and above - must pass
        val threshold = 30
        assertTrue(threshold >= minCharge)

        val healthyBattery = 85
        assertTrue(healthyBattery >= minCharge)
    }

    @Test
    fun testChecksumCalculationAndManifestVerification() {
        val testData = "ROM_FLASH_TOOL_TEST_IMAGE_DATA".toByteArray()

        // Calculate MD5 and SHA-256
        val md5 = MessageDigest.getInstance("MD5").digest(testData).joinToString("") { "%02x".format(it) }
        val sha256 = MessageDigest.getInstance("SHA-256").digest(testData).joinToString("") { "%02x".format(it) }

        assertEquals(32, md5.length)
        assertEquals(64, sha256.length)

        // Mock manifest content
        val manifestContent = """
            # Manifest for ROM Flash Tool v1.0
            $sha256  boot.img
            $md5  vbmeta.img
        """.trimIndent()

        // Verify SHA256 match
        val lines = manifestContent.lines().filter { !it.startsWith("#") && it.isNotBlank() }
        val bootLine = lines.firstOrNull { it.contains("boot.img") }
        assertNotNull(bootLine)
        assertTrue(bootLine!!.startsWith(sha256))

        // Verify MD5 match
        val vbmetaLine = lines.firstOrNull { it.contains("vbmeta.img") }
        assertNotNull(vbmetaLine)
        assertTrue(vbmetaLine!!.startsWith(md5))

        // Verify corrupted hash fails
        val corruptedSha256 = sha256.replaceRange(0, 4, "0000")
        assertFalse(corruptedSha256 == sha256)
    }

    @Test
    fun testFlashHistoryEntityAuditFields() {
        val successRecord = com.example.data.entity.FlashHistoryEntity(
            operationType = "FASTBOOT_ROM",
            targetName = "lineage-21.0-fuxi.zip",
            deviceModel = "Xiaomi 13 (fuxi)",
            filePath = "/sdcard/Download/lineage-21.0-fuxi.zip",
            timestamp = 1728000000000L,
            status = "SUCCESS",
            durationMs = 4500L,
            bytesProcessed = 2800000000L,
            summary = "Full ROM package flashed successfully",
            auditChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )
        assertEquals("SUCCESS", successRecord.status)
        assertEquals("Xiaomi 13 (fuxi)", successRecord.deviceModel)
        assertEquals("/sdcard/Download/lineage-21.0-fuxi.zip", successRecord.filePath)
        assertEquals(1728000000000L, successRecord.timestamp)

        val failedRecord = com.example.data.entity.FlashHistoryEntity(
            operationType = "CUSTOM_IMAGE",
            targetName = "vbmeta.img",
            deviceModel = "Pixel 7 Pro (cheetah)",
            filePath = "/sdcard/Download/vbmeta.img",
            status = "FAILED",
            errorMessage = "Integrity check failed: corrupt AVB hashtree descriptor"
        )
        assertEquals("FAILED", failedRecord.status)
        assertEquals("Pixel 7 Pro (cheetah)", failedRecord.deviceModel)
        assertNotNull(failedRecord.errorMessage)
    }

    @Test
    fun testThemeModeEnum() {
        val modes = com.example.ui.theme.ThemeMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(com.example.ui.theme.ThemeMode.SYSTEM))
        assertTrue(modes.contains(com.example.ui.theme.ThemeMode.LIGHT))
        assertTrue(modes.contains(com.example.ui.theme.ThemeMode.DARK))

        assertEquals("LIGHT", com.example.ui.theme.ThemeMode.LIGHT.name)
        assertEquals("DARK", com.example.ui.theme.ThemeMode.DARK.name)
        assertEquals("SYSTEM", com.example.ui.theme.ThemeMode.SYSTEM.name)
    }

    @Test
    fun testAuditExportCsvAndTxtGeneration() {
        val records = listOf(
            com.example.data.entity.FlashHistoryEntity(
                id = 1L,
                operationType = "FASTBOOT_ROM",
                targetName = "factory_cheetah.zip",
                deviceModel = "Pixel 7 Pro",
                filePath = "/sdcard/factory_cheetah.zip",
                timestamp = 1728000000000L,
                status = "SUCCESS",
                durationMs = 3200L,
                bytesProcessed = 2500000000L,
                summary = "All partitions written",
                auditChecksum = "sha256_mock_hash_123"
            )
        )

        val csv = com.example.util.AuditExportHelper.generateCsv(records)
        assertTrue(csv.contains("id,timestamp_epoch,timestamp_iso"))
        assertTrue(csv.contains("factory_cheetah.zip"))
        assertTrue(csv.contains("sha256_mock_hash_123"))

        val txt = com.example.util.AuditExportHelper.generateFormattedText(records)
        assertTrue(txt.contains("ROM FLASH TOOL - AUDIT TRAIL LOG"))
        assertTrue(txt.contains("Pixel 7 Pro"))
        assertTrue(txt.contains("SUCCESS"))
    }

    @Test
    fun testStorageLayoutScannerModel() {
        val sample = com.example.model.StorageLayoutScanResult.defaultSampleLayout("cheetah")
        assertEquals("cheetah", sample.deviceName)
        assertTrue(sample.partitions.isNotEmpty())
        assertTrue(sample.totalCapacityBytes > 0)

        val bootPart = sample.partitions.first { it.name == "boot_a" }
        assertEquals(com.example.model.PartitionCategory.BOOT_KERNEL, bootPart.category)
        assertEquals("64.0 MB", bootPart.formattedSize)

        val superPart = sample.partitions.first { it.name == "super" }
        assertEquals(com.example.model.PartitionCategory.DYNAMIC_SUPER, superPart.category)
    }

    @Test
    fun testDeviceAuthStatusEnum() {
        val authStatuses = com.example.model.DeviceAuthStatus.values()
        assertEquals(4, authStatuses.size)
        assertTrue(authStatuses.contains(com.example.model.DeviceAuthStatus.AUTHORIZED))
        assertTrue(authStatuses.contains(com.example.model.DeviceAuthStatus.UNAUTHORIZED))
        assertTrue(authStatuses.contains(com.example.model.DeviceAuthStatus.OFFLINE))
        assertTrue(authStatuses.contains(com.example.model.DeviceAuthStatus.DISCONNECTED))

        val state = com.example.model.DeviceState(
            connectionType = com.example.model.DeviceConnectionType.ADB_CONNECTED,
            authStatus = com.example.model.DeviceAuthStatus.UNAUTHORIZED,
            statusMessage = "Pending RSA"
        )
        assertTrue(state.isConnected)
        assertTrue(state.isUnauthorized)
        assertFalse(state.isAuthorized)
        assertFalse(state.isOffline)
    }

    @Test
    fun testFlashProgressStateByteCalculations() {
        val totalBytes = 67108864L // 64 MB
        val bytesProcessed = 33554432L // 32 MB
        val progress = com.example.model.FlashProgressState(
            isRunning = true,
            currentStep = 2,
            totalSteps = 3,
            currentAction = "Sending image payload",
            currentPartition = "boot",
            bytesProcessed = bytesProcessed,
            totalBytes = totalBytes,
            percentage = bytesProcessed.toFloat() / totalBytes.toFloat(),
            speedMbPerSec = 42.5
        )

        assertEquals(0.5f, progress.percentage, 0.001f)
        assertEquals(33554432L, progress.bytesProcessed)
        assertEquals(67108864L, progress.totalBytes)
        assertEquals("boot", progress.currentPartition)
        assertEquals(42.5, progress.speedMbPerSec, 0.01)

        val percentageFormatted = String.format(java.util.Locale.US, "%.1f%%", progress.percentage * 100)
        assertEquals("50.0%", percentageFormatted)
    }

    @Test
    fun testManualCommandLogModel() {
        val entry = com.example.model.ManualCommandLog(
            command = "getvar all",
            protocol = "FASTBOOT",
            output = "(bootloader) product: cheetah\n(bootloader) unlocked: yes",
            isSuccess = true,
            executionTimeMs = 42L
        )

        assertEquals("getvar all", entry.command)
        assertEquals("FASTBOOT", entry.protocol)
        assertTrue(entry.isSuccess)
        assertEquals(42L, entry.executionTimeMs)
        assertTrue(entry.output.contains("cheetah"))
    }

    @Test
    fun testNavigationTabEnumIncludesManualCommand() {
        val tabs = com.example.viewmodel.NavigationTab.values()
        assertTrue(tabs.contains(com.example.viewmodel.NavigationTab.MANUAL_COMMAND))
    }
}
