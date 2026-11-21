package com.example.foodcheck

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class GetInformation(private val client: HttpClient) {

    suspend fun GetProduct(number: String): InfoProduct {
        val response = client.get(
            "https://world.openfoodfacts.org/api/v2/product/${number}"
        )
        return response.body<InfoProduct>()
    }
}


