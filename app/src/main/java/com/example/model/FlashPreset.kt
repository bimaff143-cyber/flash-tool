package com.example.model

import org.json.JSONArray
import org.json.JSONObject

data class PresetPartitionItem(
    val partition: String,
    val sampleImageName: String,
    val isCritical: Boolean = false
)

data class FlashPreset(
    val id: String,
    val name: String,
    val description: String,
    val category: String, // "Recovery", "Root & AVB", "Custom Kernel", "Custom"
    val items: List<PresetPartitionItem>,
    val wipeUserdata: Boolean = false,
    val disableVerity: Boolean = false,
    val isBuiltIn: Boolean = true
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("id", id)
        root.put("name", name)
        root.put("description", description)
        root.put("category", category)
        root.put("wipeUserdata", wipeUserdata)
        root.put("disableVerity", disableVerity)
        root.put("isBuiltIn", isBuiltIn)

        val arr = JSONArray()
        for (item in items) {
            val itemObj = JSONObject()
            itemObj.put("partition", item.partition)
            itemObj.put("sampleImageName", item.sampleImageName)
            itemObj.put("isCritical", item.isCritical)
            arr.put(itemObj)
        }
        root.put("items", arr)
        return root.toString(2)
    }

    companion object {
        fun fromJson(jsonStr: String): FlashPreset {
            val root = JSONObject(jsonStr)
            val itemsArr = root.getJSONArray("items")
            val itemsList = mutableListOf<PresetPartitionItem>()
            for (i in 0 until itemsArr.length()) {
                val obj = itemsArr.getJSONObject(i)
                itemsList.add(
                    PresetPartitionItem(
                        partition = obj.getString("partition"),
                        sampleImageName = obj.optString("sampleImageName", "${obj.getString("partition")}.img"),
                        isCritical = obj.optBoolean("isCritical", false)
                    )
                )
            }
            return FlashPreset(
                id = root.getString("id"),
                name = root.getString("name"),
                description = root.optString("description", ""),
                category = root.optString("category", "Custom"),
                items = itemsList,
                wipeUserdata = root.optBoolean("wipeUserdata", false),
                disableVerity = root.optBoolean("disableVerity", false),
                isBuiltIn = root.optBoolean("isBuiltIn", false)
            )
        }

        val BUILT_IN_PRESETS = listOf(
            FlashPreset(
                id = "magisk_root_avb",
                name = "Magisk Root & AVB Bypass",
                description = "Flashes patched boot image and disables AVB 2.0 verification flags.",
                category = "Root & AVB",
                items = listOf(
                    PresetPartitionItem("boot", "magisk_patched_boot.img", false),
                    PresetPartitionItem("vbmeta", "vbmeta_disabled.img", true)
                ),
                disableVerity = true,
                wipeUserdata = false,
                isBuiltIn = true
            ),
            FlashPreset(
                id = "orangefox_recovery_kit",
                name = "OrangeFox / TWRP Recovery Kit",
                description = "Flashes custom recovery and kernel boot staging for custom ROM installation.",
                category = "Recovery",
                items = listOf(
                    PresetPartitionItem("recovery", "orangefox_recovery.img", false),
                    PresetPartitionItem("boot", "boot.img", false),
                    PresetPartitionItem("vendor_boot", "vendor_boot.img", false)
                ),
                disableVerity = false,
                wipeUserdata = false,
                isBuiltIn = true
            ),
            FlashPreset(
                id = "custom_kernel_dtbo",
                name = "Custom Kernel & DTBO Kit",
                description = "KernelSU compatible kernel tree with device tree blob overlay.",
                category = "Custom Kernel",
                items = listOf(
                    PresetPartitionItem("boot", "kernelsu_boot.img", false),
                    PresetPartitionItem("dtbo", "custom_dtbo.img", false)
                ),
                disableVerity = false,
                wipeUserdata = false,
                isBuiltIn = true
            ),
            FlashPreset(
                id = "stock_avb_lock",
                name = "Stock Bootloader Restorer",
                description = "Restores clean factory boot, init_boot and signed Google vbmeta.",
                category = "Recovery",
                items = listOf(
                    PresetPartitionItem("boot", "stock_boot.img", false),
                    PresetPartitionItem("init_boot", "stock_init_boot.img", false),
                    PresetPartitionItem("vbmeta", "stock_vbmeta.img", true)
                ),
                disableVerity = false,
                wipeUserdata = false,
                isBuiltIn = true
            )
        )
    }
}
