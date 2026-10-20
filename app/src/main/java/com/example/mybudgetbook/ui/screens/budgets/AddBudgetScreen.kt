package com.example.mybudgetbook.ui.screens.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mybudgetbook.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(viewModel: BudgetViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var maxSumme by remember { mutableStateOf("") }
    var carryRemaining by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إضافة ميزانية") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الميزانية") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            OutlinedTextField(value = maxSumme, onValueChange = { maxSumme = it }, label = { Text("الحد الأقصى للمبلغ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = carryRemaining, onCheckedChange = { carryRemaining = it })
                Text("ترحيل المتبقي للشهر التالي", color = Color.White)
            }
            Button(
                onClick = {
                    val max = maxSumme.toDoubleOrNull()
                    if (name.isNotBlank() && max != null && max > 0) {
                        viewModel.addBudget(name, max, carryRemaining)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) { Text("حفظ الميزانية") }
        }
    }
}
