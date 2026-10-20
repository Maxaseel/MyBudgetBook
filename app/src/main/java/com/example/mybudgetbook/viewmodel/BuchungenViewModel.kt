package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.relation.BuchungMitDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BuchungenViewModel(db: AppDatabase) : ViewModel() {
    val buchungen: StateFlow<List<BuchungMitDetails>> = db.buchungDao().getAllWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val balance: StateFlow<Double?> = db.buchungDao().getTotalBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val income: StateFlow<Double?> = db.buchungDao().getTotalIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
