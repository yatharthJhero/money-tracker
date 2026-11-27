package com.yatha.moneytracker.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs against a real SQLite database held in memory, so nothing is written to the
 * device's storage and every test starts from an empty table.
 */
@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var database: MoneyTrackerDatabase
    private lateinit var dao: ExpenseDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MoneyTrackerDatabase::class.java).build()
        dao = database.expenseDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun entity(
        id: Long = 0L,
        amountMinor: Long = 12_550L,
        transactionDate: Long = JAN_2,
    ) = ExpenseEntity(
        id = id,
        amountMinor = amountMinor,
        currencyCode = "INR",
        category = "FOOD",
        description = "Lunch",
        transactionDate = transactionDate,
        createdAt = JAN_3,
    )

    @Test
    fun insert_generatesPositiveIncreasingIds() = runBlocking<Unit> {
        val first = dao.insert(entity())
        val second = dao.insert(entity())

        assertTrue("first id should be positive, was $first", first > 0)
        assertTrue("ids should increase: $first then $second", second > first)
    }

    @Test
    fun insert_thenGetById_returnsTheStoredRow() = runBlocking<Unit> {
        val original = entity(amountMinor = 4_999L)

        val id = dao.insert(original)

        assertEquals(original.copy(id = id), dao.getById(id))
    }

    @Test
    fun ids_areNotReusedAfterDelete() = runBlocking<Unit> {
        val deletedId = dao.insert(entity())
        dao.deleteById(deletedId)

        val nextId = dao.insert(entity())

        assertTrue("id $deletedId must not be reused, got $nextId", nextId > deletedId)
    }

    @Test
    fun insert_withExistingId_failsInsteadOfOverwriting() = runBlocking<Unit> {
        val id = dao.insert(entity(amountMinor = 100L))

        val failure = runCatching { dao.insert(entity(id = id, amountMinor = 999L)) }.exceptionOrNull()

        assertTrue("expected SQLiteConstraintException, got $failure", failure is SQLiteConstraintException)
        assertEquals(100L, dao.getById(id)?.amountMinor)
    }

    @Test
    fun getById_missingRow_returnsNull() = runBlocking<Unit> {
        assertNull(dao.getById(12_345L))
    }

    @Test
    fun largestAmount_isStoredExactly() = runBlocking<Unit> {
        val id = dao.insert(entity(amountMinor = Long.MAX_VALUE))

        assertEquals(Long.MAX_VALUE, dao.getById(id)?.amountMinor)
    }

    @Test
    fun update_existingRow_replacesValues_andReportsOneRow() = runBlocking<Unit> {
        val id = dao.insert(entity(amountMinor = 100L))
        val edited = entity(id = id, amountMinor = 250L, transactionDate = JAN_1)

        val updatedRows = dao.update(edited)

        assertEquals(1, updatedRows)
        assertEquals(edited, dao.getById(id))
    }

    @Test
    fun update_missingRow_reportsZeroRows_andInsertsNothing() = runBlocking<Unit> {
        val updatedRows = dao.update(entity(id = 77L))

        assertEquals(0, updatedRows)
        assertEquals(emptyList<ExpenseEntity>(), dao.observeAll().first())
    }

    @Test
    fun deleteById_removesOnlyThatRow() = runBlocking<Unit> {
        val deletedId = dao.insert(entity())
        val keptId = dao.insert(entity())

        val deletedRows = dao.deleteById(deletedId)

        assertEquals(1, deletedRows)
        assertNull(dao.getById(deletedId))
        assertEquals(listOf(keptId), dao.observeAll().first().map { it.id })
    }

    @Test
    fun deleteById_missingRow_reportsZeroRows() = runBlocking<Unit> {
        assertEquals(0, dao.deleteById(12_345L))
    }

    @Test
    fun observeAll_emptyTable_emitsEmptyList() = runBlocking<Unit> {
        assertEquals(emptyList<ExpenseEntity>(), dao.observeAll().first())
    }

    @Test
    fun observeAll_ordersByTransactionDateDescending_thenIdDescending() = runBlocking<Unit> {
        // Inserted in an order that differs from the expected output.
        val oldest = dao.insert(entity(transactionDate = JAN_1))
        val newest = dao.insert(entity(transactionDate = JAN_3))
        val tieInsertedFirst = dao.insert(entity(transactionDate = JAN_2))
        val tieInsertedSecond = dao.insert(entity(transactionDate = JAN_2))

        val ids = dao.observeAll().first().map { it.id }

        assertEquals(listOf(newest, tieInsertedSecond, tieInsertedFirst, oldest), ids)
    }

    @Test
    fun observeAll_emitsAgainWhenTheTableChanges() = runBlocking<Unit> {
        val firstNonEmptyEmission = async(Dispatchers.Default) {
            withTimeout(5_000L) { dao.observeAll().first { it.isNotEmpty() } }
        }

        val id = dao.insert(entity())

        assertEquals(listOf(id), firstNonEmptyEmission.await().map { it.id })
    }

    private companion object {
        // Midnight UTC on 1, 2 and 3 January 2026, in epoch milliseconds.
        const val JAN_1 = 1_767_225_600_000L
        const val JAN_2 = 1_767_312_000_000L
        const val JAN_3 = 1_767_398_400_000L
    }
}
