package com.example.mybudgetbook.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupRestoreManager(
    private val context: Context
) {

    suspend fun createBackup(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(IllegalStateException("قاعدة البيانات غير موجودة"))
            }

            val backupFile = File(
                context.getExternalFilesDir(null),
                "backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.db"
            )

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(Uri.fromFile(backupFile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IllegalStateException("تعذر فتح الملف"))

            val dbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            val outputStream = FileOutputStream(dbPath)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Result.success("تم استعادة النسخة الاحتياطية بنجاح")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
