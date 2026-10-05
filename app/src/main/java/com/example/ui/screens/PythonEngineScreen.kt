package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.python.PythonEngineBridge
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
fun PythonEngineScreen(
    pythonEngine: PythonEngineBridge,
    modifier: Modifier = Modifier
) {
    val scripts = pythonEngine.getAvailableScripts()
    var selectedScript by remember { mutableStateOf(scripts.firstOrNull() ?: "fastboot_manager.py") }
    var interactiveInput by remember { mutableStateOf("import fastboot_manager\nhelp()") }
    var interactiveOutput by remember { mutableStateOf(">> Python 3.11.0 Engine Ready\n>> Tap Run to execute AST analysis.") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Architecture Overview
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
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EMBEDDED PYTHON 3 ENGINE ARCHITECTURE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Python 3 drives the application logic, ROM parsing algorithms, partition header validation (AVB vbmeta flags, boot.img v0-v4, DTBO), checksum verifications, and backup/restore workflows.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Script Selector Tabs
        item {
            Text(
                text = "PYTHON 3 MODULES (${scripts.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("python_scripts_row")
            ) {
                items(scripts) { script ->
                    val isSelected = selectedScript == script
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CyberSurface)
                            .border(
                                1.dp,
                                if (isSelected) ElectricCyan else CyberCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedScript = script }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = script,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ElectricCyan else TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Script Code Viewer
        item {
            val code = pythonEngine.getScriptContent(selectedScript)
            val hScroll = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberBackground)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedScript,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${code.lines().size} lines",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(hScroll)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(code.lines()) { line ->
                                Text(
                                    text = line,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = when {
                                        line.trim().startsWith("#") || line.trim().startsWith("\"\"\"") -> TextMuted
                                        line.trim().startsWith("class ") || line.trim().startsWith("def ") -> ElectricCyan
                                        line.trim().startsWith("import ") || line.trim().startsWith("from ") -> NeonAmber
                                        line.trim().startsWith("return ") -> NeonEmerald
                                        else -> TextPrimary
                                    },
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Python REPL / Testing Sandbox
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("python_interactive_sandbox")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INTERACTIVE PYTHON 3 SANDBOX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        Button(
                            onClick = {
                                interactiveOutput = pythonEngine.executePythonSnippet(interactiveInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("run_python_snippet_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = CyberBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Run",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = interactiveInput,
                        onValueChange = { interactiveInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonEmerald,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedContainerColor = CyberBackground,
                            unfocusedContainerColor = CyberBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        placeholder = {
                            Text(
                                text = "e.g. import fastboot_manager",
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Output console
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberBackground)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = interactiveOutput,
                            color = NeonEmerald,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
