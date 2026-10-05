package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DangerousPartitionDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.AdbSideloadScreen
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.BootImageScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FastbootFlashScreen
import com.example.ui.screens.HistoryAuditScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.ManualCommandScreen
import com.example.ui.screens.PythonEngineScreen
import com.example.ui.screens.RecoveryScreen
import com.example.ui.screens.RestoreScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RomFlashToolTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            var showSettings by remember { mutableStateOf(false) }

            RomFlashToolTheme(themeMode = themeMode) {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val deviceState by viewModel.deviceState.collectAsStateWithLifecycle()
                val flashProgress by viewModel.flashProgress.collectAsStateWithLifecycle()
                val logs by viewModel.logs.collectAsStateWithLifecycle()
                val flashHistory by viewModel.flashHistory.collectAsStateWithLifecycle()
                val backupRecords by viewModel.backupRecords.collectAsStateWithLifecycle()

                val selectedRom by viewModel.selectedRom.collectAsStateWithLifecycle()
                val selectedSlot by viewModel.selectedSlot.collectAsStateWithLifecycle()
                val wipeUserdata by viewModel.wipeUserdata.collectAsStateWithLifecycle()
                val disableVerity by viewModel.disableVerity.collectAsStateWithLifecycle()
                val autoReboot by viewModel.autoReboot.collectAsStateWithLifecycle()

                val targetPartition by viewModel.targetPartition.collectAsStateWithLifecycle()
                val inspectionResult by viewModel.selectedImageResult.collectAsStateWithLifecycle()
                val selectedBackupPartitions by viewModel.selectedBackupPartitions.collectAsStateWithLifecycle()
                val presets by viewModel.presets.collectAsStateWithLifecycle()
                val storageLayout by viewModel.storageLayout.collectAsStateWithLifecycle()
                val isScanningStorage by viewModel.isScanningStorage.collectAsStateWithLifecycle()
                val manualCommandHistory by viewModel.manualCommandHistory.collectAsStateWithLifecycle()
                val isExecutingManualCommand by viewModel.isExecutingManualCommand.collectAsStateWithLifecycle()
                val context = androidx.compose.ui.platform.LocalContext.current

                val dangerWarning by viewModel.dangerousPartitionWarning.collectAsStateWithLifecycle()

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ElectricCyan.copy(alpha = 0.2f))
                                            .border(1.dp, ElectricCyan, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ROM Flash Tool v1.0",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Fastboot & ADB Protocol Engine",
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { showSettings = true },
                                    modifier = Modifier.testTag("settings_top_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = ElectricCyan
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = CyberSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CyberSurface,
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar")
                        ) {
                            val items = listOf(
                                Triple(NavigationTab.DASHBOARD, "Home", Icons.Default.Dashboard),
                                Triple(NavigationTab.FASTBOOT_FLASH, "Fastboot", Icons.Default.FlashOn),
                                Triple(NavigationTab.BOOT_IMAGE, "Partitions", Icons.Default.Memory),
                                Triple(NavigationTab.ADB_SIDELOAD, "Sideload", Icons.Default.InstallMobile),
                                Triple(NavigationTab.BACKUP, "Backup", Icons.Default.Backup),
                                Triple(NavigationTab.RESTORE, "Restore", Icons.Default.Restore),
                                Triple(NavigationTab.PYTHON_ENGINE, "Python", Icons.Default.Code),
                                Triple(NavigationTab.LOGS, "Logs", Icons.Default.Terminal)
                            )

                            // Show primary tabs in bottom bar, others reachable via Dashboard
                            val bottomTabs = listOf(
                                Triple(NavigationTab.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
                                Triple(NavigationTab.FASTBOOT_FLASH, "Fastboot", Icons.Default.FlashOn),
                                Triple(NavigationTab.MANUAL_COMMAND, "Console", Icons.Default.Terminal),
                                Triple(NavigationTab.BOOT_IMAGE, "Boot.img", Icons.Default.Memory),
                                Triple(NavigationTab.HISTORY, "Audit", Icons.Default.History),
                                Triple(NavigationTab.LOGS, "Logs", Icons.Default.Code)
                            )

                            bottomTabs.forEach { (tab, label, icon) ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.setTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CyberBackground,
                                        selectedTextColor = ElectricCyan,
                                        indicatorColor = ElectricCyan,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(CyberBackground)
                    ) {
                        Crossfade(targetState = currentTab, label = "tabCrossfade") { tab ->
                            when (tab) {
                                NavigationTab.DASHBOARD -> DashboardScreen(
                                    deviceState = deviceState,
                                    progressState = flashProgress,
                                    logs = logs,
                                    history = flashHistory,
                                    onNavigate = { viewModel.setTab(it) },
                                    onScanUsb = { viewModel.scanDevices() },
                                    onToggleSim = { viewModel.toggleSimulationMode(it) },
                                    onReboot = { viewModel.rebootDevice(it) },
                                    onClearLogs = { viewModel.clearLogs() },
                                    onOpenSettings = { showSettings = true },
                                    onSimulateAuthStatus = { viewModel.setSimulatedAuthStatus(it) }
                                )
                                NavigationTab.FASTBOOT_FLASH -> FastbootFlashScreen(
                                    deviceState = deviceState,
                                    selectedRom = selectedRom,
                                    selectedSlot = selectedSlot,
                                    wipeUserdata = wipeUserdata,
                                    disableVerity = disableVerity,
                                    autoReboot = autoReboot,
                                    progressState = flashProgress,
                                    logs = logs,
                                    presets = presets,
                                    onApplyPreset = { viewModel.applyPreset(it) },
                                    onSaveCustomPreset = { n, d, p, v, w -> viewModel.saveCustomPreset(n, d, p, v, w) },
                                    onDeleteCustomPreset = { viewModel.deleteCustomPreset(it) },
                                    onSelectSlot = { viewModel.setSelectedSlot(it) },
                                    onSetWipeUserdata = { viewModel.setWipeUserdata(it) },
                                    onSetDisableVerity = { viewModel.setDisableVerity(it) },
                                    onSetAutoReboot = { viewModel.setAutoReboot(it) },
                                    onStartFlash = { viewModel.flashFastbootRom() },
                                    onLoadSampleRom = { viewModel.loadSampleRom(it) },
                                    onScanUsb = { viewModel.scanDevices() },
                                    onToggleSim = { viewModel.toggleSimulationMode(it) },
                                    onClearLogs = { viewModel.clearLogs() },
                                    onSimulateAuthStatus = { viewModel.setSimulatedAuthStatus(it) }
                                )
                                NavigationTab.BOOT_IMAGE -> BootImageScreen(
                                    deviceState = deviceState,
                                    targetPartition = targetPartition,
                                    inspectionResult = inspectionResult,
                                    progressState = flashProgress,
                                    logs = logs,
                                    onSelectPartition = { viewModel.setTargetPartition(it) },
                                    onStartFlash = { viewModel.flashSingleImage() },
                                    onScanUsb = { viewModel.scanDevices() },
                                    onToggleSim = { viewModel.toggleSimulationMode(it) },
                                    onClearLogs = { viewModel.clearLogs() }
                                )
                                NavigationTab.RECOVERY -> RecoveryScreen(
                                    deviceState = deviceState,
                                    progressState = flashProgress,
                                    logs = logs,
                                    onRebootRecovery = { viewModel.rebootDevice("recovery") },
                                    onFlashRecoveryRom = { viewModel.flashFastbootRom() },
                                    onScanUsb = { viewModel.scanDevices() },
                                    onToggleSim = { viewModel.toggleSimulationMode(it) },
                                    onClearLogs = { viewModel.clearLogs() }
                                )
                                NavigationTab.ADB_SIDELOAD -> AdbSideloadScreen(
                                    deviceState = deviceState,
                                    progressState = flashProgress,
                                    logs = logs,
                                    autoReboot = autoReboot,
                                    onSetAutoReboot = { viewModel.setAutoReboot(it) },
                                    onStartSideload = { name, sz -> viewModel.startAdbSideload(name, sz) },
                                    onScanUsb = { viewModel.scanDevices() },
                                    onToggleSim = { viewModel.toggleSimulationMode(it) },
                                    onClearLogs = { viewModel.clearLogs() }
                                )
                                NavigationTab.BACKUP -> BackupScreen(
                                    deviceState = deviceState,
                                    selectedPartitions = selectedBackupPartitions,
                                    progressState = flashProgress,
                                    logs = logs,
                                    storageResult = storageLayout,
                                    isScanningStorage = isScanningStorage,
                                    onTogglePartition = { viewModel.toggleBackupPartition(it) },
                                    onStartBackup = { viewModel.startPartitionBackup() },
                                    onScanUsb = { viewModel.scanDevices() },
                                    onToggleSim = { viewModel.toggleSimulationMode(it) },
                                    onClearLogs = { viewModel.clearLogs() },
                                    onScanStorage = { viewModel.scanDevicePartitionTable() },
                                    onSelectAllBootKernel = { viewModel.selectAllBootKernelPartitions() },
                                    onSelectAllStorage = { viewModel.selectAllStoragePartitions() },
                                    onClearSelection = { viewModel.clearBackupSelection() },
                                    onSimulateAuthStatus = { viewModel.setSimulatedAuthStatus(it) }
                                )
                                NavigationTab.RESTORE -> RestoreScreen(
                                    deviceState = deviceState,
                                    backups = backupRecords,
                                    progressState = flashProgress,
                                    logs = logs,
                                    onRestoreBackup = { viewModel.startPartitionRestore(it) },
                                    onScanUsb = { viewModel.scanDevices() },
                                    onToggleSim = { viewModel.toggleSimulationMode(it) },
                                    onClearLogs = { viewModel.clearLogs() }
                                )
                                NavigationTab.HISTORY -> HistoryAuditScreen(
                                    history = flashHistory,
                                    onDeleteRecord = { viewModel.deleteHistoryItem(it) },
                                    onClearAll = { viewModel.clearAuditHistory() },
                                    onExportSave = { isCsv -> viewModel.exportAndSaveReport(context, isCsv) },
                                    onExportShare = { isCsv -> viewModel.shareAuditReport(context, isCsv) }
                                )
                                NavigationTab.PYTHON_ENGINE -> PythonEngineScreen(
                                    pythonEngine = viewModel.pythonEngine
                                )
                                NavigationTab.LOGS -> LogsScreen(
                                    logs = logs,
                                    onClearLogs = { viewModel.clearLogs() }
                                )
                                NavigationTab.MANUAL_COMMAND -> ManualCommandScreen(
                                    deviceState = deviceState,
                                    commandHistory = manualCommandHistory,
                                    isExecuting = isExecutingManualCommand,
                                    onExecuteCommand = { cmd, proto -> viewModel.executeManualCommand(cmd, proto) },
                                    onClearHistory = { viewModel.clearManualCommandHistory() },
                                    onScanUsb = { viewModel.scanDevices() },
                                    onToggleSim = { viewModel.toggleSimulationMode(it) },
                                    onSimulateAuthStatus = { viewModel.setSimulatedAuthStatus(it) }
                                )
                            }
                        }

                        // Dangerous partition modal confirmation
                        dangerWarning?.let { warning ->
                            DangerousPartitionDialog(
                                warningMessage = warning,
                                onConfirm = { viewModel.confirmDangerousAction() },
                                onDismiss = { viewModel.dismissDangerDialog() }
                            )
                        }

                        // Settings and Theme Dialog
                        if (showSettings) {
                            SettingsDialog(
                                currentThemeMode = themeMode,
                                onSelectThemeMode = { viewModel.setThemeMode(it) },
                                onDismiss = { showSettings = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
