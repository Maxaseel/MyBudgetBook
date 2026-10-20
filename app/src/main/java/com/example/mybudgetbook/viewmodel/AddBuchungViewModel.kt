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

data class SplitPart(val amount: String = "", val categoryId: Long = 0L, val description: String = "")

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
    val isRecurring: Boolean = false,
    val recurringPeriod: Int = 3,
    val recurringEndDate: Long? = null,
    val executeOnWeekends: Boolean = false,
    val isSplit: Boolean = false,
    val splitParts: List<SplitPart> = listOf(SplitPart()),
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
    fun onDateChange(date: Long) = _state.update { it.copy(datum = date) }
    fun onRecurringChange(enabled: Boolean) = _state.update { it.copy(isRecurring = enabled) }
    fun onRecurringPeriodChange(period: Int) = _state.update { it.copy(recurringPeriod = period) }
    fun onRecurringEndDateChange(date: Long?) = _state.update { it.copy(recurringEndDate = date) }
    fun onExecuteOnWeekendsChange(enabled: Boolean) = _state.update { it.copy(executeOnWeekends = enabled) }
    fun onSplitChange(enabled: Boolean) = _state.update {
        it.copy(isSplit = enabled, splitParts = if (enabled && it.splitParts.isEmpty()) listOf(SplitPart()) else it.splitParts)
    }
    fun addSplitPart() = _state.update { it.copy(splitParts = it.splitParts + SplitPart()) }
    fun removeSplitPart(index: Int) = _state.update {
        if (it.splitParts.size <= 1) it else it.copy(splitParts = it.splitParts.filterIndexed { i, _ -> i != index })
    }
    fun updateSplitPart(index: Int, part: SplitPart) = _state.update {
        it.copy(splitParts = it.splitParts.mapIndexed { i, old -> if (i == index) part else old })
    }

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
        if (s.isSplit && s.type == TransactionType.TRANSFER) {
            _state.update { it.copy(error = "لا يمكن تقسيم معاملة التحويل حاليًا") }; return
        }
        if (s.isRecurring && s.isSplit) {
            _state.update { it.copy(error = "اختر التكرار أو التقسيم، ولا تجمع بينهما") }; return
        }
        if (s.isSplit) {
            val parts = s.splitParts.map { it.amount.replace(",", ".").toDoubleOrNull() }
            if (parts.size < 2 || parts.any { it == null || it <= 0.0 } || kotlin.math.abs(parts.filterNotNull().sum() - betrag) > 0.01) {
                _state.update { it.copy(error = "أدخل جزأين صالحين على الأقل، ويجب أن يساوي مجموعهما المبلغ الأساسي") }; return
            }
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
                    dauerauftrag = if (s.isRecurring) 1 else 0,
                    periode = if (s.isRecurring) s.recurringPeriod else null,
                    ablaufdatum = if (s.isRecurring) s.recurringEndDate else null,
                    ablaufdatumSt = if (s.isRecurring) s.recurringEndDate?.let { fmt.format(Date(it)) } else null,
                    offenesEnde = if (s.isRecurring && s.recurringEndDate == null) 1 else 0,
                    wochenende = if (s.isRecurring && s.executeOnWeekends) 1 else 0,
                    splittbuchung = if (s.isSplit) 1 else 0,
                    idBuchungKonto = s.selectedKontoId, idBuchungKategorie = categoryId,
                    zahlungsart = s.selectedZahlungsartId.toInt(), idBuchungPerson = 1, idBuchungGruppe = 1,
                    createDate = now, createDateSt = fmt.format(Date(now)),
                )

                db.withTransaction {
                    val insertedId = db.buchungDao().insert(buchung)
                    if (s.isSplit) {
                        s.splitParts.forEach { part ->
                            val partAmount = part.amount.replace(",", ".").toDouble()
                            db.buchungDao().insert(
                                buchung.copy(
                                    id = 0,
                                    text = s.text.trim(),
                                    beschreibung = part.description.ifBlank { s.beschreibung.ifBlank { null } },
                                    betrag = partAmount,
                                    betragvz = if (s.type == TransactionType.EXPENSE) -partAmount else partAmount,
                                    idBuchungKategorie = part.categoryId.takeIf { it != 0L } ?: categoryId,
                                    splittbuchung = 1,
                                    idBuchungSplittid = insertedId,
                                    dauerauftrag = 0,
                                    ablaufdatum = null,
                                    periode = null,
                                ),
                            )
                        }
                    } else if (s.type == TransactionType.TRANSFER) {
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
