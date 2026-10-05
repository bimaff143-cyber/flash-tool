package com.example.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import com.example.model.DeviceConnectionType
import com.example.model.DeviceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

class UsbCommunicationLayer(
    private val context: Context,
    private val onLog: (String, String) -> Unit
) {
    companion object {
        private const val TAG = "UsbLayer"
        const val ACTION_USB_PERMISSION = "com.example.romflashtool.USB_PERMISSION"
    }

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    private var activeDevice: UsbDevice? = null
    private var activeInterface: UsbInterface? = null
    private var connection: UsbDeviceConnection? = null
    private var inEndpoint: UsbEndpoint? = null
    private var outEndpoint: UsbEndpoint? = null

    // Simulation fallback
    private var isSimulated: Boolean = false
    private var simulatedDevice: DeviceState = DeviceState(
        connectionType = DeviceConnectionType.SIMULATED_FASTBOOT,
        authStatus = com.example.model.DeviceAuthStatus.AUTHORIZED,
        serialNumber = "SIM-PX7PRO-94821",
        productName = "cheetah",
        isUnlocked = true,
        currentSlot = "a",
        isAbDevice = true,
        batteryPercent = 92,
        maxDownloadSize = 256 * 1024 * 1024L,
        isSimulated = true,
        fastbootVersion = "0.5-preview",
        statusMessage = "Authorized & Ready"
    )

    var onDeviceStateListener: ((DeviceState) -> Unit)? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            onLog("FASTBOOT", "USB Permission granted for device: ${it.deviceName}")
                            val connectedState = connectDevice(it)
                            onDeviceStateListener?.invoke(connectedState)
                        }
                    } else {
                        onLog("WARN", "USB Permission denied by user for device: ${device?.deviceName}")
                        val unauthState = DeviceState(
                            connectionType = DeviceConnectionType.DISCONNECTED,
                            authStatus = com.example.model.DeviceAuthStatus.UNAUTHORIZED,
                            serialNumber = device?.deviceName ?: "Unauthorized",
                            productName = "Access Denied",
                            statusMessage = "Unauthorized: USB permission denied"
                        )
                        onDeviceStateListener?.invoke(unauthState)
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                onLog("INFO", "USB OTG Device Attached! Real-time monitor scanning...")
                val state = scanForConnectedDevices()
                onDeviceStateListener?.invoke(state)
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                onLog("WARN", "USB OTG Device Detached.")
                disconnect()
                val disState = DeviceState(
                    connectionType = DeviceConnectionType.DISCONNECTED,
                    authStatus = com.example.model.DeviceAuthStatus.DISCONNECTED,
                    statusMessage = "Device Detached"
                )
                onDeviceStateListener?.invoke(disState)
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }
    }

    fun cleanup() {
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (_: Exception) {}
        disconnect()
    }

    fun setSimulationMode(enabled: Boolean, productPreset: String = "cheetah") {
        isSimulated = enabled
        if (enabled) {
            val isXiaomi = productPreset == "fuxi"
            simulatedDevice = DeviceState(
                connectionType = DeviceConnectionType.SIMULATED_FASTBOOT,
                authStatus = com.example.model.DeviceAuthStatus.AUTHORIZED,
                serialNumber = if (isXiaomi) "SIM-XI13-88219" else "SIM-PX7PRO-94821",
                productName = productPreset,
                isUnlocked = true,
                currentSlot = "a",
                isAbDevice = true,
                batteryPercent = 88,
                maxDownloadSize = 256 * 1024 * 1024L,
                isSimulated = true,
                fastbootVersion = "0.5",
                statusMessage = "Authorized & Ready"
            )
            onLog("FASTBOOT", "Simulator active: ${simulatedDevice.productName} (${simulatedDevice.serialNumber}) in Fastboot mode.")
            onDeviceStateListener?.invoke(simulatedDevice)
        } else {
            onLog("INFO", "Exited simulator mode. Scanning for physical USB OTG devices...")
            val state = scanForConnectedDevices()
            onDeviceStateListener?.invoke(state)
        }
    }

    fun setSimulationAuthStatus(status: com.example.model.DeviceAuthStatus) {
        simulatedDevice = simulatedDevice.copy(
            authStatus = status,
            statusMessage = when (status) {
                com.example.model.DeviceAuthStatus.AUTHORIZED -> "Authorized & Ready"
                com.example.model.DeviceAuthStatus.UNAUTHORIZED -> "Unauthorized - Check device screen"
                com.example.model.DeviceAuthStatus.OFFLINE -> "Device Offline / In sleep mode"
                com.example.model.DeviceAuthStatus.DISCONNECTED -> "No device detected"
            }
        )
        onDeviceStateListener?.invoke(simulatedDevice)
        onLog("INFO", "Status monitor updated: $status (${simulatedDevice.statusMessage})")
    }

    fun scanForConnectedDevices(): DeviceState {
        if (isSimulated) {
            return simulatedDevice
        }

        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) {
            return DeviceState(
                connectionType = DeviceConnectionType.DISCONNECTED,
                authStatus = com.example.model.DeviceAuthStatus.DISCONNECTED,
                statusMessage = "No device detected"
            )
        }

        for ((_, device) in deviceList) {
            val (connType, iface) = inspectDeviceInterfaces(device)
            if (connType != DeviceConnectionType.DISCONNECTED && iface != null) {
                if (!usbManager.hasPermission(device)) {
                    onLog("INFO", "Fastboot/ADB device detected: ${device.deviceName}. Requesting USB permission...")
                    requestUsbPermission(device)
                    return DeviceState(
                        connectionType = connType,
                        authStatus = com.example.model.DeviceAuthStatus.UNAUTHORIZED,
                        serialNumber = "Unauthorized",
                        productName = "Pending Authorization",
                        vendorId = device.vendorId,
                        productId = device.productId,
                        statusMessage = "Unauthorized - Accept USB debugging / permission on device"
                    )
                } else {
                    return connectDevice(device)
                }
            }
        }

        return DeviceState(
            connectionType = DeviceConnectionType.DISCONNECTED,
            authStatus = com.example.model.DeviceAuthStatus.DISCONNECTED,
            statusMessage = "No device detected"
        )
    }

    private fun requestUsbPermission(device: UsbDevice) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            flags
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    private fun inspectDeviceInterfaces(device: UsbDevice): Pair<DeviceConnectionType, UsbInterface?> {
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            // Fastboot: 255, 66, 3
            if (iface.interfaceClass == UsbProtocolConstants.USB_CLASS_VENDOR_SPEC &&
                iface.interfaceSubclass == UsbProtocolConstants.USB_SUBCLASS_FASTBOOT &&
                iface.interfaceProtocol == UsbProtocolConstants.USB_PROTOCOL_FASTBOOT
            ) {
                return Pair(DeviceConnectionType.FASTBOOT_CONNECTED, iface)
            }
            // ADB: 255, 66, 1
            if (iface.interfaceClass == UsbProtocolConstants.USB_CLASS_VENDOR_SPEC &&
                iface.interfaceSubclass == UsbProtocolConstants.USB_SUBCLASS_ADB &&
                iface.interfaceProtocol == UsbProtocolConstants.USB_PROTOCOL_ADB
            ) {
                return Pair(DeviceConnectionType.ADB_CONNECTED, iface)
            }
        }
        return Pair(DeviceConnectionType.DISCONNECTED, null)
    }

    private fun connectDevice(device: UsbDevice): DeviceState {
        val (connType, iface) = inspectDeviceInterfaces(device)
        if (iface == null) {
            return DeviceState(connectionType = DeviceConnectionType.DISCONNECTED)
        }

        val conn = usbManager.openDevice(device)
        if (conn == null) {
            onLog("ERROR", "Failed to open USB device connection: ${device.deviceName}")
            return DeviceState(connectionType = DeviceConnectionType.DISCONNECTED)
        }

        if (!conn.claimInterface(iface, true)) {
            onLog("ERROR", "Failed to claim USB interface #${iface.id}")
            conn.close()
            return DeviceState(connectionType = DeviceConnectionType.DISCONNECTED)
        }

        var inEp: UsbEndpoint? = null
        var outEp: UsbEndpoint? = null

        for (e in 0 until iface.endpointCount) {
            val ep = iface.getEndpoint(e)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_IN) {
                    inEp = ep
                } else {
                    outEp = ep
                }
            }
        }

        if (inEp == null || outEp == null) {
            onLog("ERROR", "Missing bulk endpoints on device interface")
            conn.releaseInterface(iface)
            conn.close()
            return DeviceState(connectionType = DeviceConnectionType.DISCONNECTED)
        }

        activeDevice = device
        activeInterface = iface
        connection = conn
        inEndpoint = inEp
        outEndpoint = outEp

        val serial = try {
            conn.serial ?: device.deviceName
        } catch (_: Exception) {
            device.deviceName
        }

        onLog(
            if (connType == DeviceConnectionType.FASTBOOT_CONNECTED) "FASTBOOT" else "ADB",
            "Connected to physical USB device: $serial (VID: 0x${device.vendorId.toString(16)}, PID: 0x${device.productId.toString(16)})"
        )

        return DeviceState(
            connectionType = connType,
            authStatus = com.example.model.DeviceAuthStatus.AUTHORIZED,
            serialNumber = serial,
            productName = device.productName ?: "Android Device",
            vendorId = device.vendorId,
            productId = device.productId,
            statusMessage = if (connType == DeviceConnectionType.FASTBOOT_CONNECTED) "Fastboot Device Authorized" else "ADB Device Authorized"
        )
    }

    fun disconnect() {
        try {
            connection?.let { conn ->
                activeInterface?.let { iface ->
                    conn.releaseInterface(iface)
                }
                conn.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting USB", e)
        } finally {
            connection = null
            activeInterface = null
            activeDevice = null
            inEndpoint = null
            outEndpoint = null
        }
    }

    suspend fun executeFastbootCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (isSimulated) {
            delay(120) // simulate USB wire latency
            return@withContext simulateFastbootResponse(command)
        }

        val conn = connection
        val outEp = outEndpoint
        val inEp = inEndpoint

        if (conn == null || outEp == null || inEp == null) {
            return@withContext Pair(false, "Device not connected via USB")
        }

        val cmdBytes = command.toByteArray(StandardCharsets.US_ASCII)
        val sent = conn.bulkTransfer(outEp, cmdBytes, cmdBytes.size, UsbProtocolConstants.TIMEOUT_CMD_MS)
        if (sent < 0) {
            return@withContext Pair(false, "Failed to send command over bulk OUT endpoint")
        }

        val responseBuffer = ByteArray(4096)
        val received = conn.bulkTransfer(inEp, responseBuffer, responseBuffer.size, UsbProtocolConstants.TIMEOUT_CMD_MS)
        if (received < 4) {
            return@withContext Pair(false, "Empty or truncated response received from device")
        }

        val prefix = String(responseBuffer, 0, 4, StandardCharsets.US_ASCII)
        val payload = if (received > 4) {
            String(responseBuffer, 4, received - 4, StandardCharsets.US_ASCII).trim()
        } else ""

        when (prefix) {
            UsbProtocolConstants.FASTBOOT_RESPONSE_OKAY -> Pair(true, payload)
            UsbProtocolConstants.FASTBOOT_RESPONSE_INFO -> Pair(true, payload)
            UsbProtocolConstants.FASTBOOT_RESPONSE_DATA -> Pair(true, "DATA:$payload")
            else -> Pair(false, if (payload.isNotEmpty()) payload else "Command rejected with $prefix")
        }
    }

    suspend fun sendFastbootDataChunk(
        data: ByteArray,
        offset: Int,
        length: Int,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        if (isSimulated) {
            // Simulate realistic USB transfer rate ~35MB/s
            val subChunks = (length / 65536).coerceAtLeast(1)
            for (i in 0 until subChunks) {
                delay(15)
                onProgress(65536)
            }
            return@withContext true
        }

        val conn = connection ?: return@withContext false
        val outEp = outEndpoint ?: return@withContext false

        var sentTotal = 0
        val subChunkSize = 65536 // 64KB per USB bulk transfer

        while (sentTotal < length) {
            val toSend = minOf(subChunkSize, length - sentTotal)
            val written = conn.bulkTransfer(
                outEp,
                data,
                offset + sentTotal,
                toSend,
                UsbProtocolConstants.TIMEOUT_DATA_MS
            )
            if (written < 0) {
                return@withContext false
            }
            sentTotal += written
            onProgress(written)
        }
        true
    }

    suspend fun executeAdbCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cleanCmd = command.trim().removePrefix("adb ").trim()
        if (isSimulated) {
            delay(100)
            return@withContext simulateAdbResponse(cleanCmd)
        }

        val conn = connection
        val outEp = outEndpoint
        val inEp = inEndpoint
        if (conn == null || outEp == null || inEp == null) {
            return@withContext Pair(false, "Device not connected via USB")
        }

        val cmdBytes = cleanCmd.toByteArray(StandardCharsets.US_ASCII)
        val sent = conn.bulkTransfer(outEp, cmdBytes, cmdBytes.size, UsbProtocolConstants.TIMEOUT_CMD_MS)
        if (sent < 0) {
            return@withContext Pair(false, "Failed to send ADB command packet")
        }

        val responseBuffer = ByteArray(4096)
        val received = conn.bulkTransfer(inEp, responseBuffer, responseBuffer.size, UsbProtocolConstants.TIMEOUT_CMD_MS)
        if (received <= 0) {
            return@withContext Pair(false, "No response received from ADB daemon")
        }

        val responseStr = String(responseBuffer, 0, received, StandardCharsets.UTF_8).trim()
        Pair(true, responseStr)
    }

    private fun simulateAdbResponse(command: String): Pair<Boolean, String> {
        val clean = command.lowercase().trim()
        return when {
            clean == "devices" -> Pair(true, "List of devices attached\n${simulatedDevice.serialNumber}\tdevice")
            clean == "get-state" -> Pair(true, "device")
            clean.contains("ro.product.model") -> Pair(true, simulatedDevice.productName.uppercase())
            clean.contains("ro.build.version.release") -> Pair(true, "14")
            clean.contains("ro.boot.slot_suffix") -> Pair(true, "_${simulatedDevice.currentSlot}")
            clean.contains("ro.build.version.sdk") -> Pair(true, "34")
            clean.startsWith("shell uname") -> Pair(true, "Linux localhost 5.15.131-android14-9-ge7 #1 SMP PREEMPT aarch64")
            clean.startsWith("shell id") -> Pair(true, "uid=0(root) gid=0(root) groups=0(root) context=u:r:su:s0")
            clean.startsWith("reboot bootloader") -> Pair(true, "Rebooting to fastboot bootloader mode...")
            clean.startsWith("reboot recovery") -> Pair(true, "Rebooting to recovery mode...")
            clean.startsWith("reboot sideload") -> Pair(true, "Rebooting to recovery sideload mode...")
            clean.startsWith("reboot") -> Pair(true, "Rebooting target device...")
            clean.startsWith("shell") -> Pair(true, "Executed: ${command.removePrefix("shell").trim()}\nexit status 0")
            else -> Pair(true, "OKAY: executed '$command'")
        }
    }

    private fun simulateFastbootResponse(command: String): Pair<Boolean, String> {
        val clean = command.trim().removePrefix("fastboot ").trim()
        return when {
            clean == "devices" -> Pair(true, "${simulatedDevice.serialNumber}\tfastboot")
            clean == "getvar all" || clean == "getvar:all" -> Pair(
                true,
                "(bootloader) product: ${simulatedDevice.productName}\n" +
                "(bootloader) serialno: ${simulatedDevice.serialNumber}\n" +
                "(bootloader) unlocked: ${if (simulatedDevice.isUnlocked) "yes" else "no"}\n" +
                "(bootloader) current-slot: ${simulatedDevice.currentSlot}\n" +
                "(bootloader) slot-count: 2\n" +
                "(bootloader) max-download-size: 0x10000000\n" +
                "(bootloader) version-bootloader: cheetah-1.0-cloud\n" +
                "(bootloader) battery-soc-ok: yes\n" +
                "(bootloader) is-userspace: no\n" +
                "all: done"
            )
            clean == "oem device-info" || clean == "oem:device-info" -> Pair(
                true,
                "(bootloader) Device tampered: false\n" +
                "(bootloader) Device unlocked: ${simulatedDevice.isUnlocked}\n" +
                "(bootloader) Device critical unlocked: ${simulatedDevice.isUnlocked}\n" +
                "(bootloader) Charger screen enabled: true\n" +
                "OKAY"
            )
            clean.startsWith("flashing get_unlock_ability") -> Pair(true, "(bootloader) get_unlock_ability: 1\nOKAY")
            clean.startsWith("getvar:unlocked") || clean == "getvar unlocked" -> Pair(true, if (simulatedDevice.isUnlocked) "yes" else "no")
            clean.startsWith("getvar:current-slot") || clean == "getvar current-slot" -> Pair(true, simulatedDevice.currentSlot)
            clean.startsWith("getvar:has-slot:boot") -> Pair(true, "yes")
            clean.startsWith("getvar:slot-count") -> Pair(true, "2")
            clean.startsWith("getvar:product") || clean == "getvar product" -> Pair(true, simulatedDevice.productName)
            clean.startsWith("getvar:serialno") || clean == "getvar serialno" -> Pair(true, simulatedDevice.serialNumber)
            clean.startsWith("getvar:max-download-size") || clean == "getvar max-download-size" -> Pair(true, "0x10000000") // 256MB
            clean.startsWith("getvar:version-bootloader") -> Pair(true, "cloud-fastboot-1.0")
            clean.startsWith("download:") -> Pair(true, "DATA:" + clean.substringAfter(":"))
            clean.startsWith("flash:") -> Pair(true, "OKAY [Flashing ${clean.substringAfter(":")} completed]")
            clean.startsWith("erase:") -> Pair(true, "OKAY [Partition wiped]")
            clean.startsWith("set_active:") -> {
                val newSlot = clean.substringAfter(":")
                simulatedDevice = simulatedDevice.copy(currentSlot = newSlot)
                Pair(true, "OKAY [Slot changed to $newSlot]")
            }
            clean.startsWith("reboot-bootloader") -> Pair(true, "OKAY [Rebooting to bootloader]")
            clean.startsWith("reboot-recovery") || clean == "reboot recovery" -> Pair(true, "OKAY [Rebooting to recovery]")
            clean.startsWith("reboot-fastboot") || clean == "reboot fastboot" -> Pair(true, "OKAY [Rebooting to fastbootd userspace]")
            clean.startsWith("reboot") -> Pair(true, "OKAY [Rebooting target device]")
            clean.startsWith("boot") -> Pair(true, "OKAY [Booting custom kernel in RAM]")
            else -> Pair(true, "OKAY")
        }
    }
}
