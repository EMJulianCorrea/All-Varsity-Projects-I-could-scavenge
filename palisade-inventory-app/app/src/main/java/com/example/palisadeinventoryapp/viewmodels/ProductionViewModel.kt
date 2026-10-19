package com.example.palisadeinventoryapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palisadeinventoryapp.data.entity.Production
import com.example.palisadeinventoryapp.data.repository.ProductionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductionViewModel(private val productionRepository: ProductionRepository) : ViewModel() {

    val productionHistory: StateFlow<List<Production>> = productionRepository.allProduction
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayProduction: StateFlow<List<Production>> = productionRepository.getTodayProduction()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() {
        _message.value = null
    }

    // recordProduction inserts the entry AND increases stock atomically inside a DB
    // transaction (see ProductionRepository), so there's nothing else to do here on success.
    fun recordProduction(productId: Int, productName: String, quantity: Int) {
        viewModelScope.launch {
            productionRepository.recordProduction(productId, quantity)
                .onSuccess {
                    _message.value = "Recorded $quantity units of \"$productName\""
                }
                .onFailure { error ->
                    _message.value = "Couldn't record production: ${error.message ?: "unknown error"}"
                }
        }
    }

    companion object {
        fun Factory(productionRepository: ProductionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProductionViewModel(productionRepository) as T
                }
            }
    }
}