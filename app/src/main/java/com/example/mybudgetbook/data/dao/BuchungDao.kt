package com.example.mybudgetbook.data.dao

import androidx.room.*
import com.example.mybudgetbook.data.entity.Buchung
import com.example.mybudgetbook.data.relation.BuchungMitDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface BuchungDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(buchung: Buchung): Long

    @Update
    suspend fun update(buchung: Buchung)

    @Delete
    suspend fun delete(buchung: Buchung)

    @Query("DELETE FROM buchungen WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM buchungen WHERE id = :id")
    suspend fun getById(id: Long): Buchung?

    @Query("""
        SELECT
            buchungen.id AS id,
            buchungen.text AS titel,
            buchungen.beschreibung AS kommentar,
            buchungen.notiz AS notiz,
            buchungen.betragvz AS betragVz,
            MAX(buchungsdatum_st) AS buchungsdatum,
            kategorien.name AS kategorieName,
            zahlungsarten.name AS zahlungsartName,
            personen.name AS personName,
            gruppen.name AS gruppeName,
            konten.name AS kontoName
        FROM buchungen
        INNER JOIN kategorien ON kategorien.id = buchungen.id_buchung_kategorie
        INNER JOIN konten ON konten.id = buchungen.id_buchung_konto
        INNER JOIN zahlungsarten ON zahlungsarten.id = buchungen.zahlungsart
        INNER JOIN personen ON personen.id = buchungen.id_buchung_person
        INNER JOIN gruppen ON gruppen.id = buchungen.id_buchung_gruppe
        WHERE buchungen.vorlage = 0
        GROUP BY buchungen.id
        ORDER BY buchungsdatum_st DESC
    """)
    fun getAllWithDetails(): Flow<List<BuchungMitDetails>>

    @Query("SELECT SUM(betragvz) FROM buchungen WHERE vorlage = 0")
    fun getTotalBalance(): Flow<Double?>

    @Query("SELECT SUM(betragvz) FROM buchungen WHERE betragvz > 0 AND vorlage = 0")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT * FROM buchungen WHERE buchungsdatum >= :start AND buchungsdatum <= :end AND vorlage = 0")
    fun getBuchungenBetween(start: Long, end: Long): Flow<List<Buchung>>

    @Query("SELECT * FROM buchungen WHERE dauerauftrag = 1 AND vorlage = 0 ORDER BY buchungsdatum DESC")
    fun getRecurringTransactions(): Flow<List<Buchung>>

    @Query("SELECT * FROM buchungen WHERE dauerauftrag = 1 AND vorlage = 0 ORDER BY buchungsdatum ASC")
    suspend fun getRecurringMasters(): List<Buchung>

    @Query("SELECT * FROM buchungen WHERE id_buchung_refid = :masterId ORDER BY buchungsdatum DESC LIMIT 1")
    suspend fun getLatestRecurringExecution(masterId: Long): Buchung?

    @Query("SELECT COUNT(*) FROM buchungen WHERE id_buchung_refid = :masterId")
    suspend fun getRecurringExecutionCount(masterId: Long): Int

    @Query("SELECT * FROM buchungen WHERE vorlage = 0 AND length(:input) >= 2 AND (instr(lower(text), lower(:input)) > 0 OR instr(lower(:input), lower(text)) > 0) ORDER BY buchungsdatum DESC LIMIT 30")
    suspend fun findRecentBySimilarText(input: String): List<Buchung>

    @Query("SELECT * FROM buchungen WHERE vorlage = 1 ORDER BY createDate DESC")
    fun getTemplates(): Flow<List<Buchung>>
}
