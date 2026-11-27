package com.yatha.moneytracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the `expenses` table.
 *
 * All one-shot operations are `suspend` functions; Room runs them off the main thread.
 * [observeAll] returns a [Flow] that re-emits whenever the table changes.
 */
@Dao
interface ExpenseDao {

    /**
     * Inserts a new row and returns the id SQLite generated for it.
     *
     * [expense] must have `id == 0` so the database assigns the key. Inserting an id that
     * already exists fails with a constraint error instead of overwriting the row.
     */
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    /**
     * Replaces the row whose id matches [expense]. Returns the number of rows updated:
     * `1` on success, `0` if no row has that id.
     */
    @Update
    suspend fun update(expense: ExpenseEntity): Int

    /** Deletes the row with [id]. Returns `1` if a row was deleted, `0` if none existed. */
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /** Returns the row with [id], or `null` if there is none. */
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    /**
     * Observes every expense, newest transaction first.
     *
     * Rows with the same `transaction_date` are ordered by `id` descending (most recently
     * inserted first). Because `id` is unique, the order is fully deterministic.
     */
    @Query("SELECT * FROM expenses ORDER BY transaction_date DESC, id DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>
}
