package ua.mobibank.features.deposits.models

import kotlinx.serialization.Serializable

@Serializable
data class OpenDepositRequest(
    val accountId: String, // ID картки, з якої знімаємо кошти
    val amount: Double,
    val durationMonths: Int // Термін у місяцях
)

@Serializable
data class CloseDepositRequest(
    val depositId: String,
    val accountId: String
)

@Serializable
data class DepositResponse(
    val id: String,
    val amount: Double,
    val interestRate: Double,
    val currency: String,
    val startDate: String,
    val endDate: String,
    val status: String
)