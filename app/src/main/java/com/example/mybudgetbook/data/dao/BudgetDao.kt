package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Budget
import com.example.mybudgetbook.data.relation.BudgetMitAusgaben
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("""
        SELECT b.*,
               IFNULL((SELECT SUM(betragvz) FROM buchungen
                       WHERE betragvz < 0 
                       AND buchungsdatum >= b.datumVon 
                       AND buchungsdatum <= b.datumBis
                       AND NOT EXISTS (SELECT 1 FROM buchungen split_child WHERE split_child.id_buchung_splittid = buchungen.id)), 0) * -1 AS ausgegeben,
               IFNULL((SELECT MAX(0, previous.maxSumme - IFNULL((
                   SELECT SUM(tx.betragvz) * -1 FROM buchungen tx
                   WHERE tx.betragvz < 0
                     AND tx.buchungsdatum >= previous.datumVon
                     AND tx.buchungsdatum <= previous.datumBis
                     AND NOT EXISTS (SELECT 1 FROM buchungen split_child WHERE split_child.id_buchung_splittid = tx.id)
               ), 0))
               FROM budgets previous
               WHERE previous.name = b.name
                 AND previous.restbudgetUebertragen = 1
                 AND previous.datumBis < b.datumVon
               ORDER BY previous.datumBis DESC LIMIT 1), 0) AS uebertragenerBetrag
        FROM budgets b
        ORDER BY b.datumVon DESC
    """)
    fun getBudgetsWithSpent(): Flow<List<BudgetMitAusgaben>>
}
