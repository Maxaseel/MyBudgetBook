package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbook.data.AppDatabase
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class DailyStat(val dayOfMonth: Int, val income: Double, val expense: Double, val balance: Double)

class CalendarViewModel(private val db: AppDatabase) : ViewModel() {
    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()

    val dailyStats: StateFlow<Map<Int, DailyStat>> = _currentMonth.flatMapLatest { cal ->
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        
        val startCal = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        val endCal = Calendar.getInstance().apply { set(year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59); set(Calendar.MILLISECOND, 999) }

        db.buchungDao().getBuchungenBetween(startCal.timeInMillis, endCal.timeInMillis).map { list ->
            val map = mutableMapOf<Int, DailyStat>()
            for (i in 1..cal.getActualMaximum(Calendar.DAY_OF_MONTH)) map[i] = DailyStat(i, 0.0, 0.0, 0.0)
            val grouped = list.groupBy { Calendar.getInstance().apply { timeInMillis = it.buchungsdatum }.get(Calendar.DAY_OF_MONTH) }
            for ((day, buchungen) in grouped) {
                val inc = buchungen.filter { it.betragvz > 0 }.sumOf { it.betragvz }
                val exp = buchungen.filter { it.betragvz < 0 }.sumOf { it.betragvz }
                map[day] = DailyStat(day, inc, exp, inc + exp)
            }
            map.toMap()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun previousMonth() { _currentMonth.update { (it.clone() as Calendar).apply { add(Calendar.MONTH, -1) } } }
    fun nextMonth() { _currentMonth.update { (it.clone() as Calendar).apply { add(Calendar.MONTH, 1) } } }
}
