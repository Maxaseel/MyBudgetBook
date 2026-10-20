package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Gruppe
import kotlinx.coroutines.flow.Flow

@Dao
interface GruppeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gruppe: Gruppe): Long

    @Query("SELECT * FROM gruppen ORDER BY name")
    fun getAll(): Flow<List<Gruppe>>

    @Query("SELECT COUNT(id) FROM gruppen")
    suspend fun count(): Int
}
