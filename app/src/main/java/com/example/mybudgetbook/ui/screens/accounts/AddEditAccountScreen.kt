package com.example.mybudgetbook.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.Konto
import com.example.mybudgetbook.viewmodel.AddEditAccountViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(db: AppDatabase, accountId: Long, onBack: () -> Unit) {
    val viewModel: AddEditAccountViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AddEditAccountViewModel(db, accountId) as T
        }
    )
    var name by remember { mutableStateOf("") }
    var balanceStr by remember { mutableStateOf("0") }
    var comment by remember { mutableStateOf("") }
    var loadedKonto by remember { mutableStateOf<Konto?>(null) }

    LaunchedEffect(accountId) {
        if (accountId != 0L) {
            val k = viewModel.loadKonto(accountId)
            if (k != null) {
                loadedKonto = k
                name = k.name
                balanceStr = k.startsaldo?.toString() ?: "0"
                comment = k.kommentar ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (accountId == 0L) "إضافة حساب" else "تعديل حساب") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (accountId != 0L && loadedKonto != null) {
                        IconButton(onClick = { viewModel.deleteAccount(loadedKonto!!) { onBack() } }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الحساب") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            OutlinedTextField(value = balanceStr, onValueChange = { balanceStr = it }, label = { Text("الرصيد الافتتاحي") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val konto = loadedKonto?.copy(name = name, startsaldo = balanceStr.toDoubleOrNull() ?: 0.0, kommentar = comment.ifBlank { null }) 
                            ?: Konto(name = name, startsaldo = balanceStr.toDoubleOrNull() ?: 0.0, kommentar = comment.ifBlank { null })
                        viewModel.saveAccount(konto) { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) { Text("حفظ") }
        }
    }
}
