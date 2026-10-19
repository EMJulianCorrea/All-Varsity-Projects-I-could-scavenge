package com.example.palisadeinventoryapp.di

import android.content.Context
import com.example.palisadeinventoryapp.data.AppDatabase
import com.example.palisadeinventoryapp.data.repository.CategoryRepository
import com.example.palisadeinventoryapp.data.repository.ProductRepository
import com.example.palisadeinventoryapp.data.repository.ProductionRepository
import com.example.palisadeinventoryapp.data.repository.SaleRepository
import com.example.palisadeinventoryapp.data.repository.UserRepository

object DatabaseProvider {

    private lateinit var db: AppDatabase

    lateinit var userRepository: UserRepository
        private set
    lateinit var categoryRepository: CategoryRepository
        private set
    lateinit var productRepository: ProductRepository
        private set
    lateinit var saleRepository: SaleRepository
        private set
    lateinit var productionRepository: ProductionRepository
        private set

    fun init(context: Context) {
        if (::db.isInitialized) return

        db = AppDatabase.getDatabase(context)

        userRepository       = UserRepository(db.userDao())
        categoryRepository   = CategoryRepository(db.categoryDao())
        productRepository    = ProductRepository(db.productDao())
        saleRepository       = SaleRepository(db.saleDao(), productRepository)
        productionRepository = ProductionRepository(db, db.productionDao(), productRepository)
    }
}
