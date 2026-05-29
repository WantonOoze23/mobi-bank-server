package ua.mobibank.plugins

import features.accounts.AccountService
import features.accounts.accountsRouting
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.mobibank.features.accounts.authRouting
import ua.mobibank.features.auth.AuthService

fun Application.configureRouting() {
    val authService by inject<AuthService>()
    val accountService by inject<AccountService>()
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        authRouting(authService)
        accountsRouting(accountService)
    }
}