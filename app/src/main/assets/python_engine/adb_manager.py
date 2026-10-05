"""
ROM Flash Tool v1.0 - ADB Protocol Manager
Handles ADB device handshake, commands (reboot, shell properties), and ADB Sideload streaming.
"""

from typing import Dict, Any, Optional, Tuple
import struct


class AdbCommandId:
    A_SYNC = 0x434e5953  # SYNC
    A_CNXN = 0x4e584e43  # CNXN
    A_OPEN = 0x4e45504f  # OPEN
    A_OKAY = 0x59414b4f  # OKAY
    A_CLSE = 0x45534c43  # CLSE
    A_WRTE = 0x45545257  # WRTE
    A_AUTH = 0x48545541  # AUTH


class AdbPacket:
    """Standard ADB 24-byte packet header format."""
    HEADER_FORMAT = "<6I"  # command, arg0, arg1, data_length, data_check, magic

    def __init__(self, command: int, arg0: int, arg1: int, data: bytes = b""):
        self.command = command
        self.arg0 = arg0
        self.arg1 = arg1
        self.data = data
        self.data_length = len(data)
        self.data_check = sum(data) & 0xFFFFFFFF if data else 0
        self.magic = (command ^ 0xFFFFFFFF) & 0xFFFFFFFF

    def pack(self) -> bytes:
        header = struct.pack(
            self.HEADER_FORMAT,
            self.command,
            self.arg0,
            self.arg1,
            self.data_length,
            self.data_check,
            self.magic
        )
        return header + self.data

    @classmethod
    def unpack_header(cls, raw_24_bytes: bytes) -> Tuple[int, int, int, int, int, int]:
        if len(raw_24_bytes) < 24:
            raise ValueError(f"Packet header too short: {len(raw_24_bytes)} bytes")
        return struct.unpack(cls.HEADER_FORMAT, raw_24_bytes[:24])


class AdbManager:
    """
    Manages ADB connection lifecycle, commands, and ADB Sideload protocol for OTG.
    """
    MAX_ADB_PAYLOAD = 1024 * 1024  # 1MB modern ADB buffer
    SIDELOAD_DEFAULT_BLOCK_SIZE = 65536  # 64KB Sideload block

    def __init__(self):
        self.connected = False
        self.device_serial = "unknown"
        self.device_model = "unknown"
        self.state = "offline"  # device, recovery, sideload, unauthorized, offline
        self.sideload_file_size = 0
        self.sideload_block_size = self.SIDELOAD_DEFAULT_BLOCK_SIZE

    def create_connect_packet(self, banner: str = "host::ROMFlashTool-v1.0\0") -> bytes:
        """Builds CNXN packet for ADB handshake."""
        # Version 0x01000000 (Adb version 1.0.0), max_payload = 1MB
        data = banner.encode("utf-8")
        packet = AdbPacket(
            command=AdbCommandId.A_CNXN,
            arg0=0x01000000,
            arg1=self.MAX_ADB_PAYLOAD,
            data=data
        )
        return packet.pack()

    def create_open_service_packet(self, local_id: int, service_name: str) -> bytes:
        """Opens a stream on the device (e.g. 'sideload:size', 'shell:reboot', 'reboot:bootloader')."""
        service_bytes = service_name.encode("utf-8") + b"\0"
        packet = AdbPacket(
            command=AdbCommandId.A_OPEN,
            arg0=local_id,
            arg1=0,
            data=service_bytes
        )
        return packet.pack()

    def prepare_sideload_init(self, file_size: int, block_size: int = SIDELOAD_DEFAULT_BLOCK_SIZE) -> str:
        """Prepares service string for ADB sideload: sideload-host:file_size:block_size"""
        self.sideload_file_size = file_size
        self.sideload_block_size = block_size
        return f"sideload-host:{file_size}:{block_size}"

    def parse_sideload_block_request(self, message: bytes) -> Optional[int]:
        """
        In ADB Sideload, recovery requests blocks by sending 8 hex digits (block number)
        or 'DONE' / 'ERR'.
        Returns block index if valid request, or None.
        """
        text = message.decode("ascii", errors="replace").strip()
        if len(text) == 8:
            try:
                return int(text, 16)
            except ValueError:
                return None
        return None

    def calculate_sideload_block_range(self, block_index: int) -> Tuple[int, int]:
        """Returns (start_offset, length) for requested sideload block."""
        start = block_index * self.sideload_block_size
        if start >= self.sideload_file_size:
            return 0, 0
        length = min(self.sideload_block_size, self.sideload_file_size - start)
        return start, length

    def parse_device_info(self, connection_banner: str) -> Dict[str, str]:
        """Extracts ro.product.name, ro.product.model, and features from ADB CNXN response."""
        info = {}
        for piece in connection_banner.split(";"):
            if "=" in piece:
                k, v = piece.split("=", 1)
                info[k.strip()] = v.strip()
        if "ro.product.model" in info:
            self.device_model = info["ro.product.model"]
        return info
