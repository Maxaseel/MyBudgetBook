package com.example.mybudgetbook.ui.screens.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mybudgetbook.data.relation.BudgetMitAusgaben
import com.example.mybudgetbook.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(viewModel: BudgetViewModel, onBack: () -> Unit, onAddBudget: () -> Unit) {
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الميزانية") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudget, containerColor = Color(0xFF43A047)) { Icon(Icons.Default.Add, null, tint = Color.White) }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(budgets) { budget -> BudgetCard(budget) }
        }
    }
}

@Composable
fun BudgetCard(budget: BudgetMitAusgaben) {
    val max = budget.budget.maxSumme
    val spent = budget.ausgegeben
    val remaining = max - spent
    val progress = if (max > 0) (spent / max).toFloat().coerceIn(0f, 1f) else 0f

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(budget.budget.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${remaining.toInt()} SAR متبقية", color = if (remaining >= 0) Color(0xFF43A047) else Color(0xFFE53935), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (progress < 0.8f) Color(0xFF43A047) else Color(0xFFE53935),
                trackColor = Color.DarkGray, strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("صُرف: $spent", color = Color.Gray, fontSize = 12.sp)
                Text("الميزانية: $max", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
