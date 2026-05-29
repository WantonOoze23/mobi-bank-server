package com.mobibank.features.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val phone: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String
)