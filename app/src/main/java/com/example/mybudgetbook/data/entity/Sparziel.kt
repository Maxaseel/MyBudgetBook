package com.example.mybudgetbook.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sparziele")
data class Sparziel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val betrag: Double,
    val datumVon: Long,
    val datumBis: Long,
    val idSparzielKonto: Long = 0L
)
