package com.example.mybudgetbook.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buchungen")
data class Buchung(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val beschreibung: String? = null,
    val notiz: String? = null,
    val waehrung: String = "SAR",
    val referenzNummer: String? = null,
    val betrag: Double,
    val betragvz: Double,
    val buchungsdatum: Long,
    @ColumnInfo(name = "buchungsdatum_st") val buchungsdatumSt: String,
    val ablaufdatum: Long? = null,
    @ColumnInfo(name = "ablaufdatum_st") val ablaufdatumSt: String? = null,
    val anzahlWiederholungen: Int? = null,
    val dauerauftrag: Int = 0,
    val periode: Int? = null,
    val offenesEnde: Int = 0,
    val wochenende: Int = 0,
    val zahlungsart: Int = 0,
    val markierung: Int = 0,
    val vorlage: Int = 0,
    val art: Int = 0,
    val abgeglichen: Int = 0,
    val erinnerungErstellen: Int = 0,
    val erinnerungsdatum: String? = null,
    val splittbuchung: Int = 0,
    @ColumnInfo(name = "id_buchung_splittid") val idBuchungSplittid: Long? = null,
    @ColumnInfo(name = "id_buchung_kategorie") val idBuchungKategorie: Long = 0,
    @ColumnInfo(name = "id_buchung_konto") val idBuchungKonto: Long = 0,
    @ColumnInfo(name = "id_buchung_person") val idBuchungPerson: Long = 0,
    @ColumnInfo(name = "id_buchung_gruppe") val idBuchungGruppe: Long = 0,
    val fotoIds: String? = null,
    @ColumnInfo(name = "id_vorlage_konto") val idVorlageKonto: Long? = null,
    @ColumnInfo(name = "id_buchung_refid") val idBuchungRefid: Long? = null,
    @ColumnInfo(name = "id_buchung_umbuchung") val idBuchungUmbuchung: Long? = null,
    @ColumnInfo(name = "id_buchung_csvimport") val idBuchungCsvimport: Long? = null,
    val createDate: Long? = null,
    @ColumnInfo(name = "createDate_st") val createDateSt: String? = null,
    val updateDate: Long? = null,
    @ColumnInfo(name = "updateDate_st") val updateDateSt: String? = null,
)
