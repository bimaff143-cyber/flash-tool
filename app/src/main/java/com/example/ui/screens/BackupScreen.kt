package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.model.DeviceAuthStatus
import com.example.model.DeviceState
import com.example.model.FlashProgressState
import com.example.model.LogEntry
import com.example.model.PartitionDangerLevel
import com.example.model.StorageLayoutScanResult
import com.example.model.SupportedPartitions
import com.example.ui.components.DeviceStatusBar
import com.example.ui.components.ProgressLogView
import com.example.ui.components.StorageLayoutVisualizer
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

@Composable
fun BackupScreen(
    deviceState: DeviceState,
    selectedPartitions: Set<String>,
    progressState: FlashProgressState,
    logs: List<LogEntry>,
    storageResult: StorageLayoutScanResult?,
    isScanningStorage: Boolean,
    onTogglePartition: (String) -> Unit,
    onStartBackup: () -> Unit,
    onScanUsb: () -> Unit,
    onToggleSim: (Boolean) -> Unit,
    onClearLogs: () -> Unit,
    onScanStorage: () -> Unit,
    onSelectAllBootKernel: () -> Unit,
    onSelectAllStorage: () -> Unit,
    onClearSelection: () -> Unit,
    onSimulateAuthStatus: (DeviceAuthStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backupCandidates = listOf(
        "boot",
        "init_boot",
        "vendor_boot",
        "recovery",
        "dtbo",
        "vbmeta",
        "persist",
        "efs"
    )

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
                onToggleSimulation = onToggleSim,
                onSimulateAuthStatus = onSimulateAuthStatus
            )
        }

        // Automated Storage Layout & GPT Partition Table Visualizer
        item {
            StorageLayoutVisualizer(
                storageResult = storageResult,
                isScanning = isScanningStorage,
                selectedPartitions = selectedPartitions,
                onTogglePartition = onTogglePartition,
                onSelectAllBootKernel = onSelectAllBootKernel,
                onSelectAll = onSelectAllStorage,
                onClearSelection = onClearSelection,
                onScanClick = onScanStorage
            )
        }

        // Header info
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PYTHON BACKUP MANAGER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${selectedPartitions.size} SELECTED",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Creates exact bitwise dumps of crucial partitions before flashing modifications. EFS & Persist backups protect your radio IMEI and hardware calibrations.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Partitions Checklist
        item {
            Text(
                text = "SUPPORTED BACKUP PARTITIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                backupCandidates.forEach { part ->
                    val isChecked = selectedPartitions.contains(part)
                    val spec = SupportedPartitions.find(part)
                    val isCrit = spec.dangerLevel == PartitionDangerLevel.CRITICAL

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurface)
                            .border(
                                1.dp,
                                if (isChecked) ElectricCyan.copy(alpha = 0.5f) else CyberCardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onTogglePartition(part) }
                            .padding(10.dp)
                            .testTag("backup_item_$part")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onTogglePartition(part) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ElectricCyan,
                                        checkmarkColor = CyberBackground
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$part.img",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (isCrit) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "CRITICAL",
                                                color = NeonCrimson,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Text(
                                        text = spec.displayName,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Button: Start Backup
        item {
            Button(
                onClick = onStartBackup,
                enabled = !progressState.isRunning && deviceState.isConnected && selectedPartitions.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_backup_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    disabledContainerColor = CyberCardBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = if (deviceState.isConnected) CyberBackground else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (progressState.isRunning) "DUMPING PARTITIONS..." else "DUMP SELECTED PARTITIONS",
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
