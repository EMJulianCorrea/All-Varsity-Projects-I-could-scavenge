package com.example.palisadeinventoryapp.data.repository

import com.example.palisadeinventoryapp.data.dao.SaleDao
import com.example.palisadeinventoryapp.data.entity.Sale
import kotlinx.coroutines.flow.Flow

class SaleRepository(
    private val saleDao: SaleDao,
    private val productRepository: ProductRepository
) {

    val allSales: Flow<List<Sale>> = saleDao.getAllSales()

    /**
     * Records a sale and reduces product stock in a single transaction.
     * If it fails, then the sale is not inserted then nothing is written to the database at all.
     * Returns a true on success, false if stock was not enough.
     */
    suspend fun recordSale(sale: Sale): Boolean {
        val stockReduced = productRepository.reduceStock(sale.productId, sale.quantitySold)
        if (!stockReduced) return false
        saleDao.insert(sale)
        return true
    }

    suspend fun delete(sale: Sale) = saleDao.delete(sale)

    suspend fun getSaleById(id: Int): Sale? = saleDao.getSaleById(id)

    fun getSalesByProduct(productId: Int): Flow<List<Sale>> =
        saleDao.getSalesByProduct(productId)

    fun getSalesBetween(from: Long, to: Long): Flow<List<Sale>> =
        saleDao.getSalesBetween(from, to)

    suspend fun getTotalRevenue(from: Long, to: Long): Double =
        saleDao.getTotalRevenue(from, to) ?: 0.0
}
