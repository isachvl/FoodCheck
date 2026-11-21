package com.example.foodcheck

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ViewModelMainScreen(private val getProduct: GetInformation) : ViewModel() {

    private val _state = MutableStateFlow<ProductState>(ProductState.Idle)

    val state = _state.asStateFlow()



    sealed interface ProductState {
        data object Idle : ProductState
        data object Loading : ProductState
        data class Success(val product: Product) : ProductState
        data class Error(val message: String) : ProductState
    }

    fun onBarcodeScanned(code: String) {
        viewModelScope.launch {
            _state.value = ProductState.Loading

            try {
                val response = getProduct.GetProduct(code)

                val product = response.product

                _state.value = if (response.status == 1 && product != null) {
                    ProductState.Success(product)
                } else {
                    ProductState.Error("Продукт не найден")
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e

                Log.e("FoodCheck", "Ошибка запроса продукта", e)

                _state.value = ProductState.Error(
                    e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }




}