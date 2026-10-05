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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FlashHistoryEntity
import com.example.ui.components.ExportHistoryDialog
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
fun HistoryAuditScreen(
    history: List<FlashHistoryEntity>,
    onDeleteRecord: (Long) -> Unit,
    onClearAll: () -> Unit,
    onExportSave: (Boolean) -> Unit = {},
    onExportShare: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var statusFilter by remember { mutableStateOf<String?>(null) } // null = ALL, "SUCCESS", "FAILED"
    var searchQuery by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val filtered = remember(history, statusFilter, searchQuery) {
        history.filter { item ->
            val matchStatus = statusFilter == null || item.status.equals(statusFilter, ignoreCase = true)
            val matchQuery = searchQuery.isEmpty() ||
                item.targetName.contains(searchQuery, ignoreCase = true) ||
                item.deviceModel.contains(searchQuery, ignoreCase = true) ||
                item.filePath.contains(searchQuery, ignoreCase = true) ||
                item.operationType.contains(searchQuery, ignoreCase = true)
            matchStatus && matchQuery
        }
    }

    val successCount = history.count { it.status == "SUCCESS" }
    val failedCount = history.count { it.status == "FAILED" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FLASH AUDIT TRAIL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Persistent Room SQLite Database Records",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (history.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("export_audit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export Audit Logs",
                                tint = ElectricCyan
                            )
                        }

                        IconButton(
                            onClick = onClearAll,
                            modifier = Modifier.testTag("clear_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear all history",
                                tint = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Summary Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "TOTAL",
                    count = history.size.toString(),
                    color = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "SUCCESS",
                    count = successCount.toString(),
                    color = NeonEmerald,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "FAILED",
                    count = failedCount.toString(),
                    color = if (failedCount > 0) NeonCrimson else TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search by device model, file path, or target...",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("history_search_input"),
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
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    AuditFilterChip(
                        label = "ALL (${history.size})",
                        isSelected = statusFilter == null,
                        onClick = { statusFilter = null },
                        color = ElectricCyan
                    )
                }
                item {
                    AuditFilterChip(
                        label = "SUCCESSFUL ($successCount)",
                        isSelected = statusFilter == "SUCCESS",
                        onClick = { statusFilter = "SUCCESS" },
                        color = NeonEmerald
                    )
                }
                item {
                    AuditFilterChip(
                        label = "FAILED ($failedCount)",
                        isSelected = statusFilter == "FAILED",
                        onClick = { statusFilter = "FAILED" },
                        color = NeonCrimson
                    )
                }
            }
        }

        // Audit Record List
        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No audit records found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Text(
                            text = "Completed flash operations are automatically logged here.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { record ->
                AuditRecordCard(
                    record = record,
                    onDelete = { onDeleteRecord(record.id) },
                    onCopyChecksum = {
                        record.auditChecksum?.let { cs ->
                            clipboardManager.setText(AnnotatedString(cs))
                        }
                    }
                )
            }
        }
    }

    if (showExportDialog) {
        ExportHistoryDialog(
            history = history,
            onSaveFile = onExportSave,
            onShareFile = onExportShare,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun AuditFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) color else CyberCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun AuditRecordCard(
    record: FlashHistoryEntity,
    onDelete: () -> Unit,
    onCopyChecksum: () -> Unit
) {
    val isSuccess = record.status.equals("SUCCESS", ignoreCase = true)
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_record_${record.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSuccess) CyberCardBorder else NeonCrimson.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Target Name, Status Badge, and Delete Button
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.targetName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSuccess) NeonEmerald.copy(alpha = 0.15f) else NeonCrimson.copy(alpha = 0.15f))
                            .border(1.dp, if (isSuccess) NeonEmerald.copy(alpha = 0.4f) else NeonCrimson.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = record.status.uppercase(),
                            color = if (isSuccess) NeonEmerald else NeonCrimson,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete record",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audit metadata rows: Device Model, File Path, Timestamp
            AuditFieldRow(label = "Device Model", value = record.deviceModel)
            AuditFieldRow(label = "File Path", value = record.filePath)
            AuditFieldRow(label = "Timestamp", value = "$dateStr (${record.timestamp})")
            AuditFieldRow(label = "Operation", value = "${record.operationType} • ${record.durationMs}ms")

            if (record.bytesProcessed > 0) {
                AuditFieldRow(
                    label = "Bytes Flashed",
                    value = "${record.bytesProcessed / (1024 * 1024)} MB (${record.bytesProcessed} bytes)"
                )
            }

            if (!record.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonCrimson.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = NeonCrimson,
                            modifier = Modifier.size(14.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = record.errorMessage,
                            color = NeonCrimson,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            if (!record.auditChecksum.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Checksum: ${record.auditChecksum.take(24)}...",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = onCopyChecksum,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Checksum",
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
