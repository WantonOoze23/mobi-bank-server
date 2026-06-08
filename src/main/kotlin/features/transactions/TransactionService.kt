package ua.mobibank.features.transactions

import features.transactions.TransactionRepository
import ua.mobibank.features.transactions.model.TopUpRequest
import ua.mobibank.features.transactions.model.TransactionResponse
import ua.mobibank.features.transactions.model.TransactionsTable
import ua.mobibank.features.transactions.model.TransferRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class TransactionService(private val repository: TransactionRepository) {

    suspend fun transfer(userIdString: String, request: TransferRequest): Result<TransactionResponse> {
        if (request.amount <= 0) return Result.failure(Exception("Сума має бути більшою за нуль"))

        val userId = UUID.fromString(userIdString)
        val senderAccountId = UUID.fromString(request.senderAccountId)
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

        return try {
            val row = repository.transferMoney(
                userId = userId,
                senderAccountId = senderAccountId,
                receiverCardNumber = request.receiverCardNumber,
                amount = request.amount,
                timestamp = timestamp
            ) ?: return Result.failure(Exception("Помилка під час переказу"))

            val response = TransactionResponse(
                id = row[TransactionsTable.id].toJavaUuid().toString(),
                amount = row[TransactionsTable.amount].toDouble(),
                currency = row[TransactionsTable.currency],
                type = "EXPENSE",
                timestamp = row[TransactionsTable.timestamp],
                counterpartyCard = request.receiverCardNumber
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(accountIdString: String, filterType: String?): List<TransactionResponse> {
        val accountId = UUID.fromString(accountIdString)
        val rows = repository.getAccountTransactions(accountId)

        val allTransactions = rows.map { row ->
            val senderIdString = row[TransactionsTable.senderAccountId].toString()
            val receiverIdString = row[TransactionsTable.receiverAccountId].toString()

            val isExpense = senderIdString == accountIdString
            val type = if (isExpense) "EXPENSE" else "INCOME"

            TransactionResponse(
                id = row[TransactionsTable.id].toString(),
                amount = row[TransactionsTable.amount].toDouble(),
                currency = row[TransactionsTable.currency],
                type = type,
                timestamp = row[TransactionsTable.timestamp],
                counterpartyCard = null
            )
        }

        return when (filterType?.uppercase()) {
            "EXPENSE" -> allTransactions.filter { it.type == "EXPENSE" }
            "INCOME" -> allTransactions.filter { it.type == "INCOME" }
            else -> allTransactions
        }
    }

    suspend fun topUpCard(request: TopUpRequest): Result<String> {
        if (request.amount <= 0) return Result.failure(Exception("Сума поповнення має бути більшою за нуль"))

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

        return try {
            repository.topUpAccount(request.cardNumber, request.amount, timestamp)
            Result.success("Картку успішно поповнено на ${request.amount}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}