package com.example.mybudgetbook.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zahlungsarten")
data class Zahlungsart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val geschuetzt: Int = 0,
    val createDate: Long? = null,
    @ColumnInfo(name = "createDate_st") val createDateSt: String? = null,
    val updateDate: Long? = null,
    @ColumnInfo(name = "updateDate_st") val updateDateSt: String? = null,
)
