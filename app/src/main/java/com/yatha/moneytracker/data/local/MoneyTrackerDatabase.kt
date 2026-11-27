package com.yatha.moneytracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app's single on-device SQLite database.
 *
 * Schema changes:
 * - Increase [version] and add a migration (an `AutoMigration` or a manual `Migration`).
 * - The exported schema for every version is written to `app/schemas/` and must be committed.
 * - Destructive migration (wiping user data) is intentionally NOT enabled. A missing migration
 *   makes Room throw at startup instead of deleting the user's financial records.
 */
@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MoneyTrackerDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        /** File name of the database inside the app's private storage. */
        const val DATABASE_NAME = "money_tracker.db"

        @Volatile
        private var instance: MoneyTrackerDatabase? = null

        /**
         * Returns the process-wide database instance, creating it on first use.
         * Opening a Room database is expensive, so the app should hold only one.
         */
        fun getInstance(context: Context): MoneyTrackerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoneyTrackerDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
    }
}
