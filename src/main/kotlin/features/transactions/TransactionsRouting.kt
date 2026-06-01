package ua.mobibank.features.transactions

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ua.mobibank.features.transactions.model.TransferRequest

fun Route.transactionsRouting(transactionService: TransactionService) {
    authenticate("auth-jwt") {
        route("/api/v1/transactions") {

            // Виконати переказ
            post("/transfer") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val request = call.receive<TransferRequest>()

                    val result = transactionService.transfer(userId, request)

                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.OK, result.getOrNull()!!)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to result.exceptionOrNull()?.message))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            get("/{accountId}") {
                try {
                    val accountId = call.parameters["accountId"] ?: throw Exception("ID рахунку обов'язкове")
                    val typeFilter = call.request.queryParameters["type"]

                    val history = transactionService.getHistory(accountId, typeFilter)
                    call.respond(HttpStatusCode.OK, history)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }
    }
}