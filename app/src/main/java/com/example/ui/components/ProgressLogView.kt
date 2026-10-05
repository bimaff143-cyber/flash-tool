package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlashProgressState
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
fun ProgressLogView(
    progressState: FlashProgressState,
    logs: List<LogEntry>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier,
    maxLogHeight: Int = 260
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("progress_log_view")
    ) {
        // Flashing progress section (visible when running or just completed)
        AnimatedVisibility(visible = progressState.isRunning || progressState.isSuccess || progressState.hasError) {
            val progressColor = when {
                progressState.hasError -> NeonCrimson
                progressState.isSuccess -> NeonEmerald
                else -> ElectricCyan
            }

            // Smooth linear progress bar animated based on transferred bytes / percentage
            val animatedProgress by animateFloatAsState(
                targetValue = progressState.percentage.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
                label = "smoothFlashProgress"
            )

            val displayPercent = (animatedProgress * 100).coerceIn(0f, 100f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceVariant)
                    .border(
                        1.dp,
                        progressColor.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
                    .testTag("flashing_progress_container")
            ) {
                // Header row: Status indicator + Current Action + Partition / Step
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        if (progressState.isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                        } else if (progressState.isSuccess) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = NeonEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Error",
                                tint = NeonCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = progressState.currentAction,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Partition or step badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!progressState.currentPartition.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = progressState.currentPartition.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (progressState.totalSteps > 0) {
                            Text(
                                text = "Step ${progressState.currentStep}/${progressState.totalSteps}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Percentage Indicator & Transferred Bytes row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Prominent Percentage Indicator
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.testTag("flashing_percentage_indicator")
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", displayPercent),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = progressColor,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = progressColor,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }

                    // Bytes Transferred indicator
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.testTag("flashing_bytes_indicator")
                    ) {
                        if (progressState.totalBytes > 0) {
                            Text(
                                text = "${formatBytes(progressState.bytesProcessed)} / ${formatBytes(progressState.totalBytes)}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        } else if (progressState.bytesProcessed > 0) {
                            Text(
                                text = formatBytes(progressState.bytesProcessed),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (progressState.speedMbPerSec > 0.0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.testTag("flashing_speed_indicator")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${progressState.speedMbPerSec} MB/s",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Smooth linear progress bar with high-tech track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(CyberBackground)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(5.dp))
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .testTag("flashing_linear_progress_bar"),
                        color = progressColor,
                        trackColor = Color.Transparent
                    )
                }

                // Error message banner if flashing failed
                if (progressState.hasError && !progressState.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ERROR: ${progressState.errorMessage}",
                        fontSize = 10.sp,
                        color = NeonCrimson,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live logs terminal header
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE PROTOCOL LOGS (${logs.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }

            Row {
                IconButton(
                    onClick = {
                        val text = logs.joinToString("\n") { "[${it.level}] ${it.tag}: ${it.message}" }
                        clipboardManager.setText(AnnotatedString(text))
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy logs",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear logs",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logs terminal body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxLogHeight.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CyberBackground)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awaiting Fastboot/ADB protocol packets...",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.matchParentSize()
                ) {
                    items(logs, key = { it.id }) { log ->
                        LogRowItem(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogRowItem(log: LogEntry) {
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
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
            .padding(vertical = 2.dp),
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
            text = "[${log.tag}]",
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

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024L * 1024L * 1024L -> String.format(Locale.US, "%.2f GB", bytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024.0 * 1024.0))
        bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes.toDouble() / 1024.0)
        bytes > 0 -> "$bytes B"
        else -> "0 B"
    }
}

