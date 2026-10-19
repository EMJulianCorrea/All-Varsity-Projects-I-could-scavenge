package com.example.palisadeinventoryapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.palisadeinventoryapp.data.entity.Production
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionDao {

    @Insert
    suspend fun insert(production: Production): Long

    @Query("SELECT * FROM production ORDER BY date DESC")
    fun getAllProduction(): Flow<List<Production>>

    @Query("SELECT * FROM production WHERE productId = :productId ORDER BY date DESC")
    fun getProductionByProduct(productId: Int): Flow<List<Production>>

    @Query("SELECT * FROM production WHERE date BETWEEN :startOfDay AND :endOfDay ORDER BY date DESC")
    fun getProductionBetween(startOfDay: Long, endOfDay: Long): Flow<List<Production>>
}
