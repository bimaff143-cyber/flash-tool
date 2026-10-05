package com.example.model

enum class DeviceConnectionType {
    DISCONNECTED,
    FASTBOOT_CONNECTED,
    ADB_CONNECTED,
    SIMULATED_FASTBOOT,
    SIMULATED_ADB
}

enum class DeviceAuthStatus {
    AUTHORIZED,     // Device connected and authorized (ready for commands)
    UNAUTHORIZED,   // Device connected but waiting for USB Debugging RSA confirmation or USB permission
    OFFLINE,        // Device connected via USB bus but unresponsive / sleeping
    DISCONNECTED    // No physical or simulated device connected
}

data class DeviceState(
    val connectionType: DeviceConnectionType = DeviceConnectionType.DISCONNECTED,
    val authStatus: DeviceAuthStatus = DeviceAuthStatus.DISCONNECTED,
    val serialNumber: String = "No Device",
    val productName: String = "Unknown",
    val isUnlocked: Boolean = false,
    val currentSlot: String = "a",
    val isAbDevice: Boolean = true,
    val batteryPercent: Int = 85,
    val maxDownloadSize: Long = 256 * 1024 * 1024L,
    val isSimulated: Boolean = false,
    val vendorId: Int = 0,
    val productId: Int = 0,
    val fastbootVersion: String = "0.5",
    val statusMessage: String = "Disconnected"
) {
    val isConnected: Boolean
        get() = connectionType != DeviceConnectionType.DISCONNECTED && authStatus != DeviceAuthStatus.DISCONNECTED

    val isAuthorized: Boolean
        get() = authStatus == DeviceAuthStatus.AUTHORIZED

    val isUnauthorized: Boolean
        get() = authStatus == DeviceAuthStatus.UNAUTHORIZED

    val isOffline: Boolean
        get() = authStatus == DeviceAuthStatus.OFFLINE

    val isFastboot: Boolean
        get() = connectionType == DeviceConnectionType.FASTBOOT_CONNECTED || connectionType == DeviceConnectionType.SIMULATED_FASTBOOT

    val isAdb: Boolean
        get() = connectionType == DeviceConnectionType.ADB_CONNECTED || connectionType == DeviceConnectionType.SIMULATED_ADB
}
