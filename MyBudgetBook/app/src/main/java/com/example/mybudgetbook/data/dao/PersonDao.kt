package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: Person): Long

    @Query("SELECT * FROM personen ORDER BY name")
    fun getAll(): Flow<List<Person>>

    @Query("SELECT COUNT(id) FROM personen")
    suspend fun count(): Int
}
