package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Sparziel
import kotlinx.coroutines.flow.Flow

@Dao
interface SparzielDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sparziel: Sparziel): Long

    @Delete
    suspend fun delete(sparziel: Sparziel)

    @Query("SELECT * FROM sparziele ORDER BY datumBis ASC")
    fun getAll(): Flow<List<Sparziel>>
}
