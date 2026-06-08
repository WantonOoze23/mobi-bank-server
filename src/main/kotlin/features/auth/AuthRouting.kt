package ua.mobibank.features.accounts

import com.mobibank.features.auth.models.ChangePasswordRequest
import com.mobibank.features.auth.models.LoginRequest
import com.mobibank.features.auth.models.RegisterRequest
import com.mobibank.features.auth.models.UpdateProfileRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
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
                    call.respond(HttpStatusCode.Created, result.getOrNull()!!)
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
                    call.respond(HttpStatusCode.OK, result.getOrNull()!!)
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to result.exceptionOrNull()!!.message!!))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message!!)
            }
        }
    }
    authenticate("auth-jwt") {
        route("/api/v1/profile") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
                val profile = authService.getProfile(userId)

                if (profile != null) {
                    call.respond(HttpStatusCode.OK, profile)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Користувача не знайдено"))
                }
            }

            put("/update") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val request = call.receive<UpdateProfileRequest>()

                    val result = authService.updateProfile(userId, request)

                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to result.getOrNull()!!))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to result.exceptionOrNull()?.message))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Невірний формат даних"))
                }
            }

            put("/password") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString()
                    val request = call.receive<ChangePasswordRequest>()

                    val result = authService.changePassword(userId, request)

                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to result.getOrNull()!!))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to result.exceptionOrNull()?.message))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Невірний формат даних"))
                }
            }
        }
    }
}