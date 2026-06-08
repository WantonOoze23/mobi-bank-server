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
    val login: String,
    val password: String
)

@Serializable
data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val email: String,
    val currentPassword: String
)

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)