package features.loans.models

import com.mobibank.features.auth.models.UsersTable
import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object LoansTable : Table("loans") {

    val id = uuid("id").autoGenerate()
    val userId = reference("user_id", UsersTable.id)
    val amount = decimal("amount", 15, 2)
    val currency = varchar("currency", 10).default("UAH")
    val interestRate = decimal("interest_rate", 5, 2)
    val remainingAmount = decimal("remaining_amount", 15, 2)
    val dueDate = varchar("due_date", 30)
    val status = varchar("status", 20).default("ACTIVE")

    override val primaryKey = PrimaryKey(id)
}