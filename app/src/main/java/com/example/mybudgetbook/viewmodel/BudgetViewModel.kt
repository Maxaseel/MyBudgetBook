package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.Budget
import com.example.mybudgetbook.data.relation.BudgetMitAusgaben
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetViewModel(private val db: AppDatabase) : ViewModel() {
    val budgets: StateFlow<List<BudgetMitAusgaben>> = db.budgetDao().getBudgetsWithSpent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBudget(name: String, maxSumme: Double, restbudgetUebertragen: Boolean = false) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val start = cal.timeInMillis
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis
            val budget = Budget(name = name, maxSumme = maxSumme, datumVon = start, datumVonSt = fmt.format(start), datumBis = end, datumBisSt = fmt.format(end), restbudgetUebertragen = restbudgetUebertragen)
            db.budgetDao().insert(budget)
        }
    }
}
