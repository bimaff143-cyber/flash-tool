package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.FlashHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ROM Flash Tool", appName)
  }

  @Test
  fun `room database stores flash operations history with audit fields`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.flashHistoryDao()

    val success = FlashHistoryEntity(
      operationType = "FASTBOOT_ROM",
      targetName = "factory_cheetah.zip",
      deviceModel = "Pixel 7 Pro",
      filePath = "/sdcard/factory_cheetah.zip",
      timestamp = 1728000000000L,
      status = "SUCCESS",
      auditChecksum = "sha256_mock"
    )
    val failed = FlashHistoryEntity(
      operationType = "CUSTOM_IMAGE",
      targetName = "boot.img",
      deviceModel = "Pixel 7 Pro",
      filePath = "/sdcard/boot.img",
      timestamp = 1728000001000L,
      status = "FAILED",
      errorMessage = "Fastboot transfer timeout"
    )

    dao.insertHistory(success)
    dao.insertHistory(failed)

    val list = dao.getAllHistory().first()
    assertEquals(2, list.size)

    val successItem = list.first { it.targetName == "factory_cheetah.zip" }
    assertEquals("SUCCESS", successItem.status)
    assertEquals("Pixel 7 Pro", successItem.deviceModel)
    assertEquals("/sdcard/factory_cheetah.zip", successItem.filePath)

    val failedItem = list.first { it.targetName == "boot.img" }
    assertEquals("FAILED", failedItem.status)
    assertEquals("/sdcard/boot.img", failedItem.filePath)
    assertEquals("Fastboot transfer timeout", failedItem.errorMessage)

    db.close()
  }

  @Test
  fun `theme mode preference stores and loads system, light, and dark settings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    // Store Light
    prefs.edit().putString("theme_mode", com.example.ui.theme.ThemeMode.LIGHT.name).commit()
    assertEquals("LIGHT", prefs.getString("theme_mode", null))

    // Store Dark
    prefs.edit().putString("theme_mode", com.example.ui.theme.ThemeMode.DARK.name).commit()
    assertEquals("DARK", prefs.getString("theme_mode", null))

    // Store System
    prefs.edit().putString("theme_mode", com.example.ui.theme.ThemeMode.SYSTEM.name).commit()
    assertEquals("SYSTEM", prefs.getString("theme_mode", null))
  }

  @Test
  fun `flash preset JSON serialization and deserialization`() {
    val preset = com.example.model.FlashPreset(
      id = "test_custom_preset",
      name = "KernelSU Setup",
      description = "Flashes boot and dtbo with custom kernel",
      category = "Custom",
      items = listOf(
        com.example.model.PresetPartitionItem("boot", "kernelsu_boot.img", false),
        com.example.model.PresetPartitionItem("dtbo", "dtbo.img", false)
      ),
      wipeUserdata = false,
      disableVerity = true,
      isBuiltIn = false
    )

    val json = preset.toJson()
    org.junit.Assert.assertTrue(json.contains("KernelSU Setup"))
    org.junit.Assert.assertTrue(json.contains("kernelsu_boot.img"))
    org.junit.Assert.assertTrue(json.contains("dtbo.img"))

    val deserialized = com.example.model.FlashPreset.fromJson(json)
    assertEquals("test_custom_preset", deserialized.id)
    assertEquals("KernelSU Setup", deserialized.name)
    assertEquals(2, deserialized.items.size)
    org.junit.Assert.assertTrue(deserialized.disableVerity)
    org.junit.Assert.assertFalse(deserialized.wipeUserdata)
  }
}
