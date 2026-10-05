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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogEntry
import com.example.model.LogLevel
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
fun LogsScreen(
    logs: List<LogEntry>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<LogLevel?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val filteredLogs = remember(logs, selectedFilter, searchQuery) {
        logs.filter { entry ->
            val matchLevel = selectedFilter == null || entry.level == selectedFilter
            val matchQuery = searchQuery.isEmpty() ||
                entry.message.contains(searchQuery, ignoreCase = true) ||
                entry.tag.contains(searchQuery, ignoreCase = true)
            matchLevel && matchQuery
        }
    }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        // Top action bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DETAILED PROTOCOL LOGS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
            }

            Row {
                IconButton(
                    onClick = {
                        val text = filteredLogs.joinToString("\n") { "[${it.level}] ${it.tag}: ${it.message}" }
                        clipboardManager.setText(AnnotatedString(text))
                    },
                    modifier = Modifier.testTag("copy_logs_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy logs",
                        tint = TextSecondary
                    )
                }
                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier.testTag("clear_logs_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear logs",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search textfield
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs by tag or command...", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedContainerColor = CyberSurface,
                unfocusedContainerColor = CyberSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    label = "ALL (${logs.size})",
                    isSelected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
            }
            item {
                FilterChip(
                    label = "FASTBOOT",
                    isSelected = selectedFilter == LogLevel.FASTBOOT,
                    onClick = { selectedFilter = LogLevel.FASTBOOT },
                    color = ElectricCyan
                )
            }
            item {
                FilterChip(
                    label = "ADB",
                    isSelected = selectedFilter == LogLevel.ADB,
                    onClick = { selectedFilter = LogLevel.ADB },
                    color = NeonEmerald
                )
            }
            item {
                FilterChip(
                    label = "PYTHON",
                    isSelected = selectedFilter == LogLevel.PYTHON,
                    onClick = { selectedFilter = LogLevel.PYTHON },
                    color = NeonAmber
                )
            }
            item {
                FilterChip(
                    label = "ERRORS",
                    isSelected = selectedFilter == LogLevel.ERROR,
                    onClick = { selectedFilter = LogLevel.ERROR },
                    color = NeonCrimson
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Terminal view
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurface)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logs match current filter criteria.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.matchParentSize()
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        LogRowDetailed(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color? = null
) {
    val chipColor = color ?: ElectricCyan
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) chipColor.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) chipColor else CyberCardBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) chipColor else TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun LogRowDetailed(log: LogEntry) {
    val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
    val levelColor = when (log.level) {
        LogLevel.FASTBOOT -> ElectricCyan
        LogLevel.ADB -> NeonEmerald
        LogLevel.PYTHON -> NeonAmber
        LogLevel.WARN -> NeonAmber
        LogLevel.ERROR -> NeonCrimson
        LogLevel.SUCCESS -> NeonEmerald
        LogLevel.INFO -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = timeStr,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "[${log.level}]",
            color = levelColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = log.message,
            color = TextPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
