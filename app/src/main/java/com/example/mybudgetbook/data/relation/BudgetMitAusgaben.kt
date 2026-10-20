package com.example.mybudgetbook.data.relation

import androidx.room.Embedded
import com.example.mybudgetbook.data.entity.Budget

data class BudgetMitAusgaben(
    @Embedded val budget: Budget,
    val ausgegeben: Double,
    val uebertragenerBetrag: Double = 0.0,
)
