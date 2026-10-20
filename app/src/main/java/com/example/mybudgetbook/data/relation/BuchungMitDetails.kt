package com.example.mybudgetbook.data.relation

data class BuchungMitDetails(
    val id: Long,
    val titel: String,
    val kommentar: String?,
    val notiz: String?,
    val betragVz: Double,
    val buchungsdatum: String,
    val kategorieName: String?,
    val zahlungsartName: String?,
    val personName: String?,
    val gruppeName: String?,
    val kontoName: String?,
)
