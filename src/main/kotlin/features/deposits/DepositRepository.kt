package ua.mobibank.features.deposits

import database.DatabaseFactory.dbQuery
import features.accounts.models.AccountsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import ua.mobibank.features.deposits.models.DepositsTable
import ua.mobibank.СurrancyExchangeRate.exchangeRates
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class DepositRepository {

    private fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount
        val rateFrom = exchangeRates[fromCurrency.uppercase()] ?: throw Exception("Невідома валюта: $fromCurrency")
        val rateTo = exchangeRates[toCurrency.uppercase()] ?: throw Exception("Невідома валюта: $toCurrency")

        val amountInUah = amount * rateFrom
        val finalAmount = amountInUah / rateTo
        return finalAmount.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    suspend fun openDeposit(
        userId: UUID,
        accountId: UUID,
        amount: Double,
        interestRate: Double,
        startDate: String,
        endDate: String
    ): ResultRow? {
        return dbQuery {
            val account = AccountsTable.selectAll().where {
                (AccountsTable.id eq accountId.toKotlinUuid()) and (AccountsTable.userId eq userId.toKotlinUuid())
            }.singleOrNull() ?: throw Exception("Картку не знайдено")

            val currentBalance = account[AccountsTable.balance]
            val currency = account[AccountsTable.currency]

            if (currentBalance < amount.toBigDecimal()) {
                throw Exception("Недостатньо коштів на балансі для відкриття депозиту")
            }

            AccountsTable.update({ AccountsTable.id eq accountId.toKotlinUuid() }) {
                it[balance] = currentBalance - amount.toBigDecimal()
            }

            val insertStatement = DepositsTable.insert {
                it[DepositsTable.userId] = userId.toKotlinUuid()
                it[DepositsTable.amount] = amount.toBigDecimal()
                it[DepositsTable.interestRate] = interestRate.toBigDecimal()
                it[DepositsTable.currency] = currency
                it[DepositsTable.startDate] = startDate
                it[DepositsTable.endDate] = endDate
            }

            insertStatement.resultedValues?.singleOrNull()
        }
    }

    suspend fun closeDeposit(userId: UUID, depositId: UUID, accountId: UUID): Boolean {
        return dbQuery {
            val deposit = DepositsTable.selectAll().where {
                (DepositsTable.id eq depositId.toKotlinUuid()) and (DepositsTable.userId eq userId.toKotlinUuid())
            }.singleOrNull() ?: throw Exception("Депозит не знайдено")

            val status = deposit[DepositsTable.status]
            if (status == "CLOSED") throw Exception("Цей депозит вже закрито")

            val amount = deposit[DepositsTable.amount].toDouble()
            val interestRate = deposit[DepositsTable.interestRate].toDouble()
            val depositCurrency = deposit[DepositsTable.currency]

            val startDate = LocalDate.parse(deposit[DepositsTable.startDate])
            val endDate = LocalDate.parse(deposit[DepositsTable.endDate])
            val currentDate = LocalDate.now()

            val profit: Double
            if (currentDate.isBefore(endDate)) {
                val daysElapsed = ChronoUnit.DAYS.between(startDate, currentDate).coerceAtLeast(0)
                val totalDays = ChronoUnit.DAYS.between(startDate, endDate)

                val dailyRate = (interestRate / 100.0) / totalDays.toDouble()
                val earnedInterest = amount * dailyRate * daysElapsed.toDouble()

                profit = earnedInterest * 0.5
            } else {
                profit = amount * (interestRate / 100.0)
            }

            val totalToReturnInDepositCurrency = amount + profit

            val account = AccountsTable.selectAll().where {
                (AccountsTable.id eq accountId.toKotlinUuid()) and (AccountsTable.userId eq userId.toKotlinUuid())
            }.singleOrNull() ?: throw Exception("Картку для зарахування не знайдено або вона вам не належить")

            val accountCurrency = account[AccountsTable.currency]
            val currentBalance = account[AccountsTable.balance].toDouble()

            val amountToCredit = convertCurrency(totalToReturnInDepositCurrency, depositCurrency, accountCurrency)

            AccountsTable.update({ AccountsTable.id eq accountId.toKotlinUuid() }) {
                it[balance] = (currentBalance + amountToCredit).toBigDecimal()
            }

            val updatedRows = DepositsTable.update({ DepositsTable.id eq depositId.toKotlinUuid() }) {
                it[DepositsTable.status] = "CLOSED"
            }

            updatedRows > 0
        }
    }

    suspend fun getUserDeposits(userId: UUID): List<ResultRow> {
        return dbQuery {
            DepositsTable.selectAll().where { DepositsTable.userId eq userId.toKotlinUuid() }.toList()
        }
    }
}