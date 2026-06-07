package features.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String? = null,
    val message: String? = null
)

@Serializable
data class UserProfileResponse(
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val phone: String,
    val email: String
)

