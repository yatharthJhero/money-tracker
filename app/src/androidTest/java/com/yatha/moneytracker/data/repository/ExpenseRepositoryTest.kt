package com.yatha.moneytracker.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yatha.moneytracker.data.local.MoneyTrackerDatabase
import com.yatha.moneytracker.domain.model.Expense
import com.yatha.moneytracker.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end tests of the data layer: domain [Expense] -> mapper -> Room -> SQLite and back,
 * using an in-memory database.
 */
@RunWith(AndroidJUnit4::class)
class ExpenseRepositoryTest {

    private lateinit var database: MoneyTrackerDatabase
    private lateinit var repository: ExpenseRepository

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MoneyTrackerDatabase::class.java).build()
        repository = ExpenseRepository(database.expenseDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun newExpense(
        amount: Long = 12_550L,
        currencyCode: String = "INR",
        category: ExpenseCategory = ExpenseCategory.FOOD,
        description: String = "Lunch",
        transactionDate: Long = JAN_2,
        createdAt: Long = JAN_3,
    ) = Expense(
        id = ExpenseRepository.NEW_EXPENSE_ID,
        amount = amount,
        currencyCode = currencyCode,
        category = category,
        description = description,
        transactionDate = transactionDate,
        createdAt = createdAt,
    )

    @Test
    fun addExpense_returnsGeneratedId_andPreservesEveryField() = runBlocking<Unit> {
        val expense = newExpense(
            amount = 9_999_999L,
            currencyCode = "USD",
            category = ExpenseCategory.HEALTH,
            description = "Pharmacy",
            transactionDate = JAN_1,
            createdAt = JAN_3,
        )

        val id = repository.addExpense(expense)

        assertTrue("generated id should be positive, was $id", id > 0)
        assertEquals(expense.copy(id = id), repository.getExpense(id))
    }

    @Test
    fun addExpense_preservesBoundaryAmountsExactly() = runBlocking<Unit> {
        listOf(1L, Long.MAX_VALUE).forEach { amount ->
            val id = repository.addExpense(newExpense(amount = amount))

            assertEquals(amount, repository.getExpense(id)?.amount)
        }
    }

    @Test
    fun addExpense_withExistingId_isRejected_andNothingIsSaved() = runBlocking<Unit> {
        val failure = runCatching { repository.addExpense(newExpense().copy(id = 7L)) }.exceptionOrNull()

        assertTrue("expected IllegalArgumentException, got $failure", failure is IllegalArgumentException)
        assertEquals(emptyList<Expense>(), repository.observeExpenses().first())
    }

    @Test
    fun everyCategory_survivesARoundTrip() = runBlocking<Unit> {
        ExpenseCategory.entries.forEach { repository.addExpense(newExpense(category = it)) }

        val stored = repository.observeExpenses().first().map { it.category }

        assertEquals(ExpenseCategory.entries.size, stored.size)
        assertEquals(ExpenseCategory.entries.toSet(), stored.toSet())
    }

    @Test
    fun storedRow_usesCategoryNameAndIntegerAmount() = runBlocking<Unit> {
        val id = repository.addExpense(newExpense(amount = 12_550L, category = ExpenseCategory.HEALTH))

        database.query(
            "SELECT category, typeof(category), amount_minor, typeof(amount_minor), currency_code " +
                "FROM expenses WHERE id = ?",
            arrayOf<Any?>(id),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("HEALTH", cursor.getString(0))
            assertEquals("text", cursor.getString(1))
            assertEquals(12_550L, cursor.getLong(2))
            assertEquals("integer", cursor.getString(3))
            assertEquals("INR", cursor.getString(4))
        }
    }

    @Test
    fun updateExpense_changesStoredFields_andKeepsCreatedAt() = runBlocking<Unit> {
        val id = repository.addExpense(newExpense(transactionDate = JAN_2, createdAt = JAN_3))
        val edited = repository.getExpense(id)!!.copy(
            amount = 25_000L,
            currencyCode = "EUR",
            category = ExpenseCategory.TRANSPORT,
            description = "Train ticket",
            transactionDate = JAN_1,
        )

        assertTrue(repository.updateExpense(edited))

        val stored = repository.getExpense(id)
        assertEquals(edited, stored)
        assertEquals(JAN_1, stored?.transactionDate)
        assertEquals(JAN_3, stored?.createdAt)
    }

    @Test
    fun updateExpense_missingExpense_returnsFalse_andInsertsNothing() = runBlocking<Unit> {
        assertFalse(repository.updateExpense(newExpense().copy(id = 42L)))
        assertEquals(emptyList<Expense>(), repository.observeExpenses().first())
    }

    @Test
    fun deleteExpense_removesOnlyThatExpense() = runBlocking<Unit> {
        val deletedId = repository.addExpense(newExpense())
        val keptId = repository.addExpense(newExpense())

        assertTrue(repository.deleteExpense(deletedId))

        assertNull(repository.getExpense(deletedId))
        assertNotNull(repository.getExpense(keptId))
    }

    @Test
    fun deleteExpense_missingExpense_returnsFalse() = runBlocking<Unit> {
        assertFalse(repository.deleteExpense(12_345L))
    }

    @Test
    fun observeExpenses_returnsNewestTransactionFirst_tiesNewestInsertFirst() = runBlocking<Unit> {
        val oldest = repository.addExpense(newExpense(transactionDate = JAN_1))
        val tieInsertedFirst = repository.addExpense(newExpense(transactionDate = JAN_2))
        val newest = repository.addExpense(newExpense(transactionDate = JAN_3))
        val tieInsertedSecond = repository.addExpense(newExpense(transactionDate = JAN_2))

        val ids = repository.observeExpenses().first().map { it.id }

        assertEquals(listOf(newest, tieInsertedSecond, tieInsertedFirst, oldest), ids)
    }

    private companion object {
        // Midnight UTC on 1, 2 and 3 January 2026, in epoch milliseconds.
        const val JAN_1 = 1_767_225_600_000L
        const val JAN_2 = 1_767_312_000_000L
        const val JAN_3 = 1_767_398_400_000L
    }
}
