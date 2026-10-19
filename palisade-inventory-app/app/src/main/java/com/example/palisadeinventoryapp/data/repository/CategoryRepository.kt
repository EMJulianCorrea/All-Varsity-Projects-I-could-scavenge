package com.example.palisadeinventoryapp.data.repository

import com.example.palisadeinventoryapp.data.dao.CategoryDao
import com.example.palisadeinventoryapp.data.entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insert(category: Category): Long = categoryDao.insert(category)

    suspend fun update(category: Category) = categoryDao.update(category)

    suspend fun delete(category: Category) = categoryDao.delete(category)

    suspend fun getCategoryById(id: Int): Category? = categoryDao.getCategoryById(id)
}
