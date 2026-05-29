package features.loans.models

import kotlinx.serialization.Serializable

@Serializable
data class ApplyLoanRequest(
    val accountId: String, // ID картки, куди зарахувати гроші
    val amount: Double
)

@Serializable
data class LoanResponse(
    val id: String,
    val amount: Double,
    val remainingAmount: Double,
    val dueDate: String,
    val status: String
)