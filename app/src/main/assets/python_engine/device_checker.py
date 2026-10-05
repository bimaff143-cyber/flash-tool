"""
ROM Flash Tool v1.0 - Device Checker & Compatibility Verification
Verifies bootloader unlock status, target codename matching, slot topology, and battery safety.
"""

from typing import Dict, Any, List, Tuple, Union
import hashlib


class DeviceChecker:
    """Verifies target device against incoming ROM files, images, and safety requirements."""

    MIN_BATTERY_PERCENT: int = 30

    @staticmethod
    def verify_battery_level(battery_value: Union[int, float, str], min_charge_pct: int = 30) -> Tuple[bool, str, int]:
        """
        Ensures device has at least `min_charge_pct` (default 30%) charge before allowing flash operations.
        Accepts integer percentage, string ('85', '45%'), or fastboot variable 'battery-soc-ok'.
        Returns: (is_safe: bool, message: str, detected_level: int)
        """
        pct = 0
        if isinstance(battery_value, (int, float)):
            pct = int(battery_value)
        elif isinstance(battery_value, str):
            clean = battery_value.strip().lower().replace("%", "")
            if clean in ("yes", "true", "ok"):
                pct = 100
            elif clean in ("no", "false", "low"):
                pct = 15
            else:
                try:
                    pct = int(float(clean))
                except ValueError:
                    # check millivolts e.g. 3800mv
                    if clean.endswith("mv"):
                        try:
                            mv = int(clean.replace("mv", ""))
                            pct = max(0, min(100, int((mv - 3400) / (4200 - 3400) * 100)))
                        except ValueError:
                            pct = 0
                    else:
                        pct = 0

        pct = max(0, min(100, pct))

        if pct < min_charge_pct:
            return (
                False,
                f"BATTERY SAFETY ALERT: Device battery is at {pct}%. At least {min_charge_pct}% charge is strictly required to prevent mid-flash power loss and permanent bricking!",
                pct
            )

        return (
            True,
            f"Battery safety check passed: {pct}% charge available (minimum required: {min_charge_pct}%).",
            pct
        )

    @staticmethod
    def verify_bootloader_unlocked(unlocked_val: str) -> Tuple[bool, str]:
        """Checks if bootloader is confirmed unlocked."""
        val = unlocked_val.strip().lower()
        if val in ("yes", "true", "1"):
            return True, "Bootloader is UNLOCKED (Safe to flash)"
        elif val in ("no", "false", "0"):
            return False, "CRITICAL: Bootloader is LOCKED. Flashing custom images will brick or be rejected by signature checks!"
        return False, f"Unknown bootloader unlock state: {unlocked_val}"

    @staticmethod
    def verify_device_compatibility(device_product: str, rom_allowed_products: List[str]) -> Tuple[bool, str]:
        """Validates that ROM is targeted for the connected device codename."""
        if not rom_allowed_products:
            return True, "ROM does not specify product restrictions (Generic/GSI)"

        dev = device_product.strip().lower()
        for allowed in rom_allowed_products:
            a = allowed.strip().lower()
            if dev == a or dev in a or a in dev:
                return True, f"Device match verified: '{device_product}' matches allowed product '{allowed}'"

        return False, f"COMPATIBILITY MISMATCH: Connected device is '{device_product}', but ROM expects one of: {rom_allowed_products}"

    @staticmethod
    def calculate_file_checksums(data_sample: bytes) -> Dict[str, str]:
        """Computes SHA256 and MD5 for verification."""
        sha256 = hashlib.sha256(data_sample).hexdigest()
        md5 = hashlib.md5(data_sample).hexdigest()
        return {
            "sha256": sha256,
            "md5": md5
        }

    @staticmethod
    def check_slot_topology(variables: Dict[str, str]) -> Dict[str, Any]:
        """Checks whether device is A-only, A/B, or Virtual A/B."""
        current_slot = variables.get("current-slot", "").replace("_", "").lower()
        has_slot_boot = variables.get("has-slot:boot", "").lower() in ("yes", "true")
        slot_count = variables.get("slot-count", "")

        is_ab = has_slot_boot or current_slot in ("a", "b") or slot_count == "2"

        return {
            "is_ab": is_ab,
            "current_slot": current_slot if current_slot else "a",
            "other_slot": "b" if current_slot == "a" else ("a" if current_slot == "b" else None)
        }
