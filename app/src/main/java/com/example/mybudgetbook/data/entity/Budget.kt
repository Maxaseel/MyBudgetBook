package com.example.mybudgetbook.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kommentar: String? = null,
    val maxSumme: Double,
    val datumVon: Long,
    @ColumnInfo(name = "datumVon_st") val datumVonSt: String,
    val datumBis: Long,
    @ColumnInfo(name = "datumBis_st") val datumBisSt: String,
    val periode: Int = 0,
    val abgeglichen: Int? = 0,
    val nurAusgabenBeruecksichtigen: Int = 1,
    val ueberweisen: Int = 0,
    val inSummeBeruecksichtigen: Int = 1,
    @ColumnInfo(name = "id_budget_zahlungsart") val idBudgetZahlungsart: String? = null,
    @ColumnInfo(name = "id_budget_person") val idBudgetPerson: String? = null,
    @ColumnInfo(name = "id_budget_gruppe") val idBudgetGruppe: String? = null,
    @ColumnInfo(name = "id_budget_kategorie") val idBudgetKategorie: String? = null,
    @ColumnInfo(name = "id_budget_konto") val idBudgetKonto: String? = null,
    @ColumnInfo(name = "id_budget_refid") val idBudgetRefid: Long? = null,
    val createDate: Long? = null,
    @ColumnInfo(name = "createDate_st") val createDateSt: String? = null,
    val updateDate: Long? = null,
    @ColumnInfo(name = "updateDate_st") val updateDateSt: String? = null,
)
