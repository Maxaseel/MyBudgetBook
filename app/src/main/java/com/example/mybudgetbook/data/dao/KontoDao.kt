package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Konto
import com.example.mybudgetbook.data.relation.KontoMitSaldo
import kotlinx.coroutines.flow.Flow

@Dao
interface KontoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(konto: Konto): Long

    @Update
    suspend fun update(konto: Konto)

    @Delete
    suspend fun delete(konto: Konto)

    @Query("SELECT * FROM konten ORDER BY name")
    fun getAll(): Flow<List<Konto>>

    @Query("SELECT * FROM konten WHERE id = :id")
    suspend fun getById(id: Long): Konto?

    @Query("SELECT COUNT(id) FROM konten")
    suspend fun count(): Int

    @Query("""
        SELECT k.*, 
               (IFNULL(k.startsaldo, 0) + IFNULL((SELECT SUM(b.betragvz) FROM buchungen b WHERE b.id_buchung_konto = k.id AND NOT EXISTS (SELECT 1 FROM buchungen split_child WHERE split_child.id_buchung_splittid = b.id)), 0)) AS saldo
        FROM konten k
        ORDER BY k.name ASC
    """)
    fun getAccountsWithBalance(): Flow<List<KontoMitSaldo>>
}
