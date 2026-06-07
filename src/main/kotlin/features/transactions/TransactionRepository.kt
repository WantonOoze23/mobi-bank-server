package features.transactions

import database.DatabaseFactory.dbQuery
import features.accounts.models.AccountsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import ua.mobibank.features.transactions.model.TransactionsTable
import java.math.RoundingMode
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class TransactionRepository {

    private val exchangeRates = mapOf(
        "UAH" to 1.0,
        "USD" to 40.0, // 1 USD = 40 UAH
        "EUR" to 43.0  // 1 EUR = 43 UAH
    )

    private fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount

        val rateFrom = exchangeRates[fromCurrency.uppercase()] ?: throw Exception("Невідома валюта відправника: $fromCurrency")
        val rateTo = exchangeRates[toCurrency.uppercase()] ?: throw Exception("Невідома валюта отримувача: $toCurrency")

        val amountInUah = amount * rateFrom

        val finalAmount = amountInUah / rateTo

        return finalAmount.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    suspend fun transferMoney(
        userId: UUID,
        senderAccountId: UUID,
        receiverCardNumber: String,
        amount: Double,
        timestamp: String
    ): ResultRow? {
        return dbQuery {
            val senderAccount = AccountsTable.selectAll().where {
                (AccountsTable.id eq senderAccountId.toKotlinUuid()) and (AccountsTable.userId eq userId.toKotlinUuid())
            }.singleOrNull() ?: throw Exception("Картку відправника не знайдено")

            val senderBalance = senderAccount[AccountsTable.balance]
            val senderCurrency = senderAccount[AccountsTable.currency] // Валюта відправника

            if (senderBalance < amount.toBigDecimal()) {
                throw Exception("Недостатньо коштів на балансі")
            }

            val receiverAccount = AccountsTable.selectAll().where {
                AccountsTable.cardNumber eq receiverCardNumber
            }.singleOrNull() ?: throw Exception("Картку отримувача не знайдено")

            val receiverAccountId = receiverAccount[AccountsTable.id]
            val receiverCurrency = receiverAccount[AccountsTable.currency]

            if (senderAccountId.toKotlinUuid() == receiverAccountId) {
                throw Exception("Неможливо переказати кошти на ту ж саму картку")
            }

            val convertedAmount = convertCurrency(amount, senderCurrency, receiverCurrency)

            AccountsTable.update({ AccountsTable.id eq senderAccountId.toKotlinUuid() }) {
                it[balance] = senderBalance - amount.toBigDecimal()
            }

            val receiverBalance = receiverAccount[AccountsTable.balance]
            AccountsTable.update({ AccountsTable.id eq receiverAccountId }) {
                it[balance] = receiverBalance + convertedAmount.toBigDecimal()
            }

            val insertStatement = TransactionsTable.insert {
                it[TransactionsTable.senderAccountId] = senderAccountId.toKotlinUuid()
                it[TransactionsTable.receiverAccountId] = receiverAccountId
                it[TransactionsTable.currency] = senderCurrency
                it[TransactionsTable.amount] = amount.toBigDecimal()
                it[TransactionsTable.timestamp] = timestamp
            }

            insertStatement.resultedValues?.singleOrNull()
        }
    }

    suspend fun getAccountTransactions(accountId: UUID): List<ResultRow> {
        return dbQuery {
            TransactionsTable.selectAll().where {
                (TransactionsTable.senderAccountId eq accountId.toKotlinUuid()) or
                        (TransactionsTable.receiverAccountId eq accountId.toKotlinUuid())
            }.orderBy(TransactionsTable.timestamp to SortOrder.DESC).toList()
        }
    }
}