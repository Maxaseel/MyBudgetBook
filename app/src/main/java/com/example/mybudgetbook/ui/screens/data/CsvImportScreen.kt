package com.example.mybudgetbook.ui.screens.data

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.DataExportImportManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportScreen(
    db: AppDatabase,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("حدد ملف CSV") }

    val manager = remember { DataExportImportManager(db, context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val result = manager.importCsvFromUri(uri)
                    status = "تم استيراد ${result.importedCount} سجل" +
                        if (result.errors.isNotEmpty()) "، يوجد ${result.errors.size} خطأ" else ""
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("استيراد CSV") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { launcher.launch("text/csv") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("اختيار ملف CSV")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(status, color = Color.White)
        }
    }
}
