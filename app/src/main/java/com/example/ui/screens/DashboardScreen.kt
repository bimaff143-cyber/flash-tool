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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FlashHistoryEntity
import com.example.model.DeviceAuthStatus
import com.example.model.DeviceState
import com.example.model.FlashProgressState
import com.example.model.LogEntry
import com.example.ui.components.DeviceStatusBar
import com.example.ui.components.ProgressLogView
import com.example.ui.components.QuickRebootBar
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
import com.example.viewmodel.NavigationTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    deviceState: DeviceState,
    progressState: FlashProgressState,
    logs: List<LogEntry>,
    history: List<FlashHistoryEntity>,
    onNavigate: (NavigationTab) -> Unit,
    onScanUsb: () -> Unit,
    onToggleSim: (Boolean) -> Unit,
    onReboot: (String) -> Unit,
    onClearLogs: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onSimulateAuthStatus: (DeviceAuthStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Status Card
        item {
            DeviceStatusBar(
                deviceState = deviceState,
                onScanClick = onScanUsb,
                onToggleSimulation = onToggleSim,
                onSimulateAuthStatus = onSimulateAuthStatus
            )
        }

        // Quick Reboot Action Bar
        item {
            QuickRebootBar(
                onRebootSystem = { onReboot("system") },
                onRebootRecovery = { onReboot("recovery") },
                onRebootBootloader = { onReboot("bootloader") }
            )
        }

        // Feature Grid Cards
        item {
            Text(
                text = "FLASHING MODULES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureCard(
                        title = "Fastboot ROM",
                        subtitle = "Full stock / custom ROM",
                        icon = Icons.Default.FlashOn,
                        accentColor = ElectricCyan,
                        onClick = { onNavigate(NavigationTab.FASTBOOT_FLASH) },
                        modifier = Modifier.weight(1f).testTag("nav_fastboot_flash_card")
                    )
                    FeatureCard(
                        title = "Boot & Partitions",
                        subtitle = "boot, init_boot, vbmeta",
                        icon = Icons.Default.Memory,
                        accentColor = NeonEmerald,
                        onClick = { onNavigate(NavigationTab.BOOT_IMAGE) },
                        modifier = Modifier.weight(1f).testTag("nav_boot_image_card")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureCard(
                        title = "ADB Sideload",
                        subtitle = "Stream OTA zip in recovery",
                        icon = Icons.Default.InstallMobile,
                        accentColor = NeonAmber,
                        onClick = { onNavigate(NavigationTab.ADB_SIDELOAD) },
                        modifier = Modifier.weight(1f).testTag("nav_adb_sideload_card")
                    )
                    FeatureCard(
                        title = "Backup Partitions",
                        subtitle = "Dump boot, efs, persist",
                        icon = Icons.Default.Backup,
                        accentColor = ElectricCyan,
                        onClick = { onNavigate(NavigationTab.BACKUP) },
                        modifier = Modifier.weight(1f).testTag("nav_backup_card")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureCard(
                        title = "Python 3 Engine",
                        subtitle = "8 modules inspector & runner",
                        icon = Icons.Default.Code,
                        accentColor = NeonAmber,
                        onClick = { onNavigate(NavigationTab.PYTHON_ENGINE) },
                        modifier = Modifier.weight(1f).testTag("nav_python_engine_card")
                    )
                    FeatureCard(
                        title = "Audit History",
                        subtitle = "SQLite logs & timestamps",
                        icon = Icons.Default.History,
                        accentColor = ElectricCyan,
                        onClick = { onNavigate(NavigationTab.HISTORY) },
                        modifier = Modifier.weight(1f).testTag("nav_audit_history_card")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureCard(
                        title = "Manual Commands",
                        subtitle = "Fastboot & ADB CLI console",
                        icon = Icons.Default.Terminal,
                        accentColor = ElectricCyan,
                        onClick = { onNavigate(NavigationTab.MANUAL_COMMAND) },
                        modifier = Modifier.weight(1f).testTag("nav_manual_command_card")
                    )
                    FeatureCard(
                        title = "Recovery Mode",
                        subtitle = "TWRP, sideload, fastbootd",
                        icon = Icons.Default.RestartAlt,
                        accentColor = NeonEmerald,
                        onClick = { onNavigate(NavigationTab.RECOVERY) },
                        modifier = Modifier.weight(1f).testTag("nav_recovery_card")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureCard(
                        title = "Restore Backup",
                        subtitle = "Restore dumped blocks",
                        icon = Icons.Default.Restore,
                        accentColor = NeonEmerald,
                        onClick = { onNavigate(NavigationTab.RESTORE) },
                        modifier = Modifier.weight(1f).testTag("nav_restore_card")
                    )
                    FeatureCard(
                        title = "Settings & Theme",
                        subtitle = "Light, Dark, System mode",
                        icon = Icons.Default.Palette,
                        accentColor = NeonAmber,
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f).testTag("nav_settings_theme_card")
                    )
                }
            }
        }

        // Live Log and Progress View
        item {
            ProgressLogView(
                progressState = progressState,
                logs = logs,
                onClearLogs = onClearLogs,
                maxLogHeight = 200
            )
        }

        // Recent Flash History from Room
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT OPERATIONS (${history.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = "View Audit Trail >",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier
                            .clickable { onNavigate(NavigationTab.HISTORY) }
                            .padding(end = 4.dp)
                            .testTag("view_all_history_btn")
                    )
                }
            }

            items(history.take(4)) { item ->
                FlashHistoryRow(
                    item = item,
                    onClick = { onNavigate(NavigationTab.HISTORY) }
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun FlashHistoryRow(
    item: FlashHistoryEntity,
    onClick: () -> Unit = {}
) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
    val isSuccess = item.status == "SUCCESS"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
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
                        .background(if (isSuccess) NeonEmerald else NeonCrimson)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.targetName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${item.deviceModel} • $dateStr",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    if (item.filePath.isNotBlank()) {
                        Text(
                            text = item.filePath,
                            fontSize = 9.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Text(
                text = if (isSuccess) "OKAY" else "FAIL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isSuccess) NeonEmerald else NeonCrimson
            )
        }
    }
}
