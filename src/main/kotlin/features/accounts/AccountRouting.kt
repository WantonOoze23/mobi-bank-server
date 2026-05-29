package features.accounts

import features.accounts.models.OpenCardRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.accountsRouting(accountService: AccountService) {

    authenticate("auth-jwt") {
        route("/api/v1/accounts") {
            post("/open") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()

                    val request = call.receive<OpenCardRequest>()
                    val result = accountService.openCard(userId, request)

                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.Created, result.getOrThrow())
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

                val accounts = accountService.getMyAccounts(userId)
                call.respond(HttpStatusCode.OK, accounts)
            }
        }
    }
}