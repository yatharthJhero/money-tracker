package com.yatha.moneytracker.data.repository

import com.yatha.moneytracker.data.local.ExpenseDao
import com.yatha.moneytracker.data.local.toDomain
import com.yatha.moneytracker.data.local.toEntity
import com.yatha.moneytracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single entry point for reading and writing expenses.
 *
 * Callers (future ViewModels) only see domain [Expense] objects; Room entities stay
 * inside the data layer. All data is stored on the device; nothing here touches the network.
 *
 * Constructed manually, e.g.
 * `ExpenseRepository(MoneyTrackerDatabase.getInstance(context).expenseDao())`.
 */
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    /** Observes all expenses, newest transaction first (ties: most recently inserted first). */
    fun observeExpenses(): Flow<List<Expense>> =
        expenseDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    /** Returns the expense with [id], or `null` if it does not exist. */
    suspend fun getExpense(id: Long): Expense? = expenseDao.getById(id)?.toDomain()

    /**
     * Saves a new expense and returns the id the database generated for it.
     *
     * @throws IllegalArgumentException if [expense] already has an id. New expenses must use
     *   `id = 0`; use [updateExpense] to change an existing one.
     */
    suspend fun addExpense(expense: Expense): Long {
        require(expense.id == NEW_EXPENSE_ID) {
            "New expenses must have id $NEW_EXPENSE_ID so the database can generate one, " +
                "but was ${expense.id}. Use updateExpense() for existing expenses."
        }
        return expenseDao.insert(expense.toEntity())
    }

    /** Overwrites the stored expense with the same id. Returns `false` if no such expense exists. */
    suspend fun updateExpense(expense: Expense): Boolean = expenseDao.update(expense.toEntity()) > 0

    /** Deletes the expense with [id]. Returns `false` if no such expense exists. */
    suspend fun deleteExpense(id: Long): Boolean = expenseDao.deleteById(id) > 0

    companion object {
        /** The id a not-yet-saved [Expense] must carry. */
        const val NEW_EXPENSE_ID = 0L
    }
}
