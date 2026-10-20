package com.example.mybudgetbook.ui.screens.settings

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

private const val PREFS = "mybudgetbook_settings"

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE) }
    var currency by remember { mutableStateOf(prefs.getString("currency", "SAR") ?: "SAR") }
    var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }
    var preventScreenshots by remember { mutableStateOf(prefs.getBoolean("prevent_screenshots", false)) }
    var pinEnabled by remember { mutableStateOf(prefs.getBoolean("pin_enabled", false)) }
    var biometricEnabled by remember { mutableStateOf(prefs.getBoolean("biometric_enabled", false)) }
    var startDay by remember { mutableStateOf(prefs.getInt("month_start_day", 1).toString()) }
    var currencyMenu by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("الإعدادات") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
        }) },
        containerColor = Color(0xFF121212),
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("التفضيلات", color = Color.Gray)
            TextButton(onClick = { currencyMenu = true }) { Text("العملة: $currency") }
            DropdownMenu(expanded = currencyMenu, onDismissRequest = { currencyMenu = false }) {
                listOf("SAR", "USD", "EUR", "GBP").forEach { code ->
                    DropdownMenuItem(text = { Text(code) }, onClick = {
                        currency = code; prefs.edit().putString("currency", code).apply(); currencyMenu = false
                    })
                }
            }
            SettingSwitch("الوضع الداكن", darkMode) {
                darkMode = it; prefs.edit().putBoolean("dark_mode", it).apply()
            }
            OutlinedTextField(
                value = startDay,
                onValueChange = { value ->
                    val day = value.toIntOrNull()
                    if (value.isEmpty() || (day != null && day in 1..28)) {
                        startDay = value
                        day?.let { prefs.edit().putInt("month_start_day", it).apply() }
                    }
                },
                label = { Text("يوم بداية الشهر (1–28)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text("الأمان", color = Color.Gray)
            SettingSwitch("منع لقطات الشاشة", preventScreenshots) { enabled ->
                preventScreenshots = enabled
                prefs.edit().putBoolean("prevent_screenshots", enabled).apply()
                (context as? Activity)?.window?.let { window ->
                    if (enabled) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            SettingSwitch("قفل برمز PIN", pinEnabled) { enabled ->
                if (enabled) {
                    pinValue = ""; pinError = null; showPinDialog = true
                } else {
                    pinEnabled = false
                    prefs.edit().putBoolean("pin_enabled", false).remove("pin_hash").apply()
                }
            }
            SettingSwitch("السماح بفتح القفل بالبصمة", biometricEnabled) { enabled ->
                biometricEnabled = enabled
                prefs.edit().putBoolean("biometric_enabled", enabled).apply()
            }
            Text("يظهر طلب PIN أو البصمة عند فتح التطبيق إذا فُعّل القفل.", color = Color.LightGray)
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("تم") }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("تعيين رمز PIN") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinValue,
                        onValueChange = { value -> if (value.length <= 12 && value.all(Char::isDigit)) pinValue = value },
                        label = { Text("أدخل 4–12 رقمًا") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                    )
                    pinError?.let { Text(it, color = Color.Red) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pinValue.length < 4) pinError = "الرمز يجب أن يتكون من 4 أرقام على الأقل"
                    else {
                        prefs.edit().putString("pin_hash", hashPin(pinValue)).putBoolean("pin_enabled", true).apply()
                        pinEnabled = true; showPinDialog = false
                    }
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text("إلغاء") } },
        )
    }
}

@Composable
private fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Color.White, modifier = Modifier.padding(top = 12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun hashPin(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray())
    .joinToString("") { byte -> "%02x".format(byte) }
