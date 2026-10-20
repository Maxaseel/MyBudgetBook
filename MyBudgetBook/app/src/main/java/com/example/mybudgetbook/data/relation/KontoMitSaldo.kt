package com.example.mybudgetbook.data.relation

import androidx.room.Embedded
import com.example.mybudgetbook.data.entity.Konto

data class KontoMitSaldo(
    @Embedded val konto: Konto,
    val saldo: Double
)
