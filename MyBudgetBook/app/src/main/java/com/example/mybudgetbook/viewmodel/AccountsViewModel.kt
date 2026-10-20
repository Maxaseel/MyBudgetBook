package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.Konto
import com.example.mybudgetbook.data.relation.KontoMitSaldo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(private val db: AppDatabase) : ViewModel() {
    val accounts: StateFlow<List<KontoMitSaldo>> = db.kontoDao().getAccountsWithBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun deleteAccount(konto: Konto) { viewModelScope.launch { db.kontoDao().delete(konto) } }
}

class AddEditAccountViewModel(private val db: AppDatabase, private val kontoId: Long) : ViewModel() {
    fun saveAccount(konto: Konto, onSaved: () -> Unit) {
        viewModelScope.launch {
            if (konto.id == 0L) db.kontoDao().insert(konto) else db.kontoDao().update(konto)
            onSaved()
        }
    }
    fun deleteAccount(konto: Konto, onDeleted: () -> Unit) {
        viewModelScope.launch { db.kontoDao().delete(konto); onDeleted() }
    }
    suspend fun loadKonto(id: Long): Konto? = db.kontoDao().getById(id)
}
