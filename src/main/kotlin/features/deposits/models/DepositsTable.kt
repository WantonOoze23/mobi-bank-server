package ua.mobibank.features.deposits.models

import com.mobibank.features.auth.models.UsersTable
import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object DepositsTable : Table("deposits") {
    val id = uuid("id").autoGenerate()
    val userId = reference("user_id", UsersTable.id)
    val amount = decimal("amount", 15, 2)
    val interestRate = decimal("interest_rate", 5, 2)
    val currency = varchar("currency", 3)
    val startDate = varchar("start_date", 30)
    val endDate = varchar("end_date", 30)
    val status = varchar("status", 20).default("ACTIVE")

    override val primaryKey = PrimaryKey(id)
}