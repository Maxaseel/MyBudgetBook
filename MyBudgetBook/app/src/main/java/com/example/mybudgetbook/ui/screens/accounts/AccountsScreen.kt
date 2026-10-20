package com.example.mybudgetbook.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.mybudgetbook.data.relation.KontoMitSaldo
import com.example.mybudgetbook.viewmodel.AccountsViewModel

enum class SortOrder { NAME_ASC, NAME_DESC, BALANCE_HIGH, BALANCE_LOW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel,
    onBack: () -> Unit,
    onAddAccount: () -> Unit,
    onEditAccount: (Long) -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    var showSortDialog by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(SortOrder.NAME_ASC) }

    val sortedAccounts = remember(accounts, sortOrder) {
        when (sortOrder) {
            SortOrder.NAME_ASC -> accounts.sortedBy { it.konto.name }
            SortOrder.NAME_DESC -> accounts.sortedByDescending { it.konto.name }
            SortOrder.BALANCE_HIGH -> accounts.sortedByDescending { it.saldo }
            SortOrder.BALANCE_LOW -> accounts.sortedBy { it.saldo }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("الحسابات", color = Color.White)
                        Text("الرصيد الحالي (اليوم)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Visibility, null, tint = Color.White) }
                    IconButton(onClick = { showSortDialog = true }) { Icon(Icons.Default.Sort, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAccount, containerColor = Color(0xFF43A047)) { Icon(Icons.Default.Add, null, tint = Color.White) }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(sortedAccounts, key = { it.konto.id }) { acc ->
                AccountRow(acc) { onEditAccount(acc.konto.id) }
                HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 1.dp)
            }
        }
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("فرز حسب", color = Color.White) },
            containerColor = Color(0xFF2C2C2C),
            text = {
                Column {
                    SortOption("الحساب (من الألف إلى الياء)", sortOrder == SortOrder.NAME_ASC) { sortOrder = SortOrder.NAME_ASC; showSortDialog = false }
                    SortOption("الحساب (من الياء إلى الألف)", sortOrder == SortOrder.NAME_DESC) { sortOrder = SortOrder.NAME_DESC; showSortDialog = false }
                    SortOption("الرصيد الحالي (الأكبر)", sortOrder == SortOrder.BALANCE_HIGH) { sortOrder = SortOrder.BALANCE_HIGH; showSortDialog = false }
                    SortOption("الرصيد الحالي (الأصغر)", sortOrder == SortOrder.BALANCE_LOW) { sortOrder = SortOrder.BALANCE_LOW; showSortDialog = false }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun AccountRow(acc: KontoMitSaldo, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(acc.konto.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "${acc.saldo} SAR", color = if (acc.saldo >= 0) Color(0xFF43A047) else Color(0xFFE53935), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SortOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = Color.White))
        Text(text, color = Color.White, modifier = Modifier.padding(start = 8.dp))
    }
}
