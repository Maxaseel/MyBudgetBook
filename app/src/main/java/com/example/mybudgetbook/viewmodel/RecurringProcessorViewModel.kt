package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.Buchung
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Runs due recurring transactions when the app starts; generated rows link back to their master. */
class RecurringProcessorViewModel(private val db: AppDatabase) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) { processDueRecurringTransactions() }
    }

    suspend fun processDueRecurringTransactions(now: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val dao = db.buchungDao()
        val masters = dao.getRecurringMasters()
        for (master in masters) {
            db.withTransaction {
                var generatedCount = dao.getRecurringExecutionCount(master.id)
                while (generatedCount < MAX_CATCH_UP_PER_MASTER) {
                    val repeatLimit = master.anzahlWiederholungen
                    if (repeatLimit != null && generatedCount >= repeatLimit) break
                    val scheduledAt = nextScheduledDate(master.buchungsdatum, master.periode, generatedCount + 1) ?: break
                    val executionAt = adjustWeekend(scheduledAt, master.wochenende == 1)
                    if (master.offenesEnde == 0 && master.ablaufdatum != null && scheduledAt > master.ablaufdatum) break
                    if (executionAt > now) break

                    val current = System.currentTimeMillis()
                    val executionDate = Date(executionAt)
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    val occurrence = master.copy(
                        id = 0,
                        buchungsdatum = executionAt,
                        buchungsdatumSt = dateFormatter.format(executionDate),
                        dauerauftrag = 0,
                        splittbuchung = 0,
                        idBuchungSplittid = null,
                        idBuchungRefid = master.id,
                        createDate = current,
                        createDateSt = dateFormatter.format(Date(current)),
                        updateDate = null,
                        updateDateSt = null,
                    )
                    dao.insert(occurrence)
                    generatedCount++
                }
            }
        }
    }

    private fun nextScheduledDate(start: Long, period: Int?, occurrenceNumber: Int): Long? {
        val calendar = Calendar.getInstance().apply { timeInMillis = start }
        repeat(occurrenceNumber) {
            when (period) {
                PERIOD_DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
                PERIOD_WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                PERIOD_MONTHLY -> calendar.add(Calendar.MONTH, 1)
                PERIOD_YEARLY -> calendar.add(Calendar.YEAR, 1)
                else -> return null
            }
        }
        return calendar.timeInMillis
    }

    /** When weekend adjustment is enabled, Saturday/Sunday executions move to Monday. */
    private fun adjustWeekend(timestamp: Long, enabled: Boolean): Long {
        if (!enabled) return timestamp
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> calendar.add(Calendar.DAY_OF_MONTH, 2)
            Calendar.SUNDAY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }

    private companion object {
        const val MAX_CATCH_UP_PER_MASTER = 366
        const val PERIOD_DAILY = 1
        const val PERIOD_WEEKLY = 2
        const val PERIOD_MONTHLY = 3
        const val PERIOD_YEARLY = 4
    }
}
