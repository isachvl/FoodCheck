package com.example.foodcheck.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refrigerators")
data class Refrigerator(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String
)