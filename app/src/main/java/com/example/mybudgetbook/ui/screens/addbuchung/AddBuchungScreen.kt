package com.example.mybudgetbook.ui.screens.addbuchung

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.mybudgetbook.ui.components.CustomKeypad
import com.example.mybudgetbook.viewmodel.AddBuchungViewModel
import com.example.mybudgetbook.viewmodel.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBuchungScreen(
    viewModel: AddBuchungViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    val konten by viewModel.konten.collectAsStateWithLifecycle()
    val kategorien by viewModel.kategorien.collectAsStateWithLifecycle()
    val zahlungsarten by viewModel.zahlungsarten.collectAsStateWithLifecycle()

    var showKeypad by remember { mutableStateOf(false) }

    LaunchedEffect(konten, kategorien, zahlungsarten) {
        if (s.selectedKontoId == 0L && konten.isNotEmpty()) viewModel.onKontoChange(konten.first().id)
        if (s.selectedKategorieId == 0L && kategorien.isNotEmpty()) viewModel.onKategorieChange(kategorien.first().id)
        if (s.selectedZahlungsartId == 0L && zahlungsarten.isNotEmpty()) viewModel.onZahlungsartChange(zahlungsarten.first().id)
    }

    LaunchedEffect(s.saved) { if (s.saved) onSaved() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.type == TransactionType.TRANSFER) "إضافة تحويل" else "إضافة ${if(s.type == TransactionType.EXPENSE) "مصروف" else "دخل"}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.List, contentDescription = "قوالب") }
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "إغلاق") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
            )
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { }, modifier = Modifier.weight(1f).height(50.dp)) { Text("حفظ وجديد") }
                Button(onClick = viewModel::save, modifier = Modifier.weight(1f).height(50.dp), enabled = !s.isSaving, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))) { Text("حفظ") }
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                TransactionTab("مصروف", TransactionType.EXPENSE, s.type, viewModel::onTypeChange, Modifier.weight(1f))
                TransactionTab("دخل", TransactionType.INCOME, s.type, viewModel::onTypeChange, Modifier.weight(1f))
                TransactionTab("تحويل", TransactionType.TRANSFER, s.type, viewModel::onTypeChange, Modifier.weight(1f))
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = s.text, onValueChange = viewModel::onTextChange, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = s.referenzNummer, onValueChange = viewModel::onRefChange, label = { Text("رقم الحوالة") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = s.notiz, onValueChange = viewModel::onNotizChange, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth(), minLines = 2, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = "2026/10/18", onValueChange = {}, readOnly = true, label = { Text("التاريخ") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = s.waehrung, onValueChange = {}, readOnly = true, label = { Text("العملة") }, modifier = Modifier.weight(0.5f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }

                OutlinedTextField(
                    value = s.betrag, onValueChange = {}, readOnly = true, label = { Text("المبلغ") },
                    trailingIcon = { IconButton(onClick = { showKeypad = true }) { Icon(Icons.Default.Edit, null, tint = Color.White) } },
                    modifier = Modifier.fillMaxWidth().clickable { showKeypad = true },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                DropdownField("الفئة", kategorien.map { it.id to it.name }, s.selectedKategorieId, viewModel::onKategorieChange)

                if (s.type == TransactionType.TRANSFER) {
                    DropdownField("الحساب المصدر", konten.map { it.id to it.name }, s.selectedKontoId, viewModel::onKontoChange)
                    DropdownField("الحساب المستهدف", konten.map { it.id to it.name }, s.selectedDestKontoId, viewModel::onDestKontoChange)
                } else {
                    DropdownField("الحساب", konten.map { it.id to it.name }, s.selectedKontoId, viewModel::onKontoChange)
                }

                CheckboxRow("معاملة متكررة", false, {})
                CheckboxRow("تذكير", false, {})
                CheckboxRow("إضافة صورة", false, {})

                s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(20.dp))
            }
        }

        if (showKeypad) {
            ModalBottomSheet(onDismissRequest = { showKeypad = false }) {
                Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E))) {
                    Text(text = if (s.betrag.isEmpty()) "0" else s.betrag, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    CustomKeypad(
                        onNumberClick = { digit -> viewModel.onBetragChange(s.betrag + digit) },
                        onDeleteClick = { if (s.betrag.isNotEmpty()) viewModel.onBetragChange(s.betrag.dropLast(1)) },
                        onClearClick = { viewModel.onBetragChange("") },
                        onEqualClick = { showKeypad = false }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionTab(text: String, type: TransactionType, selectedType: TransactionType, onSelect: (TransactionType) -> Unit, modifier: Modifier) {
    val isSelected = type == selectedType
    val color = when(type) {
        TransactionType.EXPENSE -> Color(0xFFE53935)
        TransactionType.INCOME -> Color(0xFF43A047)
        TransactionType.TRANSFER -> Color(0xFF1E88E5)
    }
    OutlinedButton(
        onClick = { onSelect(type) }, modifier = modifier, shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent, contentColor = if (isSelected) color else Color.Gray),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else Color.Gray)
    ) { Text(text) }
}

@Composable
fun CheckboxRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(uncheckedColor = Color.Gray, checkedColor = Color(0xFF43A047)))
        Text(text, color = Color.LightGray, modifier = Modifier.padding(start = 8.dp))
    }
}
