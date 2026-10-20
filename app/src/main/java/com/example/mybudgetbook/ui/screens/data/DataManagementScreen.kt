package com.example.mybudgetbook.ui.screens.data

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.BackupRestoreManager
import com.example.mybudgetbook.data.DataExportImportManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    db: AppDatabase,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("جاهز") }

    val exportManager = remember { DataExportImportManager(db, context) }
    val backupManager = remember { BackupRestoreManager(context) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val result = exportManager.exportAllToCsv(uri)
                    status = result.getOrElse { it.message ?: "حدث خطأ أثناء التصدير" }
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val result = exportManager.importCsvFromUri(uri)
                    status = "تم استيراد ${result.importedCount} سجل" +
                        if (result.errors.isNotEmpty()) " مع ${result.errors.size} أخطاء" else ""
                }
            }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    db.close()
                    AppDatabase.resetInstance()
                    val result = backupManager.restoreBackup(uri)
                    status = result.getOrElse { it.message ?: "حدث خطأ أثناء الاستعادة" }
                    (context as? Activity)?.recreate()
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة البيانات") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DataActionItem("إنشاء نسخة احتياطية", Icons.Default.Save) {
                scope.launch {
                    val backupResult = backupManager.createBackup()
                    status = backupResult.fold(
                        onSuccess = { uri -> "تم إنشاء النسخة: ${uri.lastPathSegment ?: "backup"}" },
                        onFailure = { error -> error.message ?: "حدث خطأ أثناء إنشاء النسخة" },
                    )
                }
            }

            DataActionItem("استعادة نسخة احتياطية", Icons.Default.FolderOpen) {
                restoreLauncher.launch("*/*")
            }

            DataActionItem("تصدير البيانات", Icons.Default.Share) {
                exportLauncher.launch(null)
            }

            DataActionItem("استيراد CSV", Icons.Default.Download) {
                importLauncher.launch("text/csv")
            }

            DataActionItem("آخر واردات CSV", Icons.Default.Description) {
                status = "فتح آخر واردات CSV"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1F1F), shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Text(status, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun DataActionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color(0xFF1F1F1F), shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 18.sp)
    }
}
