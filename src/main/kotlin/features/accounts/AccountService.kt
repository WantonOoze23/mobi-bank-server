package features.accounts

import features.accounts.models.AccountResponse
import features.accounts.models.AccountsTable
import features.accounts.models.OpenCardRequest
import org.jetbrains.exposed.v1.core.ResultRow
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class AccountService(private val repository: AccountRepository) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun openCard(userIdString: String, request: OpenCardRequest): Result<AccountResponse> {
        val allowedCurrencies = listOf("USD", "EUR", "UAH")
        if (request.currency !in allowedCurrencies) {
            return Result.failure(Exception("Непідтримувана валюта. Доступні: USD, EUR, UAH"))
        }

        val userId = UUID.fromString(userIdString)

        val cardNumber = "4149" + (1..12).map { (0..9).random() }.joinToString("")

        val row = repository.createAccount(userId.toKotlinUuid(), cardNumber, request.currency, request.type)
            ?: return Result.failure(Exception("Помилка при створенні картки"))

        return Result.success(mapRowToResponse(row))
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getMyAccounts(userIdString: String): List<AccountResponse> {
        val userId = UUID.fromString(userIdString)
        val rows = repository.getUserAccounts(userId.toKotlinUuid())
        return rows.map { mapRowToResponse(it) }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun mapRowToResponse(row: ResultRow): AccountResponse {
        return AccountResponse(
            id = row[AccountsTable.id].toJavaUuid().toString(),
            cardNumber = row[AccountsTable.cardNumber],
            currency = row[AccountsTable.currency],
            balance = row[AccountsTable.balance].toDouble(),
            type = row[AccountsTable.type],
            status = row[AccountsTable.status]
        )
    }
}