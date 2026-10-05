package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceState
import com.example.model.FlashProgressState
import com.example.model.ImageInspectionResult
import com.example.model.LogEntry
import com.example.model.PartitionDangerLevel
import com.example.model.SupportedPartitions
import com.example.ui.components.DeviceStatusBar
import com.example.ui.components.ProgressLogView
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BootImageScreen(
    deviceState: DeviceState,
    targetPartition: String,
    inspectionResult: ImageInspectionResult?,
    progressState: FlashProgressState,
    logs: List<LogEntry>,
    onSelectPartition: (String) -> Unit,
    onStartFlash: () -> Unit,
    onScanUsb: () -> Unit,
    onToggleSim: (Boolean) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickPartitions = listOf(
        "boot",
        "init_boot",
        "vendor_boot",
        "recovery",
        "dtbo",
        "vbmeta",
        "vbmeta_system",
        "persist",
        "efs"
    )

    val currentSpec = SupportedPartitions.find(targetPartition)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Status Header
        item {
            DeviceStatusBar(
                deviceState = deviceState,
                onScanClick = onScanUsb,
                onToggleSimulation = onToggleSim
            )
        }

        // Partition Selector Chips
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("partition_selector_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SELECT TARGET PARTITION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickPartitions.forEach { part ->
                            val isSelected = targetPartition == part
                            val spec = SupportedPartitions.find(part)
                            val isCrit = spec.dangerLevel == PartitionDangerLevel.CRITICAL

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> ElectricCyan.copy(alpha = 0.2f)
                                            isCrit -> NeonCrimson.copy(alpha = 0.08f)
                                            else -> CyberSurfaceVariant
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when {
                                            isSelected -> ElectricCyan
                                            isCrit -> NeonCrimson.copy(alpha = 0.35f)
                                            else -> CyberCardBorder
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectPartition(part) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("part_chip_$part")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCrit) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(NeonCrimson)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                    }
                                    Text(
                                        text = "$part.img",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ElectricCyan else TextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Partition Description
                    Text(
                        text = currentSpec.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = currentSpec.description,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    if (currentSpec.dangerLevel == PartitionDangerLevel.CRITICAL) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCrimson.copy(alpha = 0.12f))
                                .border(1.dp, NeonCrimson.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = NeonCrimson,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentSpec.dangerWarning ?: "Critical firmware partition.",
                                    fontSize = 11.sp,
                                    color = NeonCrimson,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Image Header & Python Analysis Card
        inspectionResult?.let { img ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("image_analysis_card")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PYTHON IMAGE ANALYSIS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = img.headerMagic,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        InfoRow("File Name", img.fileName)
                        InfoRow("Target Partition", img.targetPartition)
                        InfoRow("Header Version", "v${img.headerVersion}")

                        if (img.kernelSize > 0) {
                            InfoRow("Kernel Size", "${img.kernelSize / (1024 * 1024)} MB (${img.kernelSize} bytes)")
                        }
                        if (img.ramdiskSize > 0) {
                            InfoRow("Ramdisk Size", "${img.ramdiskSize / (1024 * 1024)} MB (${img.ramdiskSize} bytes)")
                        }
                        if (img.osVersion != "N/A" && img.osVersion != "Unknown") {
                            InfoRow("OS Version", img.osVersion)
                        }
                        if (img.isAvb) {
                            InfoRow("AVB Verity Disabled", if (img.verityDisabled) "YES (0x1)" else "NO (Enforced)")
                            InfoRow("AVB Verification Disabled", if (img.verificationDisabled) "YES (0x2)" else "NO (Enforced)")
                        }

                        InfoRow("SHA-256 Digest", img.sha256.take(24) + "...")
                        InfoRow("MD5 Digest", img.md5)

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (img.isChecksumVerified) NeonEmerald.copy(alpha = 0.12f)
                                    else NeonCrimson.copy(alpha = 0.12f)
                                )
                                .border(
                                    1.dp,
                                    if (img.isChecksumVerified) NeonEmerald.copy(alpha = 0.35f)
                                    else NeonCrimson.copy(alpha = 0.35f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (img.isChecksumVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (img.isChecksumVerified) NeonEmerald else NeonCrimson,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (img.isChecksumVerified) "Checksum Verified: MD5 & SHA-256 match manifest" else img.checksumVerificationMessage,
                                    fontSize = 11.sp,
                                    color = if (img.isChecksumVerified) NeonEmerald else NeonCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Button: Flash Partition Image
        item {
            Button(
                onClick = onStartFlash,
                enabled = !progressState.isRunning && deviceState.isConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_boot_flash_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentSpec.dangerLevel == PartitionDangerLevel.CRITICAL) NeonAmber else ElectricCyan,
                    disabledContainerColor = CyberCardBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentSpec.dangerLevel == PartitionDangerLevel.CRITICAL) Icons.Default.Warning else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (deviceState.isConnected) CyberBackground else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (progressState.isRunning) "FLASHING..." else "FLASH TO [${targetPartition.uppercase()}]",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (deviceState.isConnected) CyberBackground else TextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Live logs terminal
        item {
            ProgressLogView(
                progressState = progressState,
                logs = logs,
                onClearLogs = onClearLogs
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
