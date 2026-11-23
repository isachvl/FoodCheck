package com.example.foodcheck.koin

import com.example.foodcheck.internet.GetInformation
import com.example.foodcheck.ViewModelMainScreen
import com.example.foodcheck.Room.refrigeratorDb
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModules = module {

    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }

            expectSuccess = true
        }
    }
    single{ GetInformation(get()) }

    viewModelOf(::ViewModelMainScreen)
    singleOf(::refrigeratorDb)



}