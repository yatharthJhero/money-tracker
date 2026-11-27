package com.yatha.moneytracker.data.local

import com.yatha.moneytracker.domain.model.Expense
import com.yatha.moneytracker.domain.model.ExpenseCategory

/**
 * Converts a domain [Expense] into its Room row.
 *
 * The category is stored by its enum *name*, so reordering [ExpenseCategory]
 * constants never changes the meaning of stored rows.
 */
fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amountMinor = amount,
    currencyCode = currencyCode,
    category = category.name,
    description = description,
    transactionDate = transactionDate,
    createdAt = createdAt,
)

/**
 * Converts a Room row back into a domain [Expense].
 *
 * @throws IllegalStateException if the stored category no longer matches any
 *   [ExpenseCategory] constant. That only happens if a constant is renamed or removed
 *   without a database migration that rewrites stored values. Failing loudly is preferred
 *   over silently re-categorising the user's spending.
 * @throws IllegalArgumentException if the stored amount is not positive (see [Expense]).
 */
fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amount = amountMinor,
    currencyCode = currencyCode,
    category = categoryFromStoredName(category, rowId = id),
    description = description,
    transactionDate = transactionDate,
    createdAt = createdAt,
)

private fun categoryFromStoredName(name: String, rowId: Long): ExpenseCategory =
    ExpenseCategory.entries.firstOrNull { it.name == name }
        ?: throw IllegalStateException(
            "Unknown expense category '$name' in expenses row id=$rowId. " +
                "Renaming or removing an ExpenseCategory requires a database migration.",
        )
