"""
ROM Flash Tool v1.0 - Fastboot Protocol Manager
Handles Fastboot packet generation, command dispatch, and response parsing.
"""

from typing import Dict, Any, List, Optional, Tuple
import struct


class FastbootCommand:
    GETVAR = "getvar"
    DOWNLOAD = "download"
    FLASH = "flash"
    ERASE = "erase"
    BOOT = "boot"
    CONTINUE = "continue"
    REBOOT = "reboot"
    REBOOT_BOOTLOADER = "reboot-bootloader"
    REBOOT_RECOVERY = "reboot-recovery"
    REBOOT_FASTBOOT = "reboot-fastboot"
    SET_ACTIVE = "set_active"
    OEM = "oem"
    FLASHING_UNLOCK = "flashing unlock"
    FLASHING_LOCK = "flashing lock"


class FastbootResponse:
    """Represents a parsed Fastboot protocol response."""
    def __init__(self, status: str, payload: str = ""):
        self.status = status  # OKAY, FAIL, DATA, INFO
        self.payload = payload
        self.is_okay = status == "OKAY"
        self.is_fail = status == "FAIL"
        self.is_data = status == "DATA"
        self.is_info = status == "INFO"

    @property
    def data_size(self) -> int:
        """If response is DATA, returns expected bytes length in hex."""
        if self.is_data and self.payload:
            try:
                return int(self.payload, 16)
            except ValueError:
                return 0
        return 0

    def __repr__(self) -> str:
        return f"<FastbootResponse status={self.status} payload='{self.payload}'>"


class FastbootManager:
    """
    Core manager for Fastboot protocol operations.
    Formats commands for the USB layer and interprets responses.
    """

    DEFAULT_MAX_DOWNLOAD_SIZE = 256 * 1024 * 1024  # 256 MB safe chunking

    def __init__(self, max_download_size: int = DEFAULT_MAX_DOWNLOAD_SIZE):
        self.max_download_size = max_download_size
        self.current_slot: Optional[str] = None
        self.is_unlocked: bool = False
        self.product_name: str = "unknown"
        self.serial_no: str = "unknown"
        self.variables: Dict[str, str] = {}

    def format_command(self, cmd: str, arg: Optional[str] = None) -> bytes:
        """Encodes command to ASCII bytes suitable for Fastboot USB bulk transfer."""
        full_cmd = f"{cmd}:{arg}" if arg is not None else cmd
        return full_cmd.encode("ascii")

    def parse_response(self, raw_bytes: bytes) -> FastbootResponse:
        """Parses Fastboot 4-byte prefix response (OKAY, FAIL, DATA, INFO)."""
        if not raw_bytes or len(raw_bytes) < 4:
            return FastbootResponse("FAIL", "Empty or truncated response from device")

        prefix = raw_bytes[:4].decode("ascii", errors="replace")
        payload = raw_bytes[4:].decode("ascii", errors="replace").strip()

        if prefix in ("OKAY", "FAIL", "DATA", "INFO"):
            return FastbootResponse(prefix, payload)
        return FastbootResponse("FAIL", f"Unknown protocol response: {raw_bytes[:16]!r}")

    def prepare_download_header(self, size_in_bytes: int) -> bytes:
        """Generates 'download:<hex_size>' packet."""
        hex_size = f"{size_in_bytes:08x}"
        return self.format_command(FastbootCommand.DOWNLOAD, hex_size)

    def prepare_flash_command(self, partition: str, slot: Optional[str] = None) -> bytes:
        """Prepares flash command for specified partition and optional slot."""
        target_part = f"{partition}_{slot}" if slot and not partition.endswith(f"_{slot}") else partition
        return self.format_command(FastbootCommand.FLASH, target_part)

    def prepare_erase_command(self, partition: str, slot: Optional[str] = None) -> bytes:
        target_part = f"{partition}_{slot}" if slot and not partition.endswith(f"_{slot}") else partition
        return self.format_command(FastbootCommand.ERASE, target_part)

    def prepare_getvar_command(self, variable_name: str) -> bytes:
        return self.format_command(FastbootCommand.GETVAR, variable_name)

    def prepare_set_active_command(self, slot: str) -> bytes:
        slot = slot.lower().replace("_", "")
        return self.format_command(FastbootCommand.SET_ACTIVE, slot)

    def prepare_reboot_command(self, target: str = "system") -> bytes:
        target = target.lower()
        if target == "bootloader":
            return self.format_command(FastbootCommand.REBOOT_BOOTLOADER)
        elif target == "recovery":
            return self.format_command(FastbootCommand.REBOOT_RECOVERY)
        elif target == "fastboot":
            return self.format_command(FastbootCommand.REBOOT_FASTBOOT)
        else:
            return self.format_command(FastbootCommand.REBOOT)

    def calculate_chunks(self, total_size: int, limit: Optional[int] = None) -> List[Tuple[int, int]]:
        """
        Splits image bytes into chunks compatible with max-download-size.
        Returns list of (offset, length).
        """
        chunk_size = limit or self.max_download_size
        chunks = []
        offset = 0
        while offset < total_size:
            length = min(chunk_size, total_size - offset)
            chunks.append((offset, length))
            offset += length
        return chunks

    def update_device_var(self, var_name: str, value: str):
        self.variables[var_name] = value
        if var_name == "current-slot":
            self.current_slot = value.replace("_", "")
        elif var_name == "unlocked":
            self.is_unlocked = (value.lower() in ("yes", "true", "1"))
        elif var_name in ("product", "ro.build.product"):
            self.product_name = value
        elif var_name == "serialno":
            self.serial_no = value
        elif var_name == "max-download-size":
            try:
                # Can be hex (0x10000000) or dec
                if value.startswith("0x") or value.startswith("0X"):
                    self.max_download_size = int(value, 16)
                else:
                    self.max_download_size = int(value)
            except ValueError:
                pass
