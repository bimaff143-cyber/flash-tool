package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.entity.BackupRecordEntity
import com.example.model.DeviceState
import com.example.model.FlashProgressState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RestoreScreen(
    deviceState: DeviceState,
    backups: List<BackupRecordEntity>,
    progressState: FlashProgressState,
    logs: List<LogEntry>,
    onRestoreBackup: (BackupRecordEntity) -> Unit,
    onScanUsb: () -> Unit,
    onToggleSim: (Boolean) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        // Info Banner
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
                    Text(
                        text = "PARTITION RESTORATION MANAGER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Restores previously backed-up raw partition dumps. Integrity is verified using SHA-256 hashes prior to flashing.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Saved Backups List
        item {
            Text(
                text = "AVAILABLE LOCAL BACKUPS (${backups.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (backups.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No partition backups found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Text(
                            text = "Go to the Backup tab to dump boot, dtbo, vbmeta, or efs.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(backups, key = { it.id }) { backup ->
                BackupRecordRow(
                    backup = backup,
                    isDeviceConnected = deviceState.isConnected,
                    isFlashing = progressState.isRunning,
                    onRestore = { onRestoreBackup(backup) }
                )
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
private fun BackupRecordRow(
    backup: BackupRecordEntity,
    isDeviceConnected: Boolean,
    isFlashing: Boolean,
    onRestore: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(backup.timestamp))
    val spec = SupportedPartitions.find(backup.partitionName)
    val isCrit = spec.dangerLevel == PartitionDangerLevel.CRITICAL

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurface)
            .border(
                1.dp,
                if (isCrit) NeonCrimson.copy(alpha = 0.35f) else CyberCardBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isCrit) NeonCrimson else ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${backup.partitionName}.img",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isCrit) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CRITICAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCrimson,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Button(
                    onClick = onRestore,
                    enabled = isDeviceConnected && !isFlashing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCrit) NeonAmber else ElectricCyan,
                        disabledContainerColor = CyberCardBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("restore_btn_${backup.partitionName}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = if (isDeviceConnected) CyberBackground else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Restore",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDeviceConnected) CyberBackground else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Target Device: ${backup.deviceProduct} • Size: ${backup.sizeBytes / (1024 * 1024)} MB",
                fontSize = 11.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Hash: ${backup.sha256Checksum.take(24)}... • $dateStr",
                fontSize = 10.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
