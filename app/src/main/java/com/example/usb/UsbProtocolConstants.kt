package com.example.usb

object UsbProtocolConstants {
    const val USB_CLASS_VENDOR_SPEC = 0xFF
    const val USB_SUBCLASS_FASTBOOT = 0x42
    const val USB_PROTOCOL_FASTBOOT = 0x03

    const val USB_SUBCLASS_ADB = 0x42
    const val USB_PROTOCOL_ADB = 0x01

    const val FASTBOOT_RESPONSE_OKAY = "OKAY"
    const val FASTBOOT_RESPONSE_FAIL = "FAIL"
    const val FASTBOOT_RESPONSE_DATA = "DATA"
    const val FASTBOOT_RESPONSE_INFO = "INFO"

    const val TIMEOUT_CMD_MS = 5000
    const val TIMEOUT_DATA_MS = 30000
    const val DEFAULT_CHUNK_SIZE = 1024 * 1024 * 16 // 16 MB chunks
}
