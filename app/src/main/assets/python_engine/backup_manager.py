"""
ROM Flash Tool v1.0 - Partition Backup Manager
Plans and coordinates partition backups via Fastboot fetch or ADB raw block dumping.
"""

from typing import Dict, Any, List, Optional
import json
import time


class BackupManager:
    """Manages backing up critical and boot partitions before flashing."""

    CRITICAL_BACKUP_PARTITIONS = [
        {"name": "boot", "desc": "Boot Kernel & Core Ramdisk", "critical": True},
        {"name": "init_boot", "desc": "GKI 2.0 Generic Ramdisk", "critical": True},
        {"name": "vendor_boot", "desc": "Vendor Ramdisk & Hardware Drivers", "critical": True},
        {"name": "recovery", "desc": "Recovery Environment", "critical": False},
        {"name": "dtbo", "desc": "Device Tree Blob Overlay", "critical": True},
        {"name": "vbmeta", "desc": "Verified Boot Signatures & Metadata", "critical": True},
        {"name": "vbmeta_system", "desc": "System AVB Verification", "critical": False},
        {"name": "persist", "desc": "Sensors, Calibration & Wi-Fi MAC", "critical": True},
        {"name": "efs", "desc": "IMEI & Modem Baseband Calibration", "critical": True},
    ]

    def __init__(self):
        self.active_backup_tasks = []

    def get_supported_backup_partitions(self) -> List[Dict[str, Any]]:
        return list(self.CRITICAL_BACKUP_PARTITIONS)

    def generate_backup_manifest(
        self,
        device_product: str,
        device_serial: str,
        selected_partitions: List[str]
    ) -> Dict[str, Any]:
        """Creates metadata manifest accompanying the backup files."""
        return {
            "tool": "ROM Flash Tool v1.0",
            "timestamp": int(time.time()),
            "date_utc": time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()),
            "device": {
                "product": device_product,
                "serial": device_serial
            },
            "partitions": selected_partitions,
            "manifest_version": 1
        }

    def plan_backup_execution(self, partitions: List[str], method: str = "fastboot") -> List[Dict[str, Any]]:
        """
        Creates backup steps.
        If fastboot: uses `fastboot fetch <part> <dest>` or raw partition reading.
        If adb: uses `adb shell dd if=/dev/block/by-name/<part> of=...` or pulling image.
        """
        steps = []
        for part in partitions:
            steps.append({
                "partition": part,
                "method": method,
                "output_filename": f"{part}_backup.img",
                "status": "QUEUED"
            })
        return steps
