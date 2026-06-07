package ua.mobibank.features.transactions.model

import features.accounts.models.AccountsTable
import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object TransactionsTable : Table("transactions") {
    val id = uuid("id").autoGenerate()
    val senderAccountId = reference("sender_account_id", AccountsTable.id).nullable()
    val receiverAccountId = reference("receiver_account_id", AccountsTable.id).nullable()
    val amount = decimal("amount", 15, 2)
    val currency = varchar("currency", 10).default("UAH")
    val timestamp = varchar("timestamp", 30)

    override val primaryKey = PrimaryKey(id)
}