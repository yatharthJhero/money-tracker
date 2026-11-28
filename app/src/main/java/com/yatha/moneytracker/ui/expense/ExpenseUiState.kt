package com.yatha.moneytracker.ui.expense

import com.yatha.moneytracker.domain.model.Expense
import com.yatha.moneytracker.domain.model.ExpenseCategory
import com.yatha.moneytracker.domain.money.AmountError
import com.yatha.moneytracker.domain.time.CalendarDate

/**
 * Everything the add-expense form shows. Owned by [ExpenseViewModel].
 *
 * @property amountText Exactly what the user typed; parsed only when saving.
 * @property category `null` until the user picks one. Picking is required.
 * @property date The transaction date; defaults to today.
 * @property isSaving `true` while a save is in progress. Inputs and the Save button are disabled.
 * @property message One-off feedback to show (e.g. in a snackbar), then clear via
 *   [ExpenseViewModel.onMessageShown].
 */
data class ExpenseFormState(
    val amountText: String = "",
    val category: ExpenseCategory? = null,
    val description: String = "",
    val date: CalendarDate,
    val errors: ExpenseFormErrors = ExpenseFormErrors(),
    val isSaving: Boolean = false,
    val message: ExpenseFormMessage? = null,
)

/** Validation problems in the form; all `null`/`false` means no errors are shown. */
data class ExpenseFormErrors(
    val amount: AmountError? = null,
    val categoryMissing: Boolean = false,
    val dateInFuture: Boolean = false,
    val descriptionTooLong: Boolean = false,
) {
    val hasAny: Boolean
        get() = amount != null || categoryMissing || dateInFuture || descriptionTooLong
}

enum class ExpenseFormMessage {
    SAVED,
    SAVE_FAILED,
}

/** The transaction list below the form. */
sealed interface ExpenseListState {
    data object Loading : ExpenseListState

    /** Newest transaction first, as ordered by the repository. */
    data class Loaded(val expenses: List<Expense>) : ExpenseListState

    data object Error : ExpenseListState
}
