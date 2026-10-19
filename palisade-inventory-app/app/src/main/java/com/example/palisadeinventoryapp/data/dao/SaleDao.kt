package com.example.palisadeinventoryapp.data.dao

import androidx.room.*
import com.example.palisadeinventoryapp.data.entity.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sale: Sale): Long

    @Delete
    suspend fun delete(sale: Sale)

    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Int): Sale?

    @Query("SELECT * FROM sales WHERE productId = :productId ORDER BY date DESC")
    fun getSalesByProduct(productId: Int): Flow<List<Sale>>

    // Sales within a date range — useful for daily/weekly/monthly reports
    @Query("SELECT * FROM sales WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getSalesBetween(from: Long, to: Long): Flow<List<Sale>>

    @Query("SELECT SUM(quantitySold * salePrice) FROM sales WHERE date BETWEEN :from AND :to")
    suspend fun getTotalRevenue(from: Long, to: Long): Double?
}
