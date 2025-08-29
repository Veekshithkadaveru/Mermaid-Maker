package com.example.mermaidmaker.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for MermaidDatabase
 */
object DatabaseMigrations {

    /**
     * Migration from version 1 to 2
     * Example: Add a new column to diagrams table
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example migration - add tags column to diagrams
            // database.execSQL("ALTER TABLE diagrams ADD COLUMN tags TEXT")
            
            // For now, this is a placeholder migration that doesn't change schema
            // This allows the migration framework to be tested without breaking existing data
        }
    }

    /**
     * Migration from version 2 to 3
     * Example: Add an index for better performance
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add index on diagram type for better query performance
            // database.execSQL("CREATE INDEX index_diagrams_type ON diagrams(diagramType)")
        }
    }

    /**
     * Migration from version 3 to 4
     * Example: Add user preferences table
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add user preferences table
            // database.execSQL("""
            //     CREATE TABLE user_preferences (
            //         id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            //         key TEXT NOT NULL UNIQUE,
            //         value TEXT NOT NULL,
            //         created_at INTEGER NOT NULL,
            //         updated_at INTEGER NOT NULL
            //     )
            // """)
        }
    }

    /**
     * Get all available migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4
        )
    }
}