package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Zahlungsart
import kotlinx.coroutines.flow.Flow

@Dao
interface ZahlungsartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zahlungsart: Zahlungsart): Long

    @Query("SELECT * FROM zahlungsarten ORDER BY name")
    fun getAll(): Flow<List<Zahlungsart>>

    @Query("SELECT COUNT(id) FROM zahlungsarten")
    suspend fun count(): Int
}
