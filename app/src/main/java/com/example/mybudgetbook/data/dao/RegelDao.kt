package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Regel
import kotlinx.coroutines.flow.Flow

@Dao
interface RegelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(regel: Regel): Long

    @Delete
    suspend fun delete(regel: Regel)

    @Query("SELECT * FROM regeln ORDER BY id DESC")
    fun getAll(): Flow<List<Regel>>

    @Query("SELECT * FROM regeln WHERE textToMatch LIKE '%' || :text || '%'")
    fun getMatchingRules(text: String): Flow<List<Regel>>
}
