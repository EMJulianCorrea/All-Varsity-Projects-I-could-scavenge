package com.example.palisadeinventoryapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.palisadeinventoryapp.data.dao.CategoryDao
import com.example.palisadeinventoryapp.data.dao.ProductDao
import com.example.palisadeinventoryapp.data.dao.ProductionDao
import com.example.palisadeinventoryapp.data.dao.SaleDao
import com.example.palisadeinventoryapp.data.dao.UserDao
import com.example.palisadeinventoryapp.data.entity.Category
import com.example.palisadeinventoryapp.data.entity.Product
import com.example.palisadeinventoryapp.data.entity.Production
import com.example.palisadeinventoryapp.data.entity.Sale
import com.example.palisadeinventoryapp.data.entity.User
import com.example.palisadeinventoryapp.data.migration.MIGRATION_1_2
import com.example.palisadeinventoryapp.data.migration.MIGRATION_2_3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Category::class, Product::class, Sale::class, Production::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun productionDao(): ProductionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "palisade_inventory.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed a default admin account on first run
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.userDao()?.insert(
                                    User(
                                        username = "admin",
                                        password = hashPassword("admin123"),
                                        role = "admin"
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Made it fun instead of orivate incase it needs to be accessed
        fun hashPassword(password: String): String {
            val digest = java.security.MessageDigest.getInstance("SHA-256")  // https://developer.android.com/reference/java/security/MessageDigest
            val hashBytes = digest.digest(password.toByteArray())
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }
}
