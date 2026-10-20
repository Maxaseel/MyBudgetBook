package com.example.mybudgetbook.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.mybudgetbook.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val stats by viewModel.categoryStats.collectAsStateWithLifecycle()
    val totalIncome = stats.filter { it.isIncome }.sumOf { it.total }
    val totalExpense = stats.filter { !it.isIncome }.sumOf { it.total }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Column { Text("الإحصائيات", color = Color.White); Text("الفئات", style = MaterialTheme.typography.bodySmall, color = Color.Gray) } },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Default.Visibility, null, tint = Color.White) }
                        IconButton(onClick = {}) { Icon(Icons.Default.List, null, tint = Color.White) }
                        IconButton(onClick = {}) { Icon(Icons.Default.FilterList, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF121212)).padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    Text("أكتوبر", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("2026", color = Color.White, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.FilterAlt, null, tint = Color.White)
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الدخل", color = Color.White, fontSize = 18.sp)
                            Text("+$totalIncome SAR", color = Color(0xFF43A047), fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = Color.DarkGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المصروفات", color = Color.White, fontSize = 18.sp)
                            Text("-$totalExpense SAR", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("النقل (الفئات)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        stats.forEach { stat ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stat.name, color = Color.White)
                                Text(text = if (stat.total >= 0) "+${stat.total}" else "${stat.total}", color = if (stat.total >= 0) Color(0xFF43A047) else Color(0xFFE53935))
                            }
                            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
