package com.example.mybudgetbook.ui.screens.savings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import com.example.mybudgetbook.data.entity.Sparziel
import com.example.mybudgetbook.data.relation.KontoMitSaldo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(db: AppDatabase, onBack: () -> Unit) {
    val goals by db.sparzielDao().getAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val balances by db.kontoDao().getAccountsWithBalance().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("أهداف الادخار") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = Color(0xFF43A047)) {
                Icon(Icons.Default.Add, contentDescription = "إضافة هدف", tint = Color.White)
            }
        },
        containerColor = Color(0xFF121212),
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (goals.isEmpty()) item { Text("أضف هدفًا وارتبط بحساب لمتابعة تقدم الادخار", color = Color.LightGray) }
            items(goals, key = { it.id }) { goal ->
                val saved = balances.firstOrNull { it.konto.id == goal.idSparzielKonto }?.saldo?.coerceAtLeast(0.0) ?: 0.0
                val progress = if (goal.betrag > 0.0) (saved / goal.betrag).toFloat().coerceIn(0f, 1f) else 0f
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(goal.name, color = Color.White)
                        Text("${saved.toMoney()} / ${goal.betrag.toMoney()} SAR", color = Color.LightGray)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF43A047),
                            trackColor = Color(0xFF383838),
                        )
                        Text("${(progress * 100).toInt()}% • ${goal.datumVon.toDate()} - ${goal.datumBis.toDate()}", color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddSavingsGoalDialog(
            accounts = balances,
            onDismiss = { showAdd = false },
            onSave = { name, amount, start, end, accountId ->
                scope.launch {
                    db.sparzielDao().insert(
                        Sparziel(name = name, betrag = amount, datumVon = start, datumBis = end, idSparzielKonto = accountId),
                    )
                    showAdd = false
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSavingsGoalDialog(
    accounts: List<KontoMitSaldo>,
    onDismiss: () -> Unit,
    onSave: (String, Double, Long, Long, Long) -> Unit,
) {
    val now = remember { System.currentTimeMillis() }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.konto?.id ?: 0L) }
    var start by remember { mutableStateOf(now) }
    var end by remember { mutableStateOf(now + 30L * 24 * 60 * 60 * 1000) }
    var accountMenu by remember { mutableStateOf(false) }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("هدف ادخار جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("اسم الهدف") }, singleLine = true)
                OutlinedTextField(amount, { amount = it }, label = { Text("المبلغ المستهدف") }, singleLine = true)
                androidx.compose.material3.ExposedDropdownMenuBox(
                    expanded = accountMenu,
                    onExpandedChange = { accountMenu = !accountMenu },
                ) {
                    OutlinedTextField(
                        value = accounts.firstOrNull { it.konto.id == accountId }?.konto?.name ?: "اختر الحساب",
                        onValueChange = {}, readOnly = true,
                        label = { Text("حساب الادخار") },
                        trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(accountMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = accountMenu, onDismissRequest = { accountMenu = false }) {
                        accounts.forEach { account ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(account.konto.name) },
                                onClick = { accountId = account.konto.id; accountMenu = false },
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { pickStart = true }) { Text("البداية: ${start.toDate()}") }
                    TextButton(onClick = { pickEnd = true }) { Text("الموعد: ${end.toDate()}") }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0 && accountId != 0L && end >= start,
                onClick = { onSave(name.trim(), amount.toDouble(), start, end, accountId) },
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } },
    )

    if (pickStart) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = start)
        DatePickerDialog(
            onDismissRequest = { pickStart = false },
            confirmButton = { TextButton(onClick = { picker.selectedDateMillis?.let { start = it }; pickStart = false }) { Text("تم") } },
            dismissButton = { TextButton(onClick = { pickStart = false }) { Text("إلغاء") } },
        ) { DatePicker(state = picker) }
    }
    if (pickEnd) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = end)
        DatePickerDialog(
            onDismissRequest = { pickEnd = false },
            confirmButton = { TextButton(onClick = { picker.selectedDateMillis?.let { end = it }; pickEnd = false }) { Text("تم") } },
            dismissButton = { TextButton(onClick = { pickEnd = false }) { Text("إلغاء") } },
        ) { DatePicker(state = picker) }
    }
}

private fun Long.toDate(): String = SimpleDateFormat("yyyy/MM/dd", Locale("ar")).format(Date(this))
private fun Double.toMoney(): String = String.format(Locale.US, "%.2f", this)
