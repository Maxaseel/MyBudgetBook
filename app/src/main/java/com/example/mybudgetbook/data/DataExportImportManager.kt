package com.example.mybudgetbook.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class CsvImportResult(
    val importedCount: Int,
    val errors: List<String>
)

class DataExportImportManager(
    private val db: AppDatabase,
    private val context: Context
) {

    suspend fun exportAllToCsv(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = DocumentFile.fromTreeUri(context, uri)
                ?: return@withContext Result.failure(IllegalStateException("تعذر الوصول إلى المسار المحدد"))

            val fileName = "export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
            val target = root.createFile("text/csv", fileName)
                ?: return@withContext Result.failure(IllegalStateException("تعذر إنشاء ملف CSV"))

            context.contentResolver.openOutputStream(target.uri)?.use { output ->
                output.bufferedWriter().use { writer ->
                    writer.write(buildCsvHeader())
                    writer.newLine()

                    db.buchungDao().getAllRaw().forEach { item ->
                        writer.write(
                            listOf(
                                item.id.toString(),
                                item.text,
                                item.beschreibung ?: "",
                                item.notiz ?: "",
                                item.waehrung,
                                item.referenzNummer ?: "",
                                item.betrag.toString(),
                                item.betragvz.toString(),
                                item.buchungsdatum.toString(),
                                item.buchungsdatumSt,
                                item.idBuchungKategorie.toString(),
                                item.idBuchungKonto.toString(),
                                item.idBuchungPerson.toString(),
                                item.idBuchungGruppe.toString(),
                                item.zahlungsart.toString(),
                                item.vorlage.toString(),
                                item.art.toString()
                            ).joinToString(",") { escapeCsv(it) }
                        )
                        writer.newLine()
                    }
                }
            }

            Result.success("تم تصدير البيانات بنجاح")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importCsvFromUri(uri: Uri): CsvImportResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        var importedCount = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val rows = input.bufferedReader().readLines()
                if (rows.isEmpty()) {
                    return@withContext CsvImportResult(0, listOf("الملف فارغ"))
                }

                val header = rows.firstOrNull() ?: return@withContext CsvImportResult(0, listOf("الملف فارغ"))
                if (!header.contains("text", ignoreCase = true) || !header.contains("betrag", ignoreCase = true)) {
                    return@withContext CsvImportResult(0, listOf("صيغة ملف CSV غير صحيحة"))
                }

                rows.drop(1).forEach { line ->
                    if (line.isBlank()) return@forEach

                    val values = parseCsvLine(line)
                    if (values.size < 17) {
                        errors.add("سطر غير صحيح: $line")
                        return@forEach
                    }

                    try {
                        val text = values[1].ifBlank { "غير مسمى" }
                        val betrag = values[6].toDoubleOrNull() ?: 0.0
                        val betragVz = values[7].toDoubleOrNull() ?: 0.0
                        val date = values[8].toLongOrNull() ?: System.currentTimeMillis()

                        val item = com.example.mybudgetbook.data.entity.Buchung(
                            text = text,
                            beschreibung = values[2].ifBlank { null },
                            notiz = values[3].ifBlank { null },
                            waehrung = values[4].ifBlank { "SAR" },
                            referenzNummer = values[5].ifBlank { null },
                            betrag = betrag,
                            betragvz = betragVz,
                            buchungsdatum = date,
                            buchungsdatumSt = values[9].ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(date)) },
                            idBuchungKategorie = values[10].toLongOrNull() ?: 1L,
                            idBuchungKonto = values[11].toLongOrNull() ?: 1L,
                            idBuchungPerson = values[12].toLongOrNull() ?: 1L,
                            idBuchungGruppe = values[13].toLongOrNull() ?: 1L,
                            zahlungsart = values[14].toIntOrNull() ?: 1,
                            vorlage = values[15].toIntOrNull() ?: 0,
                            art = values[16].toIntOrNull() ?: 0
                        )

                        db.buchungDao().insert(item)
                        importedCount++
                    } catch (e: Exception) {
                        errors.add("خطأ في استيراد السطر: ${e.message ?: "unknown"}")
                    }
                }
            } ?: run {
                return@withContext CsvImportResult(0, listOf("تعذر فتح الملف"))
            }

            val result = CsvImportResult(importedCount, errors)
            saveImportHistory(uri, result)
            result
        } catch (e: Exception) {
            CsvImportResult(0, listOf(e.message ?: "تعذر استيراد CSV"))
        }
    }

    private fun buildCsvHeader(): String {
        return listOf(
            "id", "text", "beschreibung", "notiz", "waehrung", "referenzNummer",
            "betrag", "betragvz", "buchungsdatum", "buchungsdatum_st",
            "id_buchung_kategorie", "id_buchung_konto", "id_buchung_person",
            "id_buchung_gruppe", "zahlungsart", "vorlage", "art"
        ).joinToString(",")
    }

    private fun saveImportHistory(uri: Uri, result: CsvImportResult) {
        val preferences = context.getSharedPreferences("mybudgetbook_imports", Context.MODE_PRIVATE)
        val history = try { JSONArray(preferences.getString("history", "[]")) } catch (_: Exception) { JSONArray() }
        val updated = JSONArray().put(
            JSONObject()
                .put("file", uri.lastPathSegment ?: "CSV")
                .put("imported", result.importedCount)
                .put("errors", result.errors.size)
                .put("timestamp", System.currentTimeMillis()),
        )
        for (index in 0 until minOf(history.length(), 19)) updated.put(history.getJSONObject(index))
        preferences.edit().putString("history", updated.toString()).apply()
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            "\"$escaped\""
        } else escaped
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }

        result.add(current.toString())
        return result
    }
}
