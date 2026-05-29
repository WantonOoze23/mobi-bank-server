package ua.mobibank.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun Application.configSerialization(){
    install(ContentNegotiation) {
        json()
    }
}
