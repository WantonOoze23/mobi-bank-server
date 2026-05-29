package ua.mobibank

import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import ua.mobibank.plugins.configSerialization
import ua.mobibank.plugins.configureDatabases
import ua.mobibank.plugins.configureRouting

fun main() {
    embeddedServer(CIO, port = 8080, host = "127.0.0.0"){
        configureRouting()
        configSerialization()
        configureDatabases()
    }.start(wait = true)
}