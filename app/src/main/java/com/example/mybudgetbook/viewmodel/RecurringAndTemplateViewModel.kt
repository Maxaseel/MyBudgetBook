package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.Buchung
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RecurringAndTemplateViewModel(db: AppDatabase) : ViewModel() {
    val recurring: StateFlow<List<Buchung>> = db.buchungDao().getRecurringTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val templates: StateFlow<List<Buchung>> = db.buchungDao().getTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
