package com.example.mybudgetbook.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mybudgetbook.data.entity.Regel
import kotlinx.coroutines.flow.Flow

@Dao
interface RegelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(regel: Regel): Long

    @Delete
    suspend fun delete(regel: Regel)

    @Query("DELETE FROM regeln WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM regeln ORDER BY id DESC")
    fun getAll(): Flow<List<Regel>>

    @Query("SELECT * FROM regeln WHERE instr(lower(:text), lower(textToMatch)) > 0 ORDER BY id DESC")
    suspend fun getMatchingRules(text: String): List<Regel>
}
