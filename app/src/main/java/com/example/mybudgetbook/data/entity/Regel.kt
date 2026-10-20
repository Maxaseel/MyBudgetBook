package com.example.mybudgetbook.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regeln")
data class Regel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val textToMatch: String,
    val kategorieId: Long
)
