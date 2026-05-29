package features.loans.models

import com.mobibank.features.auth.models.UsersTable
import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object LoansTable : Table("loans") {

    val id = uuid("id").autoGenerate()
    val userId = reference("user_id", UsersTable.id)
    val amount = decimal("amount", 15, 2)
    val interestRate = decimal("interest_rate", 5, 2) // Наприклад, 5.0 (це 5%)
    val remainingAmount = decimal("remaining_amount", 15, 2) // Сума до повернення з відсотками
    val dueDate = varchar("due_date", 30)
    val status = varchar("status", 20).default("ACTIVE") // ACTIVE або PAID

    override val primaryKey = PrimaryKey(id)
}