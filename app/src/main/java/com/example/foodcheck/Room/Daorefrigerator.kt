package com.example.foodcheck.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoRefrigerator {

    @Query("SELECT * FROM refrigerators")
    fun getAllRefrigerators(): Flow<List<Refrigerator>>

    @Insert
    suspend fun insertRefrigerator(
        refrigerator: Refrigerator
    ): Long
    @Query("DELETE FROM refrigerators WHERE id = :refrigeratorId")
    suspend fun deleteRefrigerator(refrigeratorId: Long)
    @Query("""
        SELECT * FROM refrigerator_products
        WHERE refrigeratorId = :refrigeratorId
    """)
    fun getProducts(
        refrigeratorId: Long
    ): Flow<List<RefrigeratorProduct>>

    @Insert
    suspend fun insertProduct(
        product: RefrigeratorProduct
    ): Long

    @Query("""
        DELETE FROM refrigerator_products
        WHERE id = :productId
    """)
    suspend fun deleteProduct(productId: Long)
}