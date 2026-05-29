package features.loans

import database.DatabaseFactory.dbQuery
import features.accounts.models.AccountsTable
import features.loans.models.LoansTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class LoanRepository {

    suspend fun createLoanAndFundAccount(
        userId: UUID,
        accountId: UUID,
        amount: Double,
        interestRate: Double,
        totalToRepay: Double,
        dueDate: String
    ): ResultRow? {
        return dbQuery {
            val account = AccountsTable.selectAll().where {
                (AccountsTable.id eq accountId.toKotlinUuid()) and (AccountsTable.userId eq userId.toKotlinUuid())
            }.singleOrNull() ?: throw Exception("Картку не знайдено або вона вам не належить")

            val insertStatement = LoansTable.insert {
                it[LoansTable.userId] = userId.toKotlinUuid()
                it[LoansTable.amount] = amount.toBigDecimal()
                it[LoansTable.interestRate] = interestRate.toBigDecimal()
                it[LoansTable.remainingAmount] = totalToRepay.toBigDecimal()
                it[LoansTable.dueDate] = dueDate
            }

            val currentBalance = account[AccountsTable.balance]
            AccountsTable.update({ AccountsTable.id eq accountId.toKotlinUuid() }) {
                it[balance] = currentBalance + amount.toBigDecimal()
            }

            insertStatement.resultedValues?.singleOrNull()
        }
    }

    suspend fun getUserLoans(userId: UUID): List<ResultRow> {
        return dbQuery {
            LoansTable.selectAll().where { LoansTable.userId eq userId.toKotlinUuid() }.toList()
        }
    }
}