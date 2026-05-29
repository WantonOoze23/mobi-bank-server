package com.mobibank.features.auth.models

import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

object UsersTable : Table("users") {
    @OptIn(ExperimentalUuidApi::class)
    val id = uuid("id").autoGenerate()

    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val middleName = varchar("middle_name", 50)

    val phone = varchar("phone", 15).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()

    val passwordHash = varchar("password_hash", 255)

    @OptIn(ExperimentalUuidApi::class)
    override val primaryKey = PrimaryKey(id)
}