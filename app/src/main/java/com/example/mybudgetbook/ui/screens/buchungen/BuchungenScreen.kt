package com.example.mybudgetbook.ui.screens.buchungen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mybudgetbook.data.relation.BuchungMitDetails
import com.example.mybudgetbook.viewmodel.BuchungenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuchungenScreen(
    viewModel: BuchungenViewModel,
    onAdd: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenStats: () -> Unit
) {
    val buchungen by viewModel.buchungen.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val income by viewModel.income.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("المعاملات", color = Color.White) },
                    navigationIcon = { IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, null, tint = Color.White) } },
                    actions = {
                        IconButton(onClick = onOpenStats) { Icon(Icons.Default.BarChart, null, tint = Color.White) }
                        IconButton(onClick = {}) { Icon(Icons.Default.FilterList, null, tint = Color.White) }
                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C)).padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ChevronLeft, null, tint = Color.Gray)
                    Text("أكتوبر", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("2026", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    Icon(Icons.Default.FilterAlt, null, tint = Color.Gray)
                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Color(0xFF43A047)) {
                Icon(Icons.Default.Add, contentDescription = "إضافة", tint = Color.White)
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("الرصيد الحالي", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Text("${balance ?: 0.0} SAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("مجموع الدخل", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Text("${income ?: 0.0} SAR", color = Color(0xFF43A047), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (buchungen.isEmpty()) {
                item { Text("لا توجد حركات بعد", color = Color.Gray, modifier = Modifier.padding(24.dp)) }
            } else {
                items(buchungen, key = { it.id }) { b ->
                    BuchungListItem(b)
                }
            }
        }
    }
}

@Composable
private fun BuchungListItem(b: BuchungMitDetails) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.width(60.dp).background(Color(0xFF2C2C2C), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("18", color = Color.White, fontWeight = FontWeight.Bold)
            Text("أكتوبر", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(b.titel, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${b.kontoName} - ${b.kategorieName}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            b.notiz?.let { Text("📝 $it", color = Color.Gray, style = MaterialTheme.typography.labelSmall) }
        }
        Text(
            text = if (b.betragVz >= 0) "+${b.betragVz}" else "${b.betragVz}",
            color = if (b.betragVz >= 0) Color(0xFF43A047) else Color(0xFFE53935),
            fontWeight = FontWeight.Bold
        )
    }
}
