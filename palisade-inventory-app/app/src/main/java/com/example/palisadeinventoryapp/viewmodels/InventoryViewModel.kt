package com.example.palisadeinventoryapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palisadeinventoryapp.data.entity.Category
import com.example.palisadeinventoryapp.data.entity.Product
import com.example.palisadeinventoryapp.data.repository.CategoryRepository
import com.example.palisadeinventoryapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = productRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProductsIncludingArchived: StateFlow<List<Product>> = productRepository.allProductsIncludingArchived
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedProducts: StateFlow<List<Product>> = productRepository.archivedProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Feedback for the user — success or failure — shown as a Snackbar by whichever
    // screen is on top when it's set. Survives navigation since it lives on the ViewModel,
    // which is shared between InventoryScreen and AddEditProductScreen.
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() {
        _message.value = null
    }

    val filteredProducts: StateFlow<List<Product>> =
        combine(products, _searchQuery, categories) { productList, query, categoryList ->
            if (query.isBlank()) {
                productList
            } else {
                productList.filter { product ->
                    val categoryName = categoryList.find { it.id == product.categoryId }?.name ?: ""
                    product.name.contains(query, ignoreCase = true) ||
                            categoryName.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun categoryName(categoryId: Int?): String =
        categories.value.find { it.id == categoryId }?.name ?: "Uncategorised"

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addProduct(name: String, categoryId: Int?, quantity: Int, price: Double) {
        viewModelScope.launch {
            try {
                productRepository.insert(
                    Product(name = name, categoryId = categoryId, quantity = quantity, price = price)
                )
                _message.value = "\"$name\" added to inventory"
            } catch (e: Exception) {
                _message.value = "Couldn't add \"$name\": ${e.message ?: "unknown error"}"
            }
        }
    }

    fun updateProduct(product: Product, name: String, categoryId: Int?, quantity: Int, price: Double) {
        viewModelScope.launch {
            try {
                productRepository.update(
                    product.copy(name = name, categoryId = categoryId, quantity = quantity, price = price)
                )
                _message.value = "\"$name\" updated"
            } catch (e: Exception) {
                _message.value = "Couldn't update \"$name\": ${e.message ?: "unknown error"}"
            }
        }
    }

    // Archives the product (soft delete) — sales history stays intact.
    // Checks the Result this time, so a failed archive is no longer silent.
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.archive(product.id)
                .onSuccess {
                    _message.value = "\"${product.name}\" removed from inventory"
                }
                .onFailure { error ->
                    _message.value = "Couldn't remove \"${product.name}\": ${error.message ?: "unknown error"}"
                }
        }
    }

    fun restoreProduct(product: Product) {
        viewModelScope.launch {
            productRepository.restore(product.id)
                .onSuccess {
                    _message.value = "\"${product.name}\" restored to inventory"
                }
                .onFailure { error ->
                    _message.value = "Couldn't restore \"${product.name}\": ${error.message ?: "unknown error"}"
                }
        }
    }

    fun addCategory(name: String, description: String = "") {
        viewModelScope.launch {
            try {
                categoryRepository.insert(Category(name = name, description = description))
                _message.value = "Category \"$name\" added"
            } catch (e: Exception) {
                _message.value = "Couldn't add category \"$name\": ${e.message ?: "unknown error"}"
            }
        }
    }

    companion object {
        const val LOW_STOCK_THRESHOLD = 5

        fun Factory(
            productRepository: ProductRepository,
            categoryRepository: CategoryRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InventoryViewModel(productRepository, categoryRepository) as T
                }
            }
    }
}