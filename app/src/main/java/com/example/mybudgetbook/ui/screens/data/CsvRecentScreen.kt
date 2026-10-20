package com.example.mybudgetbook.ui.screens.data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mybudgetbook.data.AppDatabase
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CsvImportRecord(val file: String, val imported: Int, val errors: Int, val timestamp: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvRecentScreen(db: AppDatabase, onBack: () -> Unit) {
    val context = LocalContext.current
    val records = remember(context) {
        val history = try {
            JSONArray(context.getSharedPreferences("mybudgetbook_imports", 0).getString("history", "[]"))
        } catch (_: Exception) { JSONArray() }
        (0 until history.length()).mapNotNull { index ->
            val item = history.optJSONObject(index) ?: return@mapNotNull null
            CsvImportRecord(
                file = item.optString("file", "CSV"),
                imported = item.optInt("imported"),
                errors = item.optInt("errors"),
                timestamp = item.optLong("timestamp"),
            )
        }
    }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar")) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("آخر واردات CSV") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
            })
        },
        containerColor = Color(0xFF121212),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (records.isEmpty()) item { Text("لا توجد واردات سابقة حاليًا", color = Color.Gray) }
            items(records) { record ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(record.file, color = Color.White)
                        Text("${record.imported} سجل مستورد • ${record.errors} أخطاء", color = Color.LightGray)
                        Text(dateFormat.format(Date(record.timestamp)), color = Color.Gray)
                    }
                }
            }
        }
    }
}
