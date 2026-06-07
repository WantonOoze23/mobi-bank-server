package ua.mobibank.features.deposits

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
import ua.mobibank.features.deposits.models.CloseDepositRequest
import ua.mobibank.features.deposits.models.OpenDepositRequest

fun Route.depositsRouting(depositService: DepositService) {
    authenticate("auth-jwt") {
        route("/api/v1/deposits") {

            post("/open") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val request = call.receive<OpenDepositRequest>()

                    val result = depositService.openDeposit(userId, request)

                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.Created, result.getOrNull()!!)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to result.exceptionOrNull()?.message))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            post("/close") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val request = call.receive<CloseDepositRequest>()

                    val result = depositService.closeDeposit(userId, request)

                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to result.getOrNull()!!))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to result.exceptionOrNull()?.message))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Невірний формат даних"))
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
                val deposits = depositService.getMyDeposits(userId)

                call.respond(HttpStatusCode.OK, deposits)
            }
        }
    }
}