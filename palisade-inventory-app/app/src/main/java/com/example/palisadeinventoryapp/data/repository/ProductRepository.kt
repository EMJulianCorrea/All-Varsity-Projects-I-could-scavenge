package com.example.palisadeinventoryapp.data.repository

import com.example.palisadeinventoryapp.data.dao.ProductDao
import com.example.palisadeinventoryapp.data.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    // Active products only
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    // All products including archived
    val allProductsIncludingArchived: Flow<List<Product>> = productDao.getAllProductsIncludingArchived()

    // Archived products only
    val archivedProducts: Flow<List<Product>> = productDao.getArchivedProducts()

    suspend fun insert(product: Product): Long = productDao.insert(product)

    suspend fun update(product: Product) = productDao.update(product)

    suspend fun archive(productId: Int): Result<Unit> = runCatching {
        productDao.archiveProduct(productId)
    }

    // Restores an archived product
    suspend fun restore(productId: Int): Result<Unit> = runCatching {
        productDao.restoreProduct(productId)
    }

    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)

    fun getProductsByCategory(categoryId: Int): Flow<List<Product>> =
        productDao.getProductsByCategory(categoryId)

    // Returns true if stock was reduced successfully.
    // Returns false if there wasn't enough stock or the product is archived.
    suspend fun reduceStock(productId: Int, amount: Int): Boolean =
        productDao.reduceStock(productId, amount) > 0


    // Used by ProductionRepository.
    suspend fun increaseStock(productId: Int, amount: Int): Boolean =
        productDao.increaseStock(productId, amount) > 0

    fun getLowStockProducts(threshold: Int = 5): Flow<List<Product>> =
        productDao.getLowStockProducts(threshold)
}
