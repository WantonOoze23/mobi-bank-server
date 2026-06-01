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
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class DepositRepository {

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

    suspend fun getUserDeposits(userId: UUID): List<ResultRow> {
        return dbQuery {
            DepositsTable.selectAll().where { DepositsTable.userId eq userId.toKotlinUuid() }.toList()
        }
    }
}