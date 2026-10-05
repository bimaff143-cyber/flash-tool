"""
ROM Flash Tool v1.0 - Partition Image Analyzer & Validator
Inspects boot.img, init_boot.img, vendor_boot.img, vbmeta.img, dtbo.img, and partition headers.
Calculates and verifies MD5/SHA256 checksums of ROM images against manifest files before flashing.
"""

from typing import Dict, Any, Optional, Tuple, List, Union
import struct
import hashlib
import json
import os


class ImageType:
    BOOT_V0_2 = "BOOT_IMG_V0_V2"
    BOOT_V3_4 = "BOOT_IMG_V3_V4"
    VENDOR_BOOT = "VENDOR_BOOT"
    INIT_BOOT = "INIT_BOOT"
    VBMETA_AVB2 = "VBMETA_AVB2"
    DTBO = "DTBO"
    SPARSE_IMG = "SPARSE_IMAGE"
    RAW_GENERIC = "RAW_GENERIC"


class BootImageHeader:
    """Parses Android boot.img headers (v0 through v4)."""
    BOOT_MAGIC = b"ANDROID!"
    VENDOR_BOOT_MAGIC = b"VNDRBOOT"

    def __init__(self, data: bytes):
        if len(data) < 64:
            raise ValueError("Data too short for boot image header")

        self.magic = data[:8]
        self.is_boot = (self.magic == self.BOOT_MAGIC)
        self.is_vendor_boot = (self.magic == self.VENDOR_BOOT_MAGIC)

        self.header_version = 0
        self.kernel_size = 0
        self.ramdisk_size = 0
        self.os_version = "Unknown"
        self.cmdline = ""

        if self.is_boot:
            self._parse_boot(data)
        elif self.is_vendor_boot:
            self._parse_vendor_boot(data)

    def _parse_boot(self, data: bytes):
        if len(data) >= 44:
            v_cand = struct.unpack("<I", data[40:44])[0]
            if v_cand in (0, 1, 2):
                self.header_version = v_cand
                self.kernel_size = struct.unpack("<I", data[8:12])[0]
                self.ramdisk_size = struct.unpack("<I", data[16:20])[0]
                return

        # Check v3/v4
        if len(data) >= 24:
            v_cand = struct.unpack("<I", data[20:24])[0]
            if v_cand in (3, 4):
                self.header_version = v_cand
                self.kernel_size = struct.unpack("<I", data[8:12])[0]
                self.ramdisk_size = struct.unpack("<I", data[12:16])[0]
                raw_os = struct.unpack("<I", data[16:20])[0]
                os_ver = (raw_os >> 11) & 0x7FF
                os_patch = raw_os & 0x7FF
                self.os_version = f"API {os_ver} (patch {os_patch})"

    def _parse_vendor_boot(self, data: bytes):
        if len(data) >= 20:
            self.header_version = struct.unpack("<I", data[8:12])[0]
            self.ramdisk_size = struct.unpack("<I", data[16:20])[0]


class VbmetaHeader:
    """Parses Android Verified Boot 2.0 (AVB) vbmeta image."""
    AVB_MAGIC = b"AVB0"

    def __init__(self, data: bytes):
        if len(data) < 64:
            raise ValueError("Data too short for vbmeta")
        self.magic = data[:4]
        self.is_avb = (self.magic == self.AVB_MAGIC)
        self.flags = 0
        self.verification_disabled = False
        self.verity_disabled = False

        if self.is_avb and len(data) >= 128:
            self.flags = struct.unpack(">I", data[120:124])[0]
            self.verity_disabled = bool(self.flags & 1)
            self.verification_disabled = bool(self.flags & 2)


