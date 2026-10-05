package com.example.model

import java.util.UUID

data class ManualCommandLog(
    val id: String = UUID.randomUUID().toString(),
    val command: String,
    val protocol: String, // "FASTBOOT" or "ADB"
    val output: String,
    val isSuccess: Boolean,
    val executionTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
