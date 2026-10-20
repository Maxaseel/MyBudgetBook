package com.example.mybudgetbook.ui.screens.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mybudgetbook.data.entity.Buchung
import com.example.mybudgetbook.viewmodel.RecurringAndTemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(viewModel: RecurringAndTemplateViewModel, onBack: () -> Unit, onUseTemplate: (Buchung) -> Unit) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("القوالب") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        if (templates.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("لا توجد قوالب محفوظة", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(templates) { b -> TemplateItem(b) { onUseTemplate(b) } }
            }
        }
    }
}

@Composable
fun TemplateItem(b: Buchung, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Assignment, null, tint = Color.Gray)
            Spacer(Modifier.width(16.dp))
            Text(b.text, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text("استخدام", color = Color(0xFF1E88E5))
    }
    HorizontalDivider(color = Color(0xFF2C2C2C))
}