class ImageManager:
    """
    Analyzes, validates partition image files, and verifies checksums against
    manifest files before flashing begins.
    """

    @staticmethod
    def calculate_checksums(data: bytes) -> Dict[str, str]:
        """Calculates both MD5 and SHA-256 hexadecimal digests for binary data."""
        md5_digest = hashlib.md5(data).hexdigest()
        sha256_digest = hashlib.sha256(data).hexdigest()
        return {
            "md5": md5_digest,
            "sha256": sha256_digest
        }

    @staticmethod
    def parse_checksum_manifest(manifest_content: str) -> Dict[str, Dict[str, str]]:
        """
        Parses checksums from a manifest string.
        Supports standard sha256sum / md5sum output lines, JSON manifest formats,
        and simple key-value entries.
        Returns a dictionary mapping filename (lowercased) -> {"sha256": ..., "md5": ...}
        """
        results: Dict[str, Dict[str, str]] = {}
        content_stripped = manifest_content.strip()

        # 1. Try JSON parsing
        if (content_stripped.startswith("{") and content_stripped.endswith("}")) or \
           (content_stripped.startswith("[") and content_stripped.endswith("]")):
            try:
                parsed_json = json.loads(content_stripped)
                if isinstance(parsed_json, dict):
                    for key, val in parsed_json.items():
                        filename = os.path.basename(key).lower()
                        if isinstance(val, dict):
                            results[filename] = {
                                "sha256": val.get("sha256", "").lower(),
                                "md5": val.get("md5", "").lower()
                            }
                        elif isinstance(val, str):
                            v_clean = val.strip().lower()
                            results[filename] = {
                                "sha256": v_clean if len(v_clean) == 64 else "",
                                "md5": v_clean if len(v_clean) == 32 else ""
                            }
                    return results
                elif isinstance(parsed_json, list):
                    for item in parsed_json:
                        if isinstance(item, dict):
                            fname = os.path.basename(item.get("filename", item.get("file", item.get("name", "")))).lower()
                            if fname:
                                results[fname] = {
                                    "sha256": item.get("sha256", "").lower(),
                                    "md5": item.get("md5", "").lower()
                                }
                    return results
            except Exception:
                pass

        # 2. Line by line parsing (standard sha256sum / md5sum format or key=value)
        for line in manifest_content.splitlines():
            line = line.strip()
            if not line or line.startswith("#") or line.startswith("//"):
                continue

            # Standard format: "<hash>  <filename>" or "<hash> *<filename>" or "<hash> <filename>"
            parts = line.split(None, 1)
            if len(parts) == 2:
                candidate_hash = parts[0].strip().lower()
                target_file = os.path.basename(parts[1].strip().lstrip("*")).lower()

                if len(candidate_hash) == 64:  # SHA-256
                    if target_file not in results:
                        results[target_file] = {"sha256": "", "md5": ""}
                    results[target_file]["sha256"] = candidate_hash
                elif len(candidate_hash) == 32:  # MD5
                    if target_file not in results:
                        results[target_file] = {"sha256": "", "md5": ""}
                    results[target_file]["md5"] = candidate_hash

            # Alternative: "filename: sha256=... md5=..." or "filename = hash"
            elif "=" in line:
                left, right = line.split("=", 1)
                left_clean = os.path.basename(left.strip().split()[-1]).lower()
                right_hash = right.strip().lower()
                if len(right_hash) == 64:
                    if left_clean not in results:
                        results[left_clean] = {"sha256": "", "md5": ""}
                    results[left_clean]["sha256"] = right_hash
                elif len(right_hash) == 32:
                    if left_clean not in results:
                        results[left_clean] = {"sha256": "", "md5": ""}
                    results[left_clean]["md5"] = right_hash

        return results

    @classmethod
    def verify_image_checksum(
        cls,
        image_name: str,
        image_data: bytes,
        manifest: Union[str, Dict[str, Dict[str, str]]]
    ) -> Tuple[bool, str, Dict[str, str]]:
        """
        Calculates and verifies MD5/SHA256 checksums of an image against the manifest.
        Returns:
            (is_valid: bool, status_message: str, details_dict: Dict[str, str])
        """
        calculated = cls.calculate_checksums(image_data)
        base_name = os.path.basename(image_name).lower()

        manifest_map = cls.parse_checksum_manifest(manifest) if isinstance(manifest, str) else manifest

        # Look up expected hashes
        expected_entry = manifest_map.get(base_name)
        if not expected_entry:
            # Also try matching without file extension or with partition name
            for k, v in manifest_map.items():
                if k == base_name or k.startswith(base_name) or base_name.startswith(k):
                    expected_entry = v
                    break

        if not expected_entry:
            return (
                False,
                f"MANIFEST MISSING: Image '{image_name}' was not found in the provided checksum manifest.",
                {
                    "calculated_sha256": calculated["sha256"],
                    "calculated_md5": calculated["md5"],
                    "expected_sha256": "N/A",
                    "expected_md5": "N/A"
                }
            )

        exp_sha256 = expected_entry.get("sha256", "").strip().lower()
        exp_md5 = expected_entry.get("md5", "").strip().lower()

        # Check SHA-256 first (stronger cryptographic guarantee)
        if exp_sha256:
            if calculated["sha256"].lower() == exp_sha256:
                return (
                    True,
                    f"CHECKSUM VERIFIED: SHA-256 matches manifest ({calculated['sha256'][:16]}...).",
                    {
                        "calculated_sha256": calculated["sha256"],
                        "calculated_md5": calculated["md5"],
                        "expected_sha256": exp_sha256,
                        "algorithm": "SHA-256"
                    }
                )
            else:
                return (
                    False,
                    f"INTEGRITY ERROR: SHA-256 mismatch for {image_name}! Expected: {exp_sha256[:16]}..., Calculated: {calculated['sha256'][:16]}...",
                    {
                        "calculated_sha256": calculated["sha256"],
                        "calculated_md5": calculated["md5"],
                        "expected_sha256": exp_sha256,
                        "algorithm": "SHA-256"
                    }
                )

        # Check MD5
        if exp_md5:
            if calculated["md5"].lower() == exp_md5:
                return (
                    True,
                    f"CHECKSUM VERIFIED: MD5 matches manifest ({calculated['md5'][:16]}...).",
                    {
                        "calculated_sha256": calculated["sha256"],
                        "calculated_md5": calculated["md5"],
                        "expected_md5": exp_md5,
                        "algorithm": "MD5"
                    }
                )
            else:
                return (
                    False,
                    f"INTEGRITY ERROR: MD5 mismatch for {image_name}! Expected: {exp_md5}, Calculated: {calculated['md5']}",
                    {
                        "calculated_sha256": calculated["sha256"],
                        "calculated_md5": calculated["md5"],
                        "expected_md5": exp_md5,
                        "algorithm": "MD5"
                    }
                )

        return (
            False,
            f"MANIFEST INCOMPLETE: No valid MD5 or SHA-256 hash specified for '{image_name}'.",
            {
                "calculated_sha256": calculated["sha256"],
                "calculated_md5": calculated["md5"]
            }
        )

    @classmethod
    def verify_rom_images_manifest(
        cls,
        images_dict: Dict[str, bytes],
        manifest_content: str
    ) -> Dict[str, Any]:
        """
        Validates all ROM images in a package against the provided manifest file before flashing.
        Returns aggregate status and per-partition results.
        """
        manifest_map = cls.parse_checksum_manifest(manifest_content)
        results = {}
        all_passed = True

        for img_name, img_data in images_dict.items():
            is_valid, msg, details = cls.verify_image_checksum(img_name, img_data, manifest_map)
            results[img_name] = {
                "valid": is_valid,
                "message": msg,
                "details": details
            }
            if not is_valid:
                all_passed = False

        return {
            "all_valid": all_passed,
            "images_verified": len(results),
            "results": results,
            "summary": "All images passed manifest verification." if all_passed else "One or more images failed checksum validation!"
        }

    @staticmethod
    def inspect_image_header(file_header: bytes, filename: str) -> Dict[str, Any]:
        result = {
            "type": ImageType.RAW_GENERIC,
            "filename": filename,
            "detected_partition": "unknown",
            "is_dangerous": False,
            "danger_reason": None,
            "details": {}
        }

        # Check for AVB vbmeta
        if file_header.startswith(b"AVB0"):
            vb = VbmetaHeader(file_header)
            result["type"] = ImageType.VBMETA_AVB2
            result["detected_partition"] = "vbmeta"
            result["is_dangerous"] = True
            result["danger_reason"] = "AVB Verified Boot structure: Flashing corrupt vbmeta may cause bootloop."
            result["details"] = {
                "verity_disabled": vb.verity_disabled,
                "verification_disabled": vb.verification_disabled,
                "flags": hex(vb.flags)
            }
            return result

        # Check for boot or vendor_boot
        if file_header.startswith(b"ANDROID!"):
            boot = BootImageHeader(file_header)
            result["type"] = ImageType.BOOT_V3_4 if boot.header_version >= 3 else ImageType.BOOT_V0_2
            lower_name = filename.lower()
            if "init_boot" in lower_name:
                result["detected_partition"] = "init_boot"
            elif "recovery" in lower_name:
                result["detected_partition"] = "recovery"
            else:
                result["detected_partition"] = "boot"
            result["details"] = {
                "header_version": boot.header_version,
                "kernel_size": boot.kernel_size,
                "ramdisk_size": boot.ramdisk_size,
                "os_version": boot.os_version
            }
            return result

        if file_header.startswith(b"VNDRBOOT"):
            boot = BootImageHeader(file_header)
            result["type"] = ImageType.VENDOR_BOOT
            result["detected_partition"] = "vendor_boot"
            result["details"] = {
                "header_version": boot.header_version,
                "ramdisk_size": boot.ramdisk_size
            }
            return result

        # Check DTBO magic (0xD7B7AB1E in big endian, or 0x1EABB7D7 little)
        if len(file_header) >= 4 and file_header[:4] in (b"\xd7\xb7\xab\x1e", b"\x1e\xab\xb7\xd7"):
            result["type"] = ImageType.DTBO
            result["detected_partition"] = "dtbo"
            result["details"] = {"dtbo_table": True}
            return result

        # Check Android Sparse Image
        if len(file_header) >= 4 and file_header[:4] == b"\x3a\xff\x26\xed":
            result["type"] = ImageType.SPARSE_IMG
            result["details"] = {"sparse": True}

        # Fallback to name heuristic
        lower = filename.lower()
        for p in ["init_boot", "vendor_boot", "boot", "recovery", "dtbo", "vbmeta", "super", "system", "vendor"]:
            if p in lower:
                result["detected_partition"] = p
                break

        if result["detected_partition"] in ("vbmeta", "xbl", "abl", "modem", "radio"):
            result["is_dangerous"] = True
            result["danger_reason"] = "Modifying low-level firmware or AVB flags can trigger security lockouts."

        return result
