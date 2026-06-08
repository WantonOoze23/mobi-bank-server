package ua.mobibank.features.transactions.model

import kotlinx.serialization.Serializable

@Serializable
data class TransferRequest(
    val senderAccountId: String,
    val receiverCardNumber: String, // Номер картки отримувача
    val amount: Double
)

@Serializable
data class TransactionResponse(
    val id: String,
    val amount: Double,
    val currency: String,
    val type: String,
    val timestamp: String,
    val counterpartyCard: String?
)

@Serializable
data class TopUpRequest(
    val cardNumber: String,
    val amount: Double
)