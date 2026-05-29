package features.accounts.models

import com.mobibank.features.auth.models.UsersTable
import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object AccountsTable : Table("accounts_cards") {
    val id = uuid("id").autoGenerate()
    val userId = reference("user_id", UsersTable.id)
    val cardNumber = varchar("card_number", 16).uniqueIndex()
    val currency = varchar("currency", 3)
    val balance = decimal("balance", 15, 2).default(0.toBigDecimal())
    val type = varchar("type", 20)
    val status = varchar("status", 20).default("ACTIVE")

    override val primaryKey = PrimaryKey(id)
}