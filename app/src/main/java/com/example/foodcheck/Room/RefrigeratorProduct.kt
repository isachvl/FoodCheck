package com.example.foodcheck.Room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "refrigerator_products",
    foreignKeys = [
        ForeignKey(
            entity = Refrigerator::class,
            parentColumns = ["id"],
            childColumns = ["refrigeratorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("refrigeratorId")]
)
data class RefrigeratorProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val refrigeratorId: Long,

    val barcode: String? = null,

    val productJson: String,

    val count: Int = 1
)