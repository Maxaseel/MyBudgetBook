package com.example.mybudgetbook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransactionType { EXPENSE, INCOME, TRANSFER }

data class AutofillSuggestion(
    val previousText: String,
    val amount: Double,
    val categoryId: Long,
    val accountId: Long,
    val type: TransactionType,
)

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
    val autofillSuggestion: AutofillSuggestion? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

class AddBuchungViewModel(private val db: AppDatabase) : ViewModel() {
    private val _state = MutableStateFlow(AddBuchungUiState())
    val state: StateFlow<AddBuchungUiState> = _state.asStateFlow()
    private var suggestionJob: Job? = null

    val konten: StateFlow<List<Konto>> = db.kontoDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kategorien: StateFlow<List<Kategorie>> = db.kategorieDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val zahlungsarten: StateFlow<List<Zahlungsart>> = db.zahlungsartDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTypeChange(type: TransactionType) = _state.update { it.copy(type = type) }
    fun onTextChange(v: String) {
        _state.update { it.copy(text = v, autofillSuggestion = null) }
        suggestionJob?.cancel()
        if (v.trim().length < 2) return
        suggestionJob = viewModelScope.launch {
            delay(250)
            val suggestion = suggestFromPreviousTransactions(v)
            if (_state.value.text == v) _state.update { it.copy(autofillSuggestion = suggestion) }
        }
    }
    fun onRefChange(v: String) = _state.update { it.copy(referenzNummer = v) }
    fun onBeschreibungChange(v: String) = _state.update { it.copy(beschreibung = v) }
    fun onNotizChange(v: String) = _state.update { it.copy(notiz = v) }
    fun onBetragChange(v: String) = _state.update { it.copy(betrag = v) }
    fun onKontoChange(id: Long) = _state.update { it.copy(selectedKontoId = id) }
    fun onDestKontoChange(id: Long) = _state.update { it.copy(selectedDestKontoId = id) }
    fun onKategorieChange(id: Long) = _state.update { it.copy(selectedKategorieId = id) }
    fun onZahlungsartChange(id: Long) = _state.update { it.copy(selectedZahlungsartId = id) }

    suspend fun suggestFromPreviousTransactions(input: String): AutofillSuggestion? {
        val normalized = input.trim().lowercase(Locale.ROOT)
        if (normalized.length < 2) return null
        val match = db.buchungDao().findRecentBySimilarText(normalized)
            .firstOrNull { it.text.trim().equals(input.trim(), ignoreCase = true) }
            ?: db.buchungDao().findRecentBySimilarText(normalized).firstOrNull()
            ?: return null
        val type = when {
            match.idBuchungUmbuchung != null -> TransactionType.TRANSFER
            match.betragvz >= 0.0 -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }
        return AutofillSuggestion(
            previousText = match.text,
            amount = match.betrag,
            categoryId = match.idBuchungKategorie,
            accountId = match.idBuchungKonto,
            type = type,
        )
    }

    fun applyAutofillSuggestion() {
        val suggestion = _state.value.autofillSuggestion ?: return
        _state.update {
            it.copy(
                betrag = suggestion.amount.toString(),
                selectedKategorieId = suggestion.categoryId,
                selectedKontoId = suggestion.accountId,
                type = suggestion.type,
                autofillSuggestion = null,
            )
        }
    }

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
                val betragVz = when (s.type) {
                    TransactionType.EXPENSE -> -betrag
                    TransactionType.INCOME -> betrag
                    TransactionType.TRANSFER -> -betrag
                }
                val categoryId = if (s.selectedKategorieId == 0L) {
                    db.regelDao().getMatchingRules(s.text).firstOrNull()?.kategorieId ?: 0L
                } else s.selectedKategorieId

                val buchung = Buchung(
                    text = s.text.trim(), beschreibung = s.beschreibung.ifBlank { null },
                    notiz = s.notiz.ifBlank { null }, referenzNummer = s.referenzNummer.ifBlank { null },
                    waehrung = s.waehrung, betrag = betrag, betragvz = betragVz,
                    buchungsdatum = s.datum, buchungsdatumSt = fmt.format(Date(s.datum)),
                    idBuchungKonto = s.selectedKontoId, idBuchungKategorie = categoryId,
                    zahlungsart = s.selectedZahlungsartId.toInt(), idBuchungPerson = 1, idBuchungGruppe = 1,
                    createDate = now, createDateSt = fmt.format(Date(now)),
                )

                db.withTransaction {
                    val insertedId = db.buchungDao().insert(buchung)
                    if (s.type == TransactionType.TRANSFER) {
                        db.buchungDao().insert(
                            buchung.copy(
                                id = 0,
                                betragvz = betrag,
                                idBuchungKonto = s.selectedDestKontoId,
                                idBuchungUmbuchung = insertedId,
                            ),
                        )
                    }
                }
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "تعذر حفظ المعاملة") }
            }
        }
    }
}
