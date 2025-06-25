package com.codewithfk.expensetracker.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * The Room database class for the Expense Tracker application.
 * Defines the database configuration, entities, and DAOs.
 *
 * @param entities An array of classes annotated with @Entity that are in the database.
 * @param version The version number of the database. When the schema changes, this version must be incremented.
 * @param exportSchema Specifies whether to export the schema into a folder. Set to false for production.
 */
@Database(entities = [ExpenseEntity::class], version = 2, exportSchema = false)
@Singleton // Marks this class as a singleton in the Hilt dependency graph
abstract class ExpenseDatabase : RoomDatabase() {

    /**
     * Abstract method to get the Data Access Object (DAO) for expenses.
     * Room will generate the implementation for this method.
     *
     * @return An instance of [ExpenseDao].
     */
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "expense_database" // Name of the database file

        // The @Volatile annotation ensures that INSTANCE is always up-to-date across all threads.
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        /**
         * Returns the singleton instance of the [ExpenseDatabase].
         * If the instance is null, it creates a new one in a synchronized block
         * to prevent multiple threads from creating multiple instances.
         *
         * @param context The application context, provided by Dagger Hilt.
         * @return The singleton instance of [ExpenseDatabase].
         */
        fun getInstance(@ApplicationContext context: Context): ExpenseDatabase {
            // If INSTANCE is not null, then return it,
            // otherwise, create a new instance in a synchronized block.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to prevent memory leaks
                    ExpenseDatabase::class.java,
                    DATABASE_NAME
                )
                    // Add database migrations here. MIGRATION_1_2 is defined below.
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance // Assign the created instance to INSTANCE
                instance // Return the instance
            }
        }
    }
}

/**
 * Database migration from version 1 to version 2.
 * This migration handles a schema change for the `expense_table`.
 * It involves creating a new table, copying data, dropping the old table, and renaming the new table.
 *
 * This pattern is useful for structural changes (like adding, removing, or changing column types)
 * while preserving existing data.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create a new table with the desired schema (e.g., if columns were added/modified)
        // In this specific migration, it looks like the schema for `expense_table_new`
        // is identical to the implicit schema of `expense_table`, which might indicate
        // a preparation for future changes or a fix of a previous schema.
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expense_table_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Step 2: Copy the data from the old table to the new table
        database.execSQL(
            """
            INSERT INTO expense_table_new (id, title, amount, date, type)
            SELECT id, title, amount, date, type FROM expense_table
            """.trimIndent()
        )

        // Step 3: Drop the old table
        database.execSQL("DROP TABLE expense_table")

        // Step 4: Rename the new table to the original table name
        database.execSQL("ALTER TABLE expense_table_new RENAME TO expense_table")
    }
}