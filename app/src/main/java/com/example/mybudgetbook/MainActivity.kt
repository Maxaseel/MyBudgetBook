package com.example.mybudgetbook

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.DatabaseSeeder
import com.example.mybudgetbook.ui.navigation.AppNavigation
import com.example.mybudgetbook.ui.theme.MyBudgetBookTheme
import kotlinx.coroutines.launch
import java.security.MessageDigest

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("mybudgetbook_settings", MODE_PRIVATE)
        applyScreenshotSetting(preferences.getBoolean("prevent_screenshots", false))

        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch { DatabaseSeeder(db).seedIfEmpty() }

        setContent {
            val pin = preferences.getString("pin_hash", null)
            val shouldLock = preferences.getBoolean("pin_enabled", false) && !pin.isNullOrBlank()
            var unlocked by remember { mutableStateOf(!shouldLock) }
            MyBudgetBookTheme {
                if (shouldLock && !unlocked) {
                    PinLockScreen(
                        expectedPin = pin.orEmpty(),
                        biometricEnabled = preferences.getBoolean("biometric_enabled", false),
                        onUnlocked = { unlocked = true },
                    )
                } else {
                    AppNavigation(db)
                }
            }
        }
    }

    private fun applyScreenshotSetting(enabled: Boolean) {
        if (enabled) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

@Composable
private fun PinLockScreen(expectedPin: String, biometricEnabled: Boolean, onUnlocked: () -> Unit) {
    val activity = LocalContext.current as? FragmentActivity
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val prompt = remember(activity) {
        activity?.let { host ->
            BiometricPrompt(
                host,
                ContextCompat.getMainExecutor(host),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onUnlocked()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        error = errString.toString()
                    }
                    override fun onAuthenticationFailed() {
                        error = "لم يتم التعرف على البصمة"
                    }
                },
            )
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("التطبيق محمي")
        OutlinedTextField(
            value = enteredPin,
            onValueChange = { value -> if (value.length <= 12 && value.all(Char::isDigit)) enteredPin = value },
            label = { Text("رمز PIN") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
        error?.let { Text(it) }
        Button(onClick = {
            if (hashPin(enteredPin) == expectedPin) onUnlocked() else error = "رمز PIN غير صحيح"
        }, modifier = Modifier.fillMaxWidth()) { Text("فتح التطبيق") }
        if (biometricEnabled) {
            Button(
                onClick = {
                    val info = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("فتح MyBudgetBook")
                        .setSubtitle("تحقق من هويتك بالبصمة")
                        .setNegativeButtonText("استخدام PIN")
                        .build()
                    prompt?.authenticate(info) ?: run { error = "تعذر تشغيل المصادقة الحيوية" }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("فتح بالبصمة") }
        }
    }
}

private fun hashPin(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray())
    .joinToString("") { byte -> "%02x".format(byte) }
