package com.example.mybudgetbook.data.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class CategoryStat(
    val name: String,
    val total: Double,
    val isIncome: Boolean
)

@Dao
interface StatDao {
    @Query("""
        SELECT k.name AS name, SUM(b.betragvz) AS total, (b.betragvz > 0) AS isIncome
        FROM buchungen b
        INNER JOIN kategorien k ON k.id = b.id_buchung_kategorie
        WHERE b.vorlage = 0
          AND NOT EXISTS (SELECT 1 FROM buchungen split_child WHERE split_child.id_buchung_splittid = b.id)
        GROUP BY b.id_buchung_kategorie, isIncome
        ORDER BY ABS(SUM(b.betragvz)) DESC
    """)
    fun getCategoryStats(): Flow<List<CategoryStat>>
}
