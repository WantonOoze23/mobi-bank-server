package features.loans

import features.loans.models.ApplyLoanRequest
import features.loans.models.LoanResponse
import features.loans.models.LoansTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

class LoanService(private val repository: LoanRepository) {

    suspend fun applyForLoan(userIdString: String, request: ApplyLoanRequest): Result<LoanResponse> {
        if (request.amount <= 0) {
            return Result.failure(Exception("Сума кредиту має бути більшою за нуль"))
        }

        val userId = UUID.fromString(userIdString)
        val accountId = UUID.fromString(request.accountId)

        val interestRate = 5.0
        val totalToRepay = request.amount + (request.amount * interestRate / 100)

        val dueDate = LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_DATE)

        return try {
            val row = repository.createLoanAndFundAccount(
                userId, accountId, request.amount, interestRate, totalToRepay, dueDate
            ) ?: return Result.failure(Exception("Помилка при оформленні кредиту"))

            Result.success(mapRowToResponse(row))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyLoans(userIdString: String): List<LoanResponse> {
        val userId = UUID.fromString(userIdString)
        val rows = repository.getUserLoans(userId)
        return rows.map { mapRowToResponse(it) }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun mapRowToResponse(row: ResultRow): LoanResponse {
        return LoanResponse(
            id = row[LoansTable.id].toJavaUuid().toString(),
            amount = row[LoansTable.amount].toDouble(),
            remainingAmount = row[LoansTable.remainingAmount].toDouble(),
            dueDate = row[LoansTable.dueDate],
            status = row[LoansTable.status]
        )
    }
}