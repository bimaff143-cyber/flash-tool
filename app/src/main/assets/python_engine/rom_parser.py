"""
ROM Flash Tool v1.0 - ROM Archive and Payload Parser
Parses Fastboot ROM zip archives, Recovery OTA zips, payload.bin, and sparse images.
"""

from typing import Dict, Any, List, Optional
import struct
import os


class RomType:
    FASTBOOT_PACKAGE = "FASTBOOT_PACKAGE"
    RECOVERY_OTA = "RECOVERY_OTA"
    PAYLOAD_BIN = "PAYLOAD_BIN"
    RAW_IMAGE = "RAW_IMAGE"
    UNKNOWN = "UNKNOWN"


class SparseImageHeader:
    """Header of Android sparse image format (.img)."""
    MAGIC = 0xED26FF3A

    def __init__(self, raw_28_bytes: bytes):
        if len(raw_28_bytes) < 28:
            raise ValueError("Sparse image header too short")
        (
            self.magic,
            self.major_version,
            self.minor_version,
            self.file_hdr_sz,
            self.chunk_hdr_sz,
            self.blk_sz,
            self.total_blks,
            self.total_chunks,
            self.image_checksum
        ) = struct.unpack("<IHHHHIIII", raw_28_bytes[:28])

    @property
    def is_valid_sparse(self) -> bool:
        return self.magic == self.MAGIC


class RomParser:
    """
    Analyzes archive entries, identifies ROM structure, and generates partition flash lists.
    """

    SUPPORTED_PARTITIONS = [
        "boot",
        "init_boot",
        "vendor_boot",
        "recovery",
        "dtbo",
        "vbmeta",
        "vbmeta_system",
        "vbmeta_vendor",
        "super",
        "system",
        "system_ext",
        "vendor",
        "product",
        "odm",
        "cust",
        "userdata",
        "metadata",
        "modem",
        "radio",
        "abl",
        "xbl",
        "tz",
        "rpm",
        "dsp"
    ]

    def __init__(self):
        self.rom_type = RomType.UNKNOWN
        self.target_product: Optional[str] = None
        self.partitions_found: Dict[str, Dict[str, Any]] = {}
        self.flash_script: Optional[str] = None
        self.is_ab_device = False

    def identify_rom_type(self, file_name: str, file_entries: List[str]) -> str:
        """Determines ROM package classification from contained file names."""
        name_lower = file_name.lower()

        if name_lower.endswith(".bin") or "payload.bin" in file_entries:
            self.rom_type = RomType.PAYLOAD_BIN
            return self.rom_type

        has_android_info = any("android-info.txt" in e.lower() for e in file_entries)
        has_flash_all = any("flash-all." in e.lower() for e in file_entries)

        if has_android_info or has_flash_all:
            self.rom_type = RomType.FASTBOOT_PACKAGE
            return self.rom_type

        has_updater_script = any("updater-script" in e.lower() for e in file_entries)
        has_update_binary = any("update-binary" in e.lower() for e in file_entries)

        if has_updater_script or has_update_binary or "payload.bin" in file_entries:
            self.rom_type = RomType.RECOVERY_OTA
            return self.rom_type

        if any(name_lower.endswith(ext) for ext in (".img", ".bin", ".iso")):
            self.rom_type = RomType.RAW_IMAGE
            return self.rom_type

        # Check if contains multiple partition images
        img_count = sum(1 for e in file_entries if e.lower().endswith(".img"))
        if img_count >= 2:
            self.rom_type = RomType.FASTBOOT_PACKAGE
        else:
            self.rom_type = RomType.UNKNOWN

        return self.rom_type

    def parse_android_info(self, content: str) -> Dict[str, Any]:
        """Parses fastboot android-info.txt for product name and board requirements."""
        info = {
            "require_products": [],
            "require_version_bootloader": None,
            "require_version_baseband": None
        }
        for line in content.splitlines():
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if line.startswith("require product="):
                val = line.split("=", 1)[1].strip()
                # could be require product=cheetah|panther
                info["require_products"] = [p.strip() for p in val.split("|")]
                if info["require_products"]:
                    self.target_product = info["require_products"][0]
            elif line.startswith("require version-bootloader="):
                info["require_version_bootloader"] = line.split("=", 1)[1].strip()
            elif line.startswith("require version-baseband="):
                info["require_version_baseband"] = line.split("=", 1)[1].strip()

        return info

    def build_partition_flash_plan(self, file_entries: List[str], target_slot: Optional[str] = None) -> List[Dict[str, Any]]:
        """
        Builds prioritized flash sequence:
        1. bootloader / radio / xbl / abl (if fastboot package)
        2. vbmeta (disable verity options)
        3. dtbo
        4. boot, init_boot, vendor_boot, recovery
        5. super / system / vendor / product
        """
        sequence = []
        priority_order = [
            "xbl", "abl", "tz", "rpm", "hyp", "pmic", "bootloader",
            "modem", "radio",
            "vbmeta", "vbmeta_system", "vbmeta_vendor",
            "dtbo",
            "boot", "init_boot", "vendor_boot", "recovery",
            "super", "system", "system_ext", "vendor", "product", "odm", "cust"
        ]

        found_map = {}
        for entry in file_entries:
            base_name = os.path.basename(entry).lower()
            for part in self.SUPPORTED_PARTITIONS:
                if base_name == f"{part}.img" or base_name == f"{part}.bin":
                    found_map[part] = entry
                    break

        for part in priority_order:
            if part in found_map:
                sequence.append({
                    "partition": part,
                    "file_path": found_map[part],
                    "target_slot": target_slot,
                    "is_dangerous": part in ("vbmeta", "xbl", "abl", "bootloader", "modem", "radio")
                })

        return sequence
