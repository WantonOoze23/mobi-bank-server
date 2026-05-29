package ua.mobibank.features.auth

import com.mobibank.features.auth.AuthRepository
import com.mobibank.features.auth.models.LoginRequest
import com.mobibank.features.auth.models.RegisterRequest
import com.mobibank.features.auth.models.UsersTable
import org.mindrot.jbcrypt.BCrypt
import ua.mobibank.plugins.JwtConfig
import kotlin.uuid.ExperimentalUuidApi

class AuthService(private val repository: AuthRepository) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun register(request: RegisterRequest) : Result<String>{
        if(repository.getUserByLogin(request.email) != null) throw Exception("User with this email or phone number already exists")

        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())

        val createUser = repository.createUser(request, passwordHash)

        return if (createUser) {
            val userRow = repository.getUserByLogin(request.phone)
            val userId = userRow!![UsersTable.id].toString()

            val token = JwtConfig.generateToken(userId)
            Result.success(token)
        } else {
            Result.failure(Exception("Error creating user"))
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun login(request: LoginRequest) : Result<String>{
        val userRow = repository.getUserByLogin(request.login) ?: return Result.failure(Exception("User not found"))

        val passwordHash = userRow[UsersTable.passwordHash]

        val isPasswordValid = BCrypt.checkpw(request.password, passwordHash)

        return if(isPasswordValid){
            val userId = userRow[UsersTable.id].toString()
            val token = JwtConfig.generateToken(userId)
            Result.success(token)
        } else{
            Result.failure(Exception("Invalid password"))
        }
    }
}