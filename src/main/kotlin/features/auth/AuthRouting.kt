package ua.mobibank.features.accounts

import com.mobibank.features.auth.models.LoginRequest
import com.mobibank.features.auth.models.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import ua.mobibank.features.auth.AuthService

fun Route.authRouting(authService: AuthService) {
    route("/api/v1/auth") {
        post("/register") {
            try{
                val request = call.receive<RegisterRequest>()

                val result = authService.register(request)

                if(result.isSuccess){
                    call.respond(HttpStatusCode.Created, mapOf("token" to result.getOrNull()!!))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to result.exceptionOrNull()!!.message!!))
                }

            }catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest, e.message!!)
            }
        }
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                val result = authService.login(request)

                if(result.isSuccess){
                    call.respond(HttpStatusCode.OK, mapOf("token" to result.getOrNull()!!))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to result.exceptionOrNull()!!.message!!))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message!!)
            }
        }
    }
}