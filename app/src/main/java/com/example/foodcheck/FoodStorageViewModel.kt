package com.example.foodcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodcheck.Room.DaoRefrigerator
import com.example.foodcheck.Room.Refrigerator
import com.example.foodcheck.Room.RefrigeratorProduct

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.foodcheck.internet.Product
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
class FoodStorageViewModel(
    private val dao: DaoRefrigerator
) : ViewModel() {



    fun deleteRefrigerator(id: Long) {
        viewModelScope.launch {
            dao.deleteRefrigerator(id)
        }
    }
    fun addScannedProduct(
        refrigeratorId: Long,
        product: Product,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            dao.insertProduct(
                RefrigeratorProduct(
                    refrigeratorId = refrigeratorId,
                    productJson = Json.encodeToString(product)
                )
            )
            onSaved()
        }
    }
    fun getProducts(refrigeratorId: Long): Flow<List<RefrigeratorProduct>> {
        return dao.getProducts(refrigeratorId)
    }
    fun addProduct(refrigeratorId: Long, name: String) {
        viewModelScope.launch {
            dao.insertProduct(
                RefrigeratorProduct(
                    refrigeratorId = refrigeratorId,
                    productJson = Json.encodeToString(mapOf("product_name" to name))
                )
            )
        }
    }
    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            dao.deleteProduct(id)
        }
    }
    val refrigerators: Flow<List<Refrigerator>> =
        dao.getAllRefrigerators()

    fun addRefrigerator(name: String) {
        viewModelScope.launch {
            dao.insertRefrigerator(
                Refrigerator(name = name)
            )
        }
    }
}