package features.loans.models

import kotlinx.serialization.Serializable

@Serializable
data class ApplyLoanRequest(
    val accountId: String,
    val amount: Double,
    val termInDays: Int
)

@Serializable
data class RepayLoanRequest(
    val loanId: String,
    val accountId: String
)

@Serializable
data class LoanResponse(
    val id: String,
    val amount: Double,
    val remainingAmount: Double,
    val currency: String, 
    val dueDate: String,
    val status: String
)