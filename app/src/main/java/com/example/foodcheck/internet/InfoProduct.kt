package com.example.foodcheck.internet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Весь ответ сервера
@Serializable
data class InfoProduct(
    val code: String? = null,
    val status: Int = 0,
    val product: Product? = null
)

// Сам продукт
@Serializable
data class Product(

    @SerialName("product_name")
    val name: String? = null,

    @SerialName("product_name_ru")
    val nameRu: String? = null,

    @SerialName("brands")
    val brand: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("quantity")
    val quantity: String? = null,

    @SerialName("ingredients_text")
    val ingredients: String? = null,

    @SerialName("ingredients_text_ru")
    val ingredientsRu: String? = null,

    @SerialName("allergens_tags")
    val allergens: List<String>? = null,

    @SerialName("nutriscore_grade")
    val nutriScore: String? = null,

    val nutriments: Nutriments? = null
)

// Пищевая ценность на 100 г
@Serializable
data class Nutriments(

    @SerialName("energy-kcal_100g")
    val calories: Double? = null,

    @SerialName("proteins_100g")
    val proteins: Double? = null,

    @SerialName("fat_100g")
    val fats: Double? = null,

    @SerialName("carbohydrates_100g")
    val carbohydrates: Double? = null,

    @SerialName("sugars_100g")
    val sugars: Double? = null,

    @SerialName("salt_100g")
    val salt: Double? = null
)
