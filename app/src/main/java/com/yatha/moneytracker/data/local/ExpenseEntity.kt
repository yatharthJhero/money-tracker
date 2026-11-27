package com.yatha.moneytracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room representation of a row in the `expenses` table.
 *
 * This class belongs to the data layer only. The rest of the app works with
 * [com.yatha.moneytracker.domain.model.Expense]; convert with the functions in
 * `ExpenseMapper.kt`.
 *
 * Column names are explicit so that renaming a Kotlin property never silently
 * changes the database schema.
 *
 * @property id Database-generated primary key. `0` means "not inserted yet";
 *   Room then lets SQLite assign the next id.
 * @property amountMinor Amount in minor currency units (paise for INR). Never a floating-point value.
 * @property currencyCode ISO 4217 currency code, stored explicitly for every row.
 * @property category Name of the [com.yatha.moneytracker.domain.model.ExpenseCategory]
 *   constant (for example `"FOOD"`), never its ordinal.
 * @property description Optional free-text note; empty string when absent.
 * @property transactionDate When the expense happened, in epoch milliseconds.
 * @property createdAt When the expense was recorded in the app, in epoch milliseconds.
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "transaction_date")
    val transactionDate: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
