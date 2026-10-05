"""
ROM Flash Tool v1.0 - Partition Restore Manager
Validates backup archive integrity and coordinates partition restoration.
"""

from typing import Dict, Any, List, Optional, Tuple
import hashlib
import json


class RestoreManager:
    """Safely restores backed-up partitions to the target device."""

    DANGEROUS_RESTORE_PARTITIONS = {
        "persist": "Restoring persist overwrites hardware calibrations. Ensure backup originated from this exact physical unit.",
        "efs": "Restoring EFS alters IMEI and radio NVRAM. Extreme caution advised.",
        "vbmeta": "Restoring vbmeta will enforce original AVB flags.",
        "abl": "Restoring bootloader firmware risks hard-brick if incompatible."
    }

    def __init__(self):
        pass

    def validate_manifest(self, manifest_data: Dict[str, Any], current_device_product: str) -> Tuple[bool, str]:
        """Ensures backup corresponds to the connected hardware."""
        backup_product = manifest_data.get("device", {}).get("product", "")
        if backup_product and current_device_product:
            if backup_product.lower() != current_device_product.lower():
                return False, f"DEVICE MISMATCH: Backup was created for '{backup_product}', but connected device is '{current_device_product}'. Restoring may brick device!"

        return True, "Backup manifest verified for this device."

    def check_restore_safety(self, partition_name: str) -> Dict[str, Any]:
        """Identifies if restoring this partition requires explicit warning acknowledgment."""
        part_clean = partition_name.lower().replace("_a", "").replace("_b", "")
        is_dangerous = part_clean in self.DANGEROUS_RESTORE_PARTITIONS
        warning = self.DANGEROUS_RESTORE_PARTITIONS.get(part_clean, None)

        return {
            "partition": partition_name,
            "is_dangerous": is_dangerous,
            "warning": warning
        }

    def plan_restore_execution(self, items: List[Dict[str, Any]], slot: Optional[str] = None) -> List[Dict[str, Any]]:
        """
        Builds list of restore flash steps.
        Each item has {"partition": "boot", "filepath": "..."}
        """
        plan = []
        for it in items:
            part = it["partition"]
            plan.append({
                "action": "FLASH_PARTITION",
                "partition": part,
                "file_path": it["filepath"],
                "target_slot": slot,
                "safety": self.check_restore_safety(part)
            })
        return plan
