package features.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val message: String
)
