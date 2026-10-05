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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.LogEntry
import com.example.ui.components.DeviceStatusBar
import com.example.ui.components.ProgressLogView
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdbSideloadScreen(
    deviceState: DeviceState,
    progressState: FlashProgressState,
    logs: List<LogEntry>,
    autoReboot: Boolean,
    onSetAutoReboot: (Boolean) -> Unit,
    onStartSideload: (String, Long) -> Unit,
    onScanUsb: () -> Unit,
    onToggleSim: (Boolean) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleZipName = "lineage-21.0-nightly-ota-signed.zip"
    val sampleZipSize = 1450000000L

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

        // Instructions Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADB SIDELOAD RECOVERY PROTOCOL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. Boot your connected device into Recovery Mode (TWRP, Lineage Recovery, or AOSP Recovery).\n2. On the device screen, tap 'Apply Update' -> 'Apply from ADB' (or 'Advanced' -> 'ADB Sideload').\n3. ROM Flash Tool will stream the OTA package in 64KB blocks over USB Host.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Package Card
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
                        text = "SELECTED RECOVERY ZIP PACKAGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = sampleZipName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Size: ${sampleZipSize / (1024 * 1024)} MB • Blocks: ~${sampleZipSize / 65536}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto Reboot after Installation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Reboots system when Sideload returns 0/success",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = autoReboot,
                            onCheckedChange = onSetAutoReboot,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonAmber,
                                checkedTrackColor = NeonAmber.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        // Action Button: Start ADB Sideload
        item {
            Button(
                onClick = { onStartSideload(sampleZipName, sampleZipSize) },
                enabled = !progressState.isRunning && deviceState.isConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_sideload_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonAmber,
                    disabledContainerColor = CyberCardBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = if (deviceState.isConnected) CyberBackground else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (progressState.isRunning) "STREAMING SIDELOAD..." else "START ADB SIDELOAD",
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
