package com.example.foodcheck.Room

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Refrigerator::class,
        RefrigeratorProduct::class
    ],
    version = 1
)
abstract class RefrigeratorBD : RoomDatabase() {

    abstract fun refrigeratorDao(): DaoRefrigerator
}
fun refrigeratorDb(app: Application): RefrigeratorBD{
    return Room.databaseBuilder(app,
        RefrigeratorBD::class.java,
        "refrigerator.db").build()
}