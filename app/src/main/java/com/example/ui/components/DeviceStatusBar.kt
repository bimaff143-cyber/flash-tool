package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceAuthStatus
import com.example.model.DeviceConnectionType
import com.example.model.DeviceState
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
fun DeviceStatusBar(
    deviceState: DeviceState,
    onScanClick: () -> Unit,
    onToggleSimulation: (Boolean) -> Unit,
    onSimulateAuthStatus: (DeviceAuthStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("device_status_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real-time connection & authorization indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (deviceState.authStatus) {
                        DeviceAuthStatus.AUTHORIZED -> if (deviceState.isFastboot) ElectricCyan else NeonEmerald
                        DeviceAuthStatus.UNAUTHORIZED -> NeonAmber
                        DeviceAuthStatus.OFFLINE -> NeonCrimson
                        DeviceAuthStatus.DISCONNECTED -> TextMuted
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when (deviceState.authStatus) {
                                DeviceAuthStatus.AUTHORIZED -> {
                                    val proto = if (deviceState.isFastboot) "FASTBOOT" else "ADB"
                                    if (deviceState.isSimulated) "SIM: $proto AUTHORIZED" else "$proto ONLINE"
                                }
                                DeviceAuthStatus.UNAUTHORIZED -> "CONNECTED (UNAUTHORIZED)"
                                DeviceAuthStatus.OFFLINE -> "DEVICE OFFLINE"
                                DeviceAuthStatus.DISCONNECTED -> "NO DEVICE DETECTED"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = deviceState.statusMessage,
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Scan Refresh Button
                IconButton(
                    onClick = onScanClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("scan_usb_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan USB Devices",
                        tint = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Device Name & Serial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (deviceState.isConnected) deviceState.productName.uppercase() else "Waiting for USB OTG",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (deviceState.isConnected) "Serial: ${deviceState.serialNumber}" else "Plug phone via OTG in Fastboot/Bootloader",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Simulation mode switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "Sim Mode",
                        fontSize = 11.sp,
                        color = if (deviceState.isSimulated) ElectricCyan else TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = deviceState.isSimulated,
                        onCheckedChange = onToggleSimulation,
                        modifier = Modifier.testTag("simulation_toggle"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberSurface
                        )
                    )
                }
            }

            // Real-time status simulation controls (when Sim Mode is active)
            if (deviceState.isSimulated) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time State:",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )

                    AuthStatusChip(
                        label = "Authorized",
                        isSelected = deviceState.authStatus == DeviceAuthStatus.AUTHORIZED,
                        color = NeonEmerald,
                        onClick = { onSimulateAuthStatus(DeviceAuthStatus.AUTHORIZED) }
                    )

                    AuthStatusChip(
                        label = "Unauthorized",
                        isSelected = deviceState.authStatus == DeviceAuthStatus.UNAUTHORIZED,
                        color = NeonAmber,
                        onClick = { onSimulateAuthStatus(DeviceAuthStatus.UNAUTHORIZED) }
                    )

                    AuthStatusChip(
                        label = "Offline",
                        isSelected = deviceState.authStatus == DeviceAuthStatus.OFFLINE,
                        color = NeonCrimson,
                        onClick = { onSimulateAuthStatus(DeviceAuthStatus.OFFLINE) }
                    )
                }
            }

            if (deviceState.isConnected) {
                Spacer(modifier = Modifier.height(10.dp))
                // Badges row: Unlocked status, Active Slot, Max download size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Unlocked Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (deviceState.isUnlocked) NeonEmerald.copy(alpha = 0.15f) else NeonCrimson.copy(alpha = 0.15f))
                            .border(1.dp, if (deviceState.isUnlocked) NeonEmerald.copy(alpha = 0.4f) else NeonCrimson.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (deviceState.isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (deviceState.isUnlocked) NeonEmerald else NeonCrimson,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (deviceState.isUnlocked) "UNLOCKED" else "LOCKED",
                                color = if (deviceState.isUnlocked) NeonEmerald else NeonCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Slot Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SLOT ${deviceState.currentSlot.uppercase()}",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Battery Safety Badge (Python DeviceChecker rule: >= 30%)
                    val isBatterySafe = deviceState.batteryPercent >= 30
                    val batteryColor = if (isBatterySafe) NeonEmerald else NeonCrimson
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(batteryColor.copy(alpha = 0.15f))
                            .border(1.dp, batteryColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("battery_status_badge")
                    ) {
                        Text(
                            text = if (isBatterySafe) "${deviceState.batteryPercent}% BATTERY" else "${deviceState.batteryPercent}% (<30% CRITICAL)",
                            color = batteryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Max chunk size
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberCardBorder.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MAX: ${deviceState.maxDownloadSize / (1024 * 1024)}MB",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthStatusChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) color else CyberCardBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else TextMuted,
            fontFamily = FontFamily.Monospace
        )
    }
}
