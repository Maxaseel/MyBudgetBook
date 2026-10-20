package com.example.mybudgetbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.DatabaseSeeder
import com.example.mybudgetbook.ui.navigation.AppNavigation
import com.example.mybudgetbook.ui.theme.MyBudgetBookTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch {
            DatabaseSeeder(db).seedIfEmpty()
        }

        setContent {
            MyBudgetBookTheme {
                AppNavigation(db)
            }
        }
    }
}
