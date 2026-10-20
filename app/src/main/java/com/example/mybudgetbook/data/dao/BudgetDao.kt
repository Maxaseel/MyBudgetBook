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
                       AND buchungsdatum <= b.datumBis), 0) * -1 AS ausgegeben
        FROM budgets b
        ORDER BY b.datumVon DESC
    """)
    fun getBudgetsWithSpent(): Flow<List<BudgetMitAusgaben>>
}
