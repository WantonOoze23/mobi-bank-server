package ua.mobibank.plugins

import features.accounts.AccountService
import features.accounts.accountsRouting
import features.loans.LoanService
import features.loans.loansRouting
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ua.mobibank.features.accounts.authRouting
import ua.mobibank.features.auth.AuthService
import ua.mobibank.features.deposits.DepositService
import ua.mobibank.features.deposits.depositsRouting
import ua.mobibank.features.transactions.TransactionService
import ua.mobibank.features.transactions.transactionsRouting

fun Application.configureRouting() {
    val authService by inject<AuthService>()
    val accountService by inject<AccountService>()
    val loanService by inject<LoanService>()
    val depositService by inject<DepositService>()
    val transactionService by inject<TransactionService>()

    routing {
        get("/") {
            call.respondText("The server is working!")
        }

        authRouting(authService)
        accountsRouting(accountService)
        loansRouting(loanService)
        depositsRouting(depositService)
        transactionsRouting(transactionService)

    }
}