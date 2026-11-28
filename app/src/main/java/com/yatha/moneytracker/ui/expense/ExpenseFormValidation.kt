package com.yatha.moneytracker.ui.expense

import com.yatha.moneytracker.domain.model.ExpenseCategory
import com.yatha.moneytracker.domain.money.AmountParseResult
import com.yatha.moneytracker.domain.money.RupeeAmountParser
import com.yatha.moneytracker.domain.time.CalendarDate

/** Longest description accepted (after trimming surrounding whitespace). */
const val MAX_DESCRIPTION_LENGTH = 200

/** Result of [validateExpenseForm]. */
sealed interface ExpenseFormValidation {
    /** All fields are valid; values are ready to build an `Expense`. */
    data class Valid(
        val amountPaise: Long,
        val category: ExpenseCategory,
        val description: String,
        val date: CalendarDate,
    ) : ExpenseFormValidation

    data class Invalid(val errors: ExpenseFormErrors) : ExpenseFormValidation
}

/**
 * Checks every field of [form] at once, so the user sees all problems together.
 *
 * Rules:
 * - Amount: see [RupeeAmountParser].
 * - Category: required.
 * - Description: optional; trimmed; at most [MAX_DESCRIPTION_LENGTH] characters.
 * - Date: today or earlier ([today] is the current date in the device's time zone).
 */
fun validateExpenseForm(form: ExpenseFormState, today: CalendarDate): ExpenseFormValidation {
    val amountResult = RupeeAmountParser.parse(form.amountText)
    val description = form.description.trim()

    val errors = ExpenseFormErrors(
        amount = (amountResult as? AmountParseResult.Invalid)?.error,
        categoryMissing = form.category == null,
        dateInFuture = form.date > today,
        descriptionTooLong = description.length > MAX_DESCRIPTION_LENGTH,
    )
    if (errors.hasAny) return ExpenseFormValidation.Invalid(errors)

    return ExpenseFormValidation.Valid(
        amountPaise = (amountResult as AmountParseResult.Valid).paise,
        category = requireNotNull(form.category),
        description = description,
        date = form.date,
    )
}
