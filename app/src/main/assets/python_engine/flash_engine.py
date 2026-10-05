"""
ROM Flash Tool v1.0 - Flash Engine Orchestrator
Coordinates multi-stage flash tasks, progress tracking, automatic reboot, and live logs.
"""

from typing import Dict, Any, List, Optional, Callable
import time


class FlashTaskStatus:
    PENDING = "PENDING"
    RUNNING = "RUNNING"
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"
    ABORTED = "ABORTED"


class FlashEngine:
    """
    State machine and step orchestrator for flashing operations.
    Coordinates between Python logic and Android USB bulk pipeline.
    """

    def __init__(self, log_callback: Optional[Callable[[str, str], None]] = None):
        self.log_callback = log_callback
        self.current_step = 0
        self.total_steps = 0
        self.is_flashing = False
        self.auto_reboot_target = "system"  # system, recovery, bootloader, none

    def log(self, level: str, message: str):
        if self.log_callback:
            self.log_callback(level, message)

    def plan_fastboot_flash(
        self,
        partition_plan: List[Dict[str, Any]],
        wipe_userdata: bool = False,
        disable_verity: bool = False,
        auto_reboot: bool = True
    ) -> List[Dict[str, Any]]:
        """
        Creates an ordered execution script of commands for the USB layer.
        """
        steps = []

        # 1. Device check
        steps.append({
            "action": "GETVAR",
            "var": "max-download-size",
            "desc": "Querying maximum transfer chunk size"
        })
        steps.append({
            "action": "GETVAR",
            "var": "unlocked",
            "desc": "Verifying bootloader security status"
        })

        # 2. Flash partitions
        for item in partition_plan:
            part = item["partition"]
            path = item.get("file_path", "")

            # If vbmeta and disable_verity requested
            if part == "vbmeta" and disable_verity:
                steps.append({
                    "action": "FLASH_VBMETA_PATCHED",
                    "partition": part,
                    "file_path": path,
                    "desc": f"Flashing {part} with disabled AVB verification flags"
                })
            else:
                steps.append({
                    "action": "FLASH_PARTITION",
                    "partition": part,
                    "file_path": path,
                    "desc": f"Flashing partition [{part}]"
                })

        # 3. Userdata wipe if selected
        if wipe_userdata:
            steps.append({
                "action": "ERASE",
                "partition": "userdata",
                "desc": "Erasing userdata partition (Factory Reset)"
            })
            steps.append({
                "action": "ERASE",
                "partition": "metadata",
                "desc": "Erasing metadata partition"
            })

        # 4. Auto Reboot
        if auto_reboot:
            steps.append({
                "action": "REBOOT",
                "target": self.auto_reboot_target,
                "desc": f"Automatic reboot to {self.auto_reboot_target}"
            })

        return steps

    def calculate_transfer_stats(self, bytes_sent: int, start_time: float) -> Dict[str, Any]:
        """Calculates throughput (MB/s) and estimated time remaining."""
        elapsed = max(0.001, time.time() - start_time)
        speed_mb_s = (bytes_sent / (1024 * 1024)) / elapsed
        return {
            "bytes_sent": bytes_sent,
            "elapsed_seconds": elapsed,
            "speed_mb_s": round(speed_mb_s, 2)
        }
