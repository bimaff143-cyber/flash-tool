package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceAuthStatus
import com.example.model.DeviceState
import com.example.model.ManualCommandLog
import com.example.ui.components.DeviceStatusBar
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
fun ManualCommandScreen(
    deviceState: DeviceState,
    commandHistory: List<ManualCommandLog>,
    isExecuting: Boolean,
    onExecuteCommand: (command: String, protocol: String) -> Unit,
    onClearHistory: () -> Unit,
    onScanUsb: () -> Unit,
    onToggleSim: (Boolean) -> Unit,
    onSimulateAuthStatus: (DeviceAuthStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedProtocol by remember { mutableStateOf("FASTBOOT") }
    var commandInput by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val terminalListState = rememberLazyListState()

    LaunchedEffect(commandHistory.size) {
        if (commandHistory.isNotEmpty()) {
            terminalListState.animateScrollToItem(0)
        }
    }

    val fastbootTemplates = listOf(
        "devices",
        "getvar all",
        "getvar current-slot",
        "getvar unlocked",
        "getvar max-download-size",
        "flashing get_unlock_ability",
        "oem device-info",
        "reboot-bootloader",
        "reboot-recovery",
        "reboot-fastboot",
        "reboot"
    )

    val adbTemplates = listOf(
        "devices",
        "get-state",
        "shell getprop ro.product.model",
        "shell getprop ro.build.version.release",
        "shell getprop ro.boot.slot_suffix",
        "shell uname -a",
        "shell id",
        "reboot bootloader",
        "reboot recovery",
        "reboot sideload",
        "reboot"
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

        // Manual Console Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("manual_command_panel")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MANUAL COMMAND CONSOLE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Execute direct Fastboot & ADB CLI instructions",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Protocol selector pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProtocolToggleChip(
                                label = "FASTBOOT",
                                isSelected = selectedProtocol == "FASTBOOT",
                                color = ElectricCyan,
                                onClick = { selectedProtocol = "FASTBOOT" }
                            )

                            ProtocolToggleChip(
                                label = "ADB",
                                isSelected = selectedProtocol == "ADB",
                                color = NeonEmerald,
                                onClick = { selectedProtocol = "ADB" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Command Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_command_input"),
                            placeholder = {
                                Text(
                                    text = if (selectedProtocol == "FASTBOOT") "e.g. getvar all or flashing unlock" else "e.g. devices or shell getprop",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                            },
                            prefix = {
                                Text(
                                    text = "${selectedProtocol.lowercase()} ",
                                    color = if (selectedProtocol == "FASTBOOT") ElectricCyan else NeonEmerald,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            trailingIcon = {
                                if (commandInput.isNotEmpty()) {
                                    IconButton(onClick = { commandInput = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear input",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (commandInput.isNotBlank() && !isExecuting) {
                                        onExecuteCommand(commandInput, selectedProtocol)
                                        commandInput = ""
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (selectedProtocol == "FASTBOOT") ElectricCyan else NeonEmerald,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedContainerColor = CyberBackground,
                                unfocusedContainerColor = CyberBackground
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (commandInput.isNotBlank() && !isExecuting) {
                                    onExecuteCommand(commandInput, selectedProtocol)
                                    commandInput = ""
                                }
                            },
                            enabled = commandInput.isNotBlank() && !isExecuting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedProtocol == "FASTBOOT") ElectricCyan else NeonEmerald,
                                disabledContainerColor = CyberSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("execute_manual_command_btn")
                        ) {
                            if (isExecuting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CyberBackground,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run",
                                    tint = CyberBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Template Chips
                    Text(
                        text = "QUICK COMMAND TEMPLATES (${selectedProtocol})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val activeTemplates = if (selectedProtocol == "FASTBOOT") fastbootTemplates else adbTemplates
                        val activeColor = if (selectedProtocol == "FASTBOOT") ElectricCyan else NeonEmerald
                        for (tmpl in activeTemplates) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberSurfaceVariant)
                                    .border(1.dp, CyberCardBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        commandInput = tmpl
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("tmpl_${tmpl.replace(" ", "_")}")
                            ) {
                                Text(
                                    text = tmpl,
                                    fontSize = 10.sp,
                                    color = activeColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Terminal Output Console Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("terminal_output_box")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CONSOLE OUTPUT (${commandHistory.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        if (commandHistory.isNotEmpty()) {
                            IconButton(
                                onClick = onClearHistory,
                                modifier = Modifier.size(28.dp).testTag("clear_terminal_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Terminal",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberBackground)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        if (commandHistory.isEmpty()) {
                            Box(
                                modifier = Modifier.matchParentSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Terminal ready. Enter a command above to execute.",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                state = terminalListState,
                                modifier = Modifier.matchParentSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(commandHistory, key = { it.id }) { log ->
                                    CommandLogItem(
                                        log = log,
                                        onCopy = {
                                            clipboard.setText(AnnotatedString(log.output))
                                            Toast.makeText(context, "Command output copied!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocolToggleChip(
    label: String,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) color else CyberCardBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else TextMuted,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun CommandLogItem(
    log: ManualCommandLog,
    onCopy: () -> Unit
) {
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    val protoColor = if (log.protocol == "FASTBOOT") ElectricCyan else NeonEmerald

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(protoColor.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.protocol,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = protoColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timeStr,
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (log.isSuccess) "OKAY (${log.executionTimeMs}ms)" else "FAIL (${log.executionTimeMs}ms)",
                    color = if (log.isSuccess) NeonEmerald else NeonCrimson,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Command line
        Text(
            text = "$ ${log.protocol.lowercase()} ${log.command}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = protoColor,
            fontFamily = FontFamily.Monospace
        )

        if (log.output.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.output,
                fontSize = 10.sp,
                color = if (log.isSuccess) TextPrimary else NeonCrimson,
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )
        }
    }
}
