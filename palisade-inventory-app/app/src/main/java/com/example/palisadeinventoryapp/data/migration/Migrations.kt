package com.example.palisadeinventoryapp.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration v1 to v2 this adds the isActive column to products table (existing products default to active)
// https://developer.android.com/training/data-storage/room/migrating-db-versions
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE products ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `production` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `productId` INTEGER NOT NULL,
                `quantityProduced` INTEGER NOT NULL,
                `date` INTEGER NOT NULL,
                FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_production_productId` ON `production` (`productId`)"
        )
    }
}
