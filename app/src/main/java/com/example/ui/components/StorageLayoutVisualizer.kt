package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PartitionBlockInfo
import com.example.model.PartitionCategory
import com.example.model.StorageLayoutScanResult
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StorageLayoutVisualizer(
    storageResult: StorageLayoutScanResult?,
    isScanning: Boolean,
    selectedPartitions: Set<String>,
    onTogglePartition: (String) -> Unit,
    onSelectAllBootKernel: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<PartitionCategory?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("storage_layout_visualizer")
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "STORAGE LAYOUT & GPT MAP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (storageResult != null) "Scanned Device: ${storageResult.deviceName.uppercase()} (${storageResult.formattedTotalCapacity})" else "Fastboot GPT Partition Table Query",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ElectricCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onScanClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("rescan_storage_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan Fastboot Partition Table",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (storageResult == null) {
                // Not yet scanned state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurfaceVariant)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Automated Partition Table Scanner",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Query Fastboot getvar partition sizes and display graphical storage layout for backup targets.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onScanClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("scan_gpt_table_btn")
                        ) {
                            Text("SCAN PARTITION TABLE", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                // Graphical Proportional Storage Distribution Bar
                Text(
                    text = "STORAGE MAP DISTRIBUTION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Segmented Storage Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberSurfaceVariant)
                ) {
                    // Proportional blocks for categories
                    val categories = PartitionCategory.values()
                    for (cat in categories) {
                        val catBytes = storageResult.partitions.filter { it.category == cat }.sumOf { it.sizeBytes }
                        val weight = (catBytes.toFloat() / storageResult.totalCapacityBytes.toFloat()).coerceAtLeast(0.04f)
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .fillMaxHeight()
                                .background(Color(cat.colorHex))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips Legend & Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryFilterChip(
                        label = "ALL (${storageResult.partitions.size})",
                        isSelected = selectedCategoryFilter == null,
                        color = ElectricCyan,
                        onClick = { selectedCategoryFilter = null }
                    )
                    for (cat in PartitionCategory.values()) {
                        val count = storageResult.partitions.count { it.category == cat }
                        CategoryFilterChip(
                            label = "${cat.displayName} ($count)",
                            isSelected = selectedCategoryFilter == cat,
                            color = Color(cat.colorHex),
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selCount = selectedPartitions.size
                    Text(
                        text = "Selected for Backup: $selCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selCount > 0) NeonEmerald else TextMuted,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Kernel Only",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .clickable(onClick = onSelectAllBootKernel)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("select_kernel_partitions_btn")
                        )
                        Text(
                            text = "Select All",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .clickable(onClick = onSelectAll)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("select_all_storage_btn")
                        )
                        if (selCount > 0) {
                            Text(
                                text = "Clear",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCrimson,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonCrimson.copy(alpha = 0.15f))
                                    .clickable(onClick = onClearSelection)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("clear_storage_selection_btn")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Graphical Partition Blocks
                val displayPartitions = storageResult.partitions.filter {
                    selectedCategoryFilter == null || it.category == selectedCategoryFilter
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (part in displayPartitions) {
                        val isSelected = selectedPartitions.contains(part.name) ||
                            (part.slot != null && selectedPartitions.contains(part.name.removeSuffix("_a").removeSuffix("_b")))
                        PartitionCardBlock(
                            partition = part,
                            isSelected = isSelected,
                            onToggle = { onTogglePartition(part.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) color else CyberCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PartitionCardBlock(
    partition: PartitionBlockInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val catColor = Color(partition.category.colorHex)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) catColor.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) catColor else CyberCardBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("part_block_${partition.name}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(catColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = partition.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) catColor else TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    if (partition.slot != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "_${partition.slot.uppercase()}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = partition.formattedSize,
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = catColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
