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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.FlashPreset
import com.example.model.FlashProgressState
import com.example.model.LogEntry
import com.example.model.RomPackageInfo
import com.example.model.RomPartitionItem
import com.example.ui.components.DeviceStatusBar
import com.example.ui.components.FlashPresetsDialog
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

@Composable
fun FastbootFlashScreen(
    deviceState: DeviceState,
    selectedRom: RomPackageInfo?,
    selectedSlot: String,
    wipeUserdata: Boolean,
    disableVerity: Boolean,
    autoReboot: Boolean,
    progressState: FlashProgressState,
    logs: List<LogEntry>,
    presets: List<FlashPreset> = emptyList(),
    onApplyPreset: (FlashPreset) -> Unit = {},
    onSaveCustomPreset: (String, String, List<String>, Boolean, Boolean) -> Unit = { _, _, _, _, _ -> },
    onDeleteCustomPreset: (String) -> Unit = {},
    onSelectSlot: (String) -> Unit,
    onSetWipeUserdata: (Boolean) -> Unit,
    onSetDisableVerity: (Boolean) -> Unit,
    onSetAutoReboot: (Boolean) -> Unit,
    onStartFlash: () -> Unit,
    onLoadSampleRom: (String) -> Unit,
    onScanUsb: () -> Unit,
    onToggleSim: (Boolean) -> Unit,
    onClearLogs: () -> Unit,
    onSimulateAuthStatus: (DeviceAuthStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showPresetsDialog by remember { mutableStateOf(false) }

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

        // Quick Flash Presets Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("flash_presets_card")
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
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = NeonAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "FLASH PRESETS (JSON BATCH)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "One-tap recovery, root & kernel partition setups",
                                    fontSize = 9.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Button(
                            onClick = { showPresetsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp).testTag("open_presets_btn")
                        ) {
                            Text("ALL PRESETS (${presets.size})", color = CyberBackground, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (presets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (preset in presets.take(3)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberSurfaceVariant)
                                        .border(1.dp, CyberCardBorder, RoundedCornerShape(6.dp))
                                        .clickable { onApplyPreset(preset) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("quick_preset_${preset.id}")
                                ) {
                                    Text(
                                        text = preset.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ROM Package Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("rom_package_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARGET FASTBOOT ROM PACKAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )

                        // Sample preset selectors
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PresetPill(
                                label = "Pixel 7 Pro",
                                isSelected = selectedRom?.fileName?.contains("cheetah") == true,
                                onClick = { onLoadSampleRom("cheetah") }
                            )
                            PresetPill(
                                label = "Xiaomi 13",
                                isSelected = selectedRom?.fileName?.contains("fuxi") == true,
                                onClick = { onLoadSampleRom("fuxi") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedRom != null) {
                        Text(
                            text = selectedRom.fileName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Type: ${selectedRom.type} • Approx Size: ${selectedRom.sizeBytes / (1024 * 1024)} MB",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Compatibility banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedRom.isCompatible) NeonEmerald.copy(alpha = 0.12f)
                                    else NeonCrimson.copy(alpha = 0.12f)
                                )
                                .border(
                                    1.dp,
                                    if (selectedRom.isCompatible) NeonEmerald.copy(alpha = 0.3f)
                                    else NeonCrimson.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (selectedRom.isCompatible) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (selectedRom.isCompatible) NeonEmerald else NeonCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedRom.compatibilityMessage,
                                    fontSize = 11.sp,
                                    color = if (selectedRom.isCompatible) NeonEmerald else NeonCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Flashing Options Card
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
                        text = "FLASH CONFIGURATION & TARGET SLOT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Slot Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SlotOptionButton(
                            slot = "a",
                            label = "Slot A",
                            isSelected = selectedSlot == "a",
                            onClick = { onSelectSlot("a") },
                            modifier = Modifier.weight(1f).testTag("slot_a_button")
                        )
                        SlotOptionButton(
                            slot = "b",
                            label = "Slot B",
                            isSelected = selectedSlot == "b",
                            onClick = { onSelectSlot("b") },
                            modifier = Modifier.weight(1f).testTag("slot_b_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wipe Userdata (Factory Reset)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSetWipeUserdata(!wipeUserdata) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = wipeUserdata,
                                onCheckedChange = onSetWipeUserdata,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonCrimson,
                                    checkmarkColor = CyberBackground
                                ),
                                modifier = Modifier.testTag("wipe_userdata_checkbox")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "Wipe Userdata (Clean Install)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Erases userdata and metadata partitions",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        if (wipeUserdata) {
                            Text(
                                text = "WIPE ON",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCrimson,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Disable Verity (AVB)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSetDisableVerity(!disableVerity) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = disableVerity,
                                onCheckedChange = onSetDisableVerity,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonAmber,
                                    checkmarkColor = CyberBackground
                                ),
                                modifier = Modifier.testTag("disable_verity_checkbox")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "Disable AVB Verity & Verification",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Patches vbmeta flags (--disable-verity)",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Auto Reboot Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Automatic Reboot after Flash",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Reboots system automatically upon OKAY status",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = autoReboot,
                            onCheckedChange = onSetAutoReboot,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricCyan,
                                checkedTrackColor = ElectricCyan.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("auto_reboot_switch")
                        )
                    }
                }
            }
        }

        // Partition Sequence Preview
        selectedRom?.let { rom ->
            item {
                Text(
                    text = "PARTITIONS TO FLASH (${rom.partitionItems.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            items(rom.partitionItems) { part ->
                RomPartitionRow(part, selectedSlot)
            }
        }

        // Action Button: Flash Fastboot ROM
        item {
            Button(
                onClick = onStartFlash,
                enabled = !progressState.isRunning && deviceState.isConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_fastboot_flash_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    disabledContainerColor = CyberCardBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (deviceState.isConnected) CyberBackground else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (progressState.isRunning) "FLASHING IN PROGRESS..." else "FLASH FASTBOOT ROM",
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

    if (showPresetsDialog) {
        FlashPresetsDialog(
            presets = presets,
            onApplyPreset = onApplyPreset,
            onSaveCustomPreset = onSaveCustomPreset,
            onDeleteCustomPreset = onDeleteCustomPreset,
            onDismiss = { showPresetsDialog = false }
        )
    }
}

@Composable
private fun PresetPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) ElectricCyan else CyberCardBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) ElectricCyan else TextSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SlotOptionButton(
    slot: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ElectricCyan.copy(alpha = 0.15f) else CyberSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) ElectricCyan else CyberCardBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) ElectricCyan else TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun RomPartitionRow(part: RomPartitionItem, slot: String) {
    val targetWithSlot = if (part.partitionName == "super") "super" else "${part.partitionName}_$slot"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (part.isDangerous) NeonCrimson else ElectricCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = targetWithSlot,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = part.fileName,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (part.isChecksumVerified) {
                    Text(
                        text = "SHA256:OK",
                        color = NeonEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                if (part.isDangerous) {
                    Text(
                        text = "CRITICAL",
                        color = NeonCrimson,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
