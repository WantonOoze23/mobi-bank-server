package ua.mobibank

import com.mobibank.plugins.configureDI
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import ua.mobibank.plugins.*

fun main() {
    embeddedServer(CIO, port = 8081, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureDI()

    configureDatabases()
    configureSecurity()
    configureRouting()
    configSerialization()

}