package features.accounts.models

import kotlinx.serialization.Serializable

@Serializable
data class OpenCardRequest(
    val currency: String,
    val type: String = "DEBIT"
)

@Serializable
data class AccountResponse(
    val id: String,
    val cardNumber: String,
    val currency: String,
    val balance: Double,
    val type: String,
    val status: String
)