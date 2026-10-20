package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class TransactionType { EXPENSE, INCOME, TRANSFER }

data class AddBuchungUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val text: String = "",
    val referenzNummer: String = "",
    val beschreibung: String = "",
    val notiz: String = "",
    val betrag: String = "",
    val waehrung: String = "SAR",
    val selectedKontoId: Long = 0,
    val selectedDestKontoId: Long = 0,
    val selectedKategorieId: Long = 0,
    val selectedZahlungsartId: Long = 0,
    val datum: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

class AddBuchungViewModel(private val db: AppDatabase) : ViewModel() {
    private val _state = MutableStateFlow(AddBuchungUiState())
    val state: StateFlow<AddBuchungUiState> = _state.asStateFlow()

    val konten: StateFlow<List<Konto>> = db.kontoDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kategorien: StateFlow<List<Kategorie>> = db.kategorieDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val zahlungsarten: StateFlow<List<Zahlungsart>> = db.zahlungsartDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTypeChange(type: TransactionType) = _state.update { it.copy(type = type) }
    fun onTextChange(v: String) = _state.update { it.copy(text = v) }
    fun onRefChange(v: String) = _state.update { it.copy(referenzNummer = v) }
    fun onBeschreibungChange(v: String) = _state.update { it.copy(beschreibung = v) }
    fun onNotizChange(v: String) = _state.update { it.copy(notiz = v) }
    fun onBetragChange(v: String) = _state.update { it.copy(betrag = v) }
    fun onKontoChange(id: Long) = _state.update { it.copy(selectedKontoId = id) }
    fun onDestKontoChange(id: Long) = _state.update { it.copy(selectedDestKontoId = id) }
    fun onKategorieChange(id: Long) = _state.update { it.copy(selectedKategorieId = id) }
    fun onZahlungsartChange(id: Long) = _state.update { it.copy(selectedZahlungsartId = id) }

    fun save() {
        val s = _state.value
        if (s.text.isBlank()) { _state.update { it.copy(error = "العنوان مطلوب") }; return }
        val betrag = s.betrag.replace(",", ".").toDoubleOrNull()
        if (betrag == null || betrag <= 0.0) { _state.update { it.copy(error = "المبلغ غير صالح") }; return }
        if (s.type == TransactionType.TRANSFER && s.selectedKontoId == s.selectedDestKontoId) {
            _state.update { it.copy(error = "لا يمكن التحويل لنفس الحساب") }; return
        }

        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val now = System.currentTimeMillis()
                val betragVz = when(s.type) {
                    TransactionType.EXPENSE -> -betrag
                    TransactionType.INCOME -> betrag
                    TransactionType.TRANSFER -> -betrag
                }

                val buchung = Buchung(
                    text = s.text.trim(), beschreibung = s.beschreibung.ifBlank { null },
                    notiz = s.notiz.ifBlank { null }, referenzNummer = s.referenzNummer.ifBlank { null },
                    waehrung = s.waehrung, betrag = betrag, betragvz = betragVz,
                    buchungsdatum = s.datum, buchungsdatumSt = fmt.format(Date(s.datum)),
                    idBuchungKonto = s.selectedKontoId, idBuchungKategorie = s.selectedKategorieId,
                    zahlungsart = s.selectedZahlungsartId.toInt(), idBuchungPerson = 1, idBuchungGruppe = 1,
                    createDate = now, createDateSt = fmt.format(Date(now))
                )
                db.buchungDao().insert(buchung)
                
                if (s.type == TransactionType.TRANSFER) {
                    val destBuchung = buchung.copy(
                        id = 0, betragvz = betrag, idBuchungKonto = s.selectedDestKontoId,
                        idBuchungUmbuchung = buchung.id
                    )
                    db.buchungDao().insert(destBuchung)
                }
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
