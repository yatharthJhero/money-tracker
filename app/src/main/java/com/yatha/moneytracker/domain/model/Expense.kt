package com.yatha.moneytracker.domain.model

/**
 * A single expense recorded by the user.
 *
 * Money is stored as an integer count of minor currency units (paise for INR)
 * so that no floating-point arithmetic is ever applied to amounts.
 *
 * @property id Unique identifier of the expense.
 * @property amount Amount in minor currency units, e.g. 12_550 = ₹125.50. Must be greater than zero.
 * @property currencyCode ISO 4217 currency code. Defaults to "INR".
 * @property category Category the expense belongs to.
 * @property description Optional free-text note. Defaults to an empty string.
 * @property transactionDate When the expense happened, in epoch milliseconds.
 * @property createdAt When the expense was recorded in the app, in epoch milliseconds.
 */
data class Expense(
    val id: Long,
    val amount: Long,
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
    val category: ExpenseCategory,
    val description: String = "",
    val transactionDate: Long,
    val createdAt: Long,
) {
    init {
        require(amount > 0) { "Expense amount must be greater than zero, but was $amount" }
    }

    companion object {
        const val DEFAULT_CURRENCY_CODE = "INR"
    }
}
