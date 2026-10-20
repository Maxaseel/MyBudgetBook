package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Kategorie
import kotlinx.coroutines.flow.Flow

@Dao
interface KategorieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kategorie: Kategorie): Long

    @Update
    suspend fun update(kategorie: Kategorie)

    @Delete
    suspend fun delete(kategorie: Kategorie)

    @Query("SELECT * FROM kategorien ORDER BY name")
    fun getAll(): Flow<List<Kategorie>>

    @Query("SELECT * FROM kategorien WHERE id = :id")
    suspend fun getById(id: Long): Kategorie?

    @Query("SELECT COUNT(id) FROM kategorien")
    suspend fun count(): Int
}
