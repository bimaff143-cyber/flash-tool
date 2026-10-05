package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.entity.FlashHistoryEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AuditExportHelper {

    fun generateCsv(history: List<FlashHistoryEntity>): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

        // CSV Header
        sb.append("id,timestamp_epoch,timestamp_iso,operation_type,target_name,device_model,file_path,status,duration_ms,bytes_processed,audit_checksum,error_message,summary\n")

        for (item in history) {
            val isoDate = dateFormat.format(Date(item.timestamp))
            sb.append(escapeCsv(item.id.toString())).append(",")
            sb.append(escapeCsv(item.timestamp.toString())).append(",")
            sb.append(escapeCsv(isoDate)).append(",")
            sb.append(escapeCsv(item.operationType)).append(",")
            sb.append(escapeCsv(item.targetName)).append(",")
            sb.append(escapeCsv(item.deviceModel)).append(",")
            sb.append(escapeCsv(item.filePath)).append(",")
            sb.append(escapeCsv(item.status)).append(",")
            sb.append(escapeCsv(item.durationMs.toString())).append(",")
            sb.append(escapeCsv(item.bytesProcessed.toString())).append(",")
            sb.append(escapeCsv(item.auditChecksum ?: "")).append(",")
            sb.append(escapeCsv(item.errorMessage ?: "")).append(",")
            sb.append(escapeCsv(item.summary))
            sb.append("\n")
        }

        return sb.toString()
    }

    fun generateFormattedText(history: List<FlashHistoryEntity>): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        val generatedAt = dateFormat.format(Date())

        val successCount = history.count { it.status.equals("SUCCESS", ignoreCase = true) }
        val failCount = history.count { it.status.equals("FAILED", ignoreCase = true) }

        sb.append("================================================================================\n")
        sb.append("                         ROM FLASH TOOL - AUDIT TRAIL LOG                       \n")
        sb.append("================================================================================\n")
        sb.append("Export Timestamp: $generatedAt\n")
        sb.append("Total Records   : ${history.size}\n")
        sb.append("Successful      : $successCount\n")
        sb.append("Failed          : $failCount\n")
        sb.append("Database        : SQLite (Room persistent storage)\n")
        sb.append("================================================================================\n\n")

        if (history.isEmpty()) {
            sb.append("No flash operations logged in the audit database.\n")
            return sb.toString()
        }

        for ((index, item) in history.withIndex()) {
            val dateStr = dateFormat.format(Date(item.timestamp))
            sb.append("--------------------------------------------------------------------------------\n")
            sb.append(String.format(Locale.US, "[RECORD #%03d] %s - %s\n", index + 1, item.operationType, item.status.uppercase()))
            sb.append("--------------------------------------------------------------------------------\n")
            sb.append("  Timestamp      : $dateStr (${item.timestamp} ms)\n")
            sb.append("  Device Model   : ${item.deviceModel}\n")
            sb.append("  Target Name    : ${item.targetName}\n")
            sb.append("  File Path      : ${item.filePath}\n")
            sb.append("  Duration       : ${item.durationMs} ms\n")
            sb.append("  Data Processed : ${item.bytesProcessed / (1024 * 1024)} MB (${item.bytesProcessed} bytes)\n")
            if (!item.auditChecksum.isNullOrBlank()) {
                sb.append("  Audit Checksum : ${item.auditChecksum}\n")
            }
            if (!item.summary.isBlank()) {
                sb.append("  Summary        : ${item.summary}\n")
            }
            if (!item.errorMessage.isNullOrBlank()) {
                sb.append("  Error Details  : ${item.errorMessage}\n")
            }
            sb.append("\n")
        }

        sb.append("================================================================================\n")
        sb.append("                                 END OF AUDIT LOG                               \n")
        sb.append("================================================================================\n")

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var str = value
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = str.replace("\"", "\"\"")
            return "\"$str\""
        }
        return str
    }

    fun saveAuditReportToFile(context: Context, content: String, fileName: String): File {
        val dir = context.getExternalFilesDir("audit_exports") ?: context.cacheDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, fileName)
        FileWriter(file).use { writer ->
            writer.write(content)
        }
        return file
    }

    fun createShareIntent(context: Context, file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ROM Flash Tool Audit Log: ${file.name}")
            putExtra(Intent.EXTRA_TEXT, "Attached SQLite Flash Operation Audit Log (${file.name}).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
