package com.yatha.moneytracker.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yatha.moneytracker.data.repository.ExpenseRepository
import com.yatha.moneytracker.domain.model.Expense
import com.yatha.moneytracker.domain.model.ExpenseCategory
import com.yatha.moneytracker.domain.time.CalendarDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.TimeZone

/**
 * State holder for the add-expense form and the transaction list.
 *
 * @param clock Current time in epoch milliseconds; replaceable in tests.
 * @param timeZone The time zone that defines "today"; replaceable in tests.
 */
class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val clock: () -> Long = System::currentTimeMillis,
    private val timeZone: () -> TimeZone = TimeZone::getDefault,
) : ViewModel() {

    private val form = MutableStateFlow(emptyForm())

    /**
     * The form. This is a plain `MutableStateFlow` updated synchronously, which keeps text
     * fields responsive while typing.
     */
    val formState: StateFlow<ExpenseFormState> = form.asStateFlow()

    /** Saved expenses, newest first. Updates automatically whenever the database changes. */
    val listState: StateFlow<ExpenseListState> = repository.observeExpenses()
        .map<List<Expense>, ExpenseListState> { ExpenseListState.Loaded(it) }
        .catch { emit(ExpenseListState.Error) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ExpenseListState.Loading,
        )

    fun onAmountChange(text: String) = form.update {
        it.copy(amountText = text, errors = it.errors.copy(amount = null))
    }

    fun onCategorySelect(category: ExpenseCategory) = form.update {
        it.copy(category = category, errors = it.errors.copy(categoryMissing = false))
    }

    fun onDescriptionChange(text: String) = form.update {
        it.copy(description = text, errors = it.errors.copy(descriptionTooLong = false))
    }

    fun onDateSelect(date: CalendarDate) = form.update {
        it.copy(date = date, errors = it.errors.copy(dateInFuture = false))
    }

    /** Call after the [ExpenseFormState.message] has been shown to the user. */
    fun onMessageShown() = form.update { it.copy(message = null) }

    /**
     * Validates the form and, if valid, saves a new expense.
     *
     * - Calls made while a save is already running are ignored (no duplicate expenses).
     * - On success the form is reset and [ExpenseFormMessage.SAVED] is reported.
     * - On failure everything the user typed is kept and [ExpenseFormMessage.SAVE_FAILED]
     *   is reported, so they can retry.
     */
    fun saveExpense() {
        val current = form.value
        if (current.isSaving) return

        val zone = timeZone()
        val today = CalendarDate.of(clock(), zone)
        val valid = when (val result = validateExpenseForm(current, today)) {
            is ExpenseFormValidation.Invalid -> {
                form.update { it.copy(errors = result.errors, message = null) }
                return
            }
            is ExpenseFormValidation.Valid -> result
        }

        // Set synchronously, before launching, so a second tap sees isSaving = true.
        form.update { it.copy(isSaving = true, errors = ExpenseFormErrors(), message = null) }

        viewModelScope.launch {
            val saved = try {
                repository.addExpense(
                    Expense(
                        id = ExpenseRepository.NEW_EXPENSE_ID,
                        amount = valid.amountPaise,
                        currencyCode = Expense.DEFAULT_CURRENCY_CODE,
                        category = valid.category,
                        description = valid.description,
                        transactionDate = valid.date.toLocalNoonMillis(zone),
                        createdAt = clock(),
                    ),
                )
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Not logged: log output could expose financial details. The UI tells the user.
                false
            }

            form.update {
                if (saved) {
                    emptyForm().copy(message = ExpenseFormMessage.SAVED)
                } else {
                    it.copy(isSaving = false, message = ExpenseFormMessage.SAVE_FAILED)
                }
            }
        }
    }

    private fun emptyForm() = ExpenseFormState(date = CalendarDate.of(clock(), timeZone()))

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        /** Manual wiring; no dependency-injection framework (see docs/ARCHITECTURE.md). */
        fun factory(repository: ExpenseRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExpenseViewModel(repository) }
        }
    }
}
