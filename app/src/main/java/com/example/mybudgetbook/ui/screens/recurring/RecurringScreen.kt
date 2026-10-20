package com.example.mybudgetbook.ui.screens.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mybudgetbook.data.entity.Buchung
import com.example.mybudgetbook.viewmodel.RecurringAndTemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(viewModel: RecurringAndTemplateViewModel, onBack: () -> Unit) {
    val recurring by viewModel.recurring.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المعاملات المتكررة") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        if (recurring.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("لا توجد معاملات متكررة", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(recurring) { b -> RecurringItem(b) }
            }
        }
    }
}

@Composable
fun RecurringItem(b: Buchung) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Loop, null, tint = Color.Gray)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(b.text, color = Color.White, fontWeight = FontWeight.Bold)
                Text("كل ${b.periode ?: 1} شهر", color = Color.Gray, fontSize = 12.sp)
            }
        }
        Text(text = "${b.betragvz} SAR", color = if (b.betragvz >= 0) Color(0xFF43A047) else Color(0xFFE53935), fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = Color(0xFF2C2C2C))
}
