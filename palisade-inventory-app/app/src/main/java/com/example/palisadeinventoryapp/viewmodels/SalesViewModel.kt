package com.example.palisadeinventoryapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palisadeinventoryapp.data.entity.Sale
import com.example.palisadeinventoryapp.data.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalesViewModel(private val saleRepository: SaleRepository) : ViewModel() {

    val salesHistory: StateFlow<List<Sale>> = saleRepository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saleMessage = MutableStateFlow("")
    val saleMessage: StateFlow<String> = _saleMessage

    // Stock reduction and the sale record are written together inside SaleRepository.recordSale,
    // so if there isn't enough stock, nothing is written at all.
    fun recordSale(productId: Int, quantity: Int, unitPrice: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = saleRepository.recordSale(
                Sale(productId = productId, quantitySold = quantity, salePrice = unitPrice)
            )
            _saleMessage.value = if (success) "Sale recorded" else "Not enough stock for that sale"
            onResult(success)
        }
    }

    fun clearMessage() {
        _saleMessage.value = ""
    }

    companion object {
        fun Factory(saleRepository: SaleRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SalesViewModel(saleRepository) as T
                }
            }
    }
}