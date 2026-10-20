package com.example.mybudgetbook.ui.screens.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.Kategorie
import com.example.mybudgetbook.data.entity.Regel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(db: AppDatabase, onBack: () -> Unit) {
    val rules by db.regelDao().getAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by db.kategorieDao().getAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var matchText by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(0L) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("القواعد التلقائية") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
            })
        },
        containerColor = Color(0xFF121212),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("أضف قاعدة لتحديد فئة المعاملة تلقائيًا")
                        OutlinedTextField(
                            value = matchText,
                            onValueChange = { matchText = it },
                            label = { Text("النص المطابق") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = categories.firstOrNull { it.id == categoryId }?.name ?: "اختر الفئة",
                                onValueChange = {}, readOnly = true,
                                label = { Text("الفئة") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                categories.forEach { category ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = { categoryId = category.id; expanded = false },
                                    )
                                }
                            }
                        }
                        Button(
                            enabled = matchText.isNotBlank() && categoryId != 0L,
                            onClick = {
                                scope.launch {
                                    db.regelDao().insert(Regel(textToMatch = matchText.trim(), kategorieId = categoryId))
                                    matchText = ""
                                    categoryId = 0L
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("حفظ القاعدة") }
                    }
                }
            }
            if (rules.isEmpty()) item { Text("لا توجد قواعد محفوظة بعد", color = Color.LightGray) }
            items(rules, key = { it.id }) { rule ->
                val categoryName = categories.firstOrNull { it.id == rule.kategorieId }?.name ?: "فئة غير موجودة"
                Card {
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                            Text("إذا احتوى النص على: ${rule.textToMatch}")
                            Text("الفئة: $categoryName", color = Color.LightGray)
                        }
                        IconButton(onClick = { scope.launch { db.regelDao().deleteById(rule.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف القاعدة", tint = Color(0xFFE53935))
                        }
                    }
                }
            }
        }
    }
}
