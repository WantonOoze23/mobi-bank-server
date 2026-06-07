package ua.mobibank.features.deposits

import org.jetbrains.exposed.v1.core.ResultRow
import ua.mobibank.features.deposits.models.CloseDepositRequest
import ua.mobibank.features.deposits.models.DepositResponse
import ua.mobibank.features.deposits.models.DepositsTable
import ua.mobibank.features.deposits.models.OpenDepositRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

class DepositService(private val repository: DepositRepository) {

    suspend fun openDeposit(userIdString: String, request: OpenDepositRequest): Result<DepositResponse> {
        if (request.amount <= 0) return Result.failure(Exception("Сума депозиту має бути більшою за нуль"))
        if (request.durationMonths <= 0) return Result.failure(Exception("Некоректний термін депозиту"))

        val userId = UUID.fromString(userIdString)
        val accountId = UUID.fromString(request.accountId)

        val interestRate = when {
            request.durationMonths >= 12 -> 14.0
            request.durationMonths >= 6 -> 11.0
            else -> 8.0
        }

        val startDate = LocalDate.now()
        val endDate = startDate.plusMonths(request.durationMonths.toLong())
        val formatter = DateTimeFormatter.ISO_DATE

        return try {
            val row = repository.openDeposit(
                userId = userId,
                accountId = accountId,
                amount = request.amount,
                interestRate = interestRate,
                startDate = startDate.format(formatter),
                endDate = endDate.format(formatter)
            ) ?: return Result.failure(Exception("Помилка при відкритті депозиту"))

            Result.success(mapRowToResponse(row))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyDeposits(userIdString: String): List<DepositResponse> {
        val userId = UUID.fromString(userIdString)
        val rows = repository.getUserDeposits(userId)
        return rows.map { mapRowToResponse(it) }
    }

    suspend fun closeDeposit(userIdString: String, request: CloseDepositRequest): Result<String> {
        val userId = UUID.fromString(userIdString)
        val depositId = UUID.fromString(request.depositId)
        val accountId = UUID.fromString(request.accountId)

        return try {
            val success = repository.closeDeposit(userId, depositId, accountId)
            if (success) {
                Result.success("Депозит успішно закрито! Кошти зараховано на картку.")
            } else {
                Result.failure(Exception("Не вдалося закрити депозит"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun mapRowToResponse(row: ResultRow): DepositResponse {
        return DepositResponse(
            id = row[DepositsTable.id].toJavaUuid().toString(),
            amount = row[DepositsTable.amount].toDouble(),
            interestRate = row[DepositsTable.interestRate].toDouble(),
            currency = row[DepositsTable.currency],
            startDate = row[DepositsTable.startDate],
            endDate = row[DepositsTable.endDate],
            status = row[DepositsTable.status]
        )
    }
}