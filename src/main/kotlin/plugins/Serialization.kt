package ua.mobibank.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

fun Application.configSerialization(){
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true // Робить JSON читабельним (з відступами)
            isLenient = true
            ignoreUnknownKeys = true // Ігнорує зайві поля, якщо клієнт передасть щось зайве
        })
    }
}
