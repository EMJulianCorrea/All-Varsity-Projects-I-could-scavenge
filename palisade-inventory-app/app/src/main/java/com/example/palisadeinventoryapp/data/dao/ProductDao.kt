package com.example.palisadeinventoryapp.data.dao

import androidx.room.*
import com.example.palisadeinventoryapp.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    // ACTIVE products only
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    // ALL products including archived
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsIncludingArchived(): Flow<List<Product>>

    // Archived only
    @Query("SELECT * FROM products WHERE isActive = 0 ORDER BY name ASC")
    fun getArchivedProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isActive = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Int): Flow<List<Product>>

    // This preserves sales history while hiding the product from active lists
    @Query("UPDATE products SET isActive = 0 WHERE id = :productId")
    suspend fun archiveProduct(productId: Int)

    // Sets the product active again
    @Query("UPDATE products SET isActive = 1 WHERE id = :productId")
    suspend fun restoreProduct(productId: Int)

    // Called automatically
    @Query("UPDATE products SET quantity = quantity - :amount WHERE id = :productId AND quantity >= :amount AND isActive = 1")
    suspend fun reduceStock(productId: Int, amount: Int): Int  // Returns 0 if insufficient stock or product archived

    // Called automatically when production is recorded.
    // Returns the number of rows updated, "0" means the product does not exist or is archived
    @Query("UPDATE products SET quantity = quantity + :amount WHERE id = :productId AND isActive = 1")
    suspend fun increaseStock(productId: Int, amount: Int): Int

    @Query("SELECT * FROM products WHERE quantity <= :threshold AND isActive = 1 ORDER BY quantity ASC")
    fun getLowStockProducts(threshold: Int = 5): Flow<List<Product>>
}
