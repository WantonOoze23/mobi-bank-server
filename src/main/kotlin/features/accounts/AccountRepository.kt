package features.accounts

import database.DatabaseFactory.dbQuery
import features.accounts.models.AccountsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AccountRepository {

    suspend fun createAccount(userId: Uuid, cardNumber: String, currency: String, type: String): ResultRow? {
        return dbQuery {
            val insertStatement = AccountsTable.insert {
                it[AccountsTable.userId] = userId
                it[AccountsTable.cardNumber] = cardNumber
                it[AccountsTable.currency] = currency
                it[AccountsTable.type] = type
            }
            insertStatement.resultedValues?.singleOrNull()
        }
    }

    suspend fun getUserAccounts(userId: Uuid): List<ResultRow> {
        return dbQuery {
            AccountsTable.selectAll().where { AccountsTable.userId eq userId }.toList()
        }
    }
}