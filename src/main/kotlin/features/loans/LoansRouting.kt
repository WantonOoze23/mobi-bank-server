package features.loans

import features.loans.models.ApplyLoanRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.loansRouting(loanService: LoanService) {
    authenticate("auth-jwt") {
        route("/api/v1/loans") {

            post("/apply") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val request = call.receive<ApplyLoanRequest>()

                    val result = loanService.applyForLoan(userId, request)

                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.Created, result.getOrNull()!!)
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
                val loans = loanService.getMyLoans(userId)
                call.respond(HttpStatusCode.OK, loans)
            }
        }
    }
}