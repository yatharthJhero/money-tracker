package com.yatha.moneytracker.testing

import com.yatha.moneytracker.data.local.ExpenseDao
import com.yatha.moneytracker.data.local.ExpenseEntity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [ExpenseDao] for JVM tests. It mimics the real DAO's id generation and ordering,
 * and lets tests slow down or fail writes.
 */
class FakeExpenseDao : ExpenseDao {

    private val table = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    private var nextId = 1L

    /** Every entity passed to [insert], exactly as received. */
    val insertArguments = mutableListOf<ExpenseEntity>()

    val insertCallCount: Int get() = insertArguments.size

    val rowCount: Int get() = table.value.size

    /** When set, [insert] suspends until this completes (simulates a slow write). */
    var insertGate: CompletableDeferred<Unit>? = null

    /** When set, the next [insert] throws this instead of saving (one-shot). */
    var nextInsertFailure: Exception? = null

    /** When set, collecting [observeAll] fails with this exception. */
    var observeFailure: Exception? = null

    fun seed(vararg rows: ExpenseEntity) {
        rows.forEach { row -> table.update { it + row } }
        nextId = (table.value.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override suspend fun insert(expense: ExpenseEntity): Long {
        insertArguments += expense
        insertGate?.await()
        nextInsertFailure?.let {
            nextInsertFailure = null
            throw it
        }
        val row = expense.copy(id = nextId++)
        table.update { it + row }
        return row.id
    }

    override suspend fun update(expense: ExpenseEntity): Int {
        if (table.value.none { it.id == expense.id }) return 0
        table.update { rows -> rows.map { if (it.id == expense.id) expense else it } }
        return 1
    }

    override suspend fun deleteById(id: Long): Int {
        if (table.value.none { it.id == id }) return 0
        table.update { rows -> rows.filterNot { it.id == id } }
        return 1
    }

    override suspend fun getById(id: Long): ExpenseEntity? = table.value.firstOrNull { it.id == id }

    override fun observeAll(): Flow<List<ExpenseEntity>> = flow {
        observeFailure?.let { throw it }
        emitAll(
            table.map { rows ->
                rows.sortedWith(compareByDescending<ExpenseEntity> { it.transactionDate }.thenByDescending { it.id })
            },
        )
    }
}
