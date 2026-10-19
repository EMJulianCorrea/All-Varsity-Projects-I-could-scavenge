package com.example.palisadeinventoryapp.data.repository

import androidx.room.withTransaction
import com.example.palisadeinventoryapp.data.AppDatabase
import com.example.palisadeinventoryapp.data.dao.ProductionDao
import com.example.palisadeinventoryapp.data.entity.Production
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ProductionRepository(
    private val db: AppDatabase,
    private val productionDao: ProductionDao,
    private val productRepository: ProductRepository
) {

    val allProduction: Flow<List<Production>> = productionDao.getAllProduction()

    fun getProductionByProduct(productId: Int): Flow<List<Production>> =
        productionDao.getProductionByProduct(productId)

    fun getTodayProduction(): Flow<List<Production>> {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
        return productionDao.getProductionBetween(startOfDay, endOfDay)
    }

    suspend fun recordProduction(productId: Int, quantityProduced: Int): Result<Unit> = runCatching {
        require(quantityProduced > 0) { "Production quantity must be greater than zero" }
        db.withTransaction {
            productionDao.insert(Production(productId = productId, quantityProduced = quantityProduced))
            val stockIncreased = productRepository.increaseStock(productId, quantityProduced)
            check(stockIncreased) { "Product is not found or archived. Cannot record production" }
        }
    }
}
