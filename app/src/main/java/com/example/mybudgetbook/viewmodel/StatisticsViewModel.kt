package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.dao.CategoryStat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(db: AppDatabase) : ViewModel() {
    val categoryStats: StateFlow<List<CategoryStat>> = db.statDao().getCategoryStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
