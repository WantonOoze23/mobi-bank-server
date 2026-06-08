package com.mobibank.features.auth

import com.mobibank.features.auth.models.RegisterRequest
import com.mobibank.features.auth.models.UpdateProfileRequest
import com.mobibank.features.auth.models.UsersTable
import database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid


@OptIn(ExperimentalUuidApi::class)
class AuthRepository {

    suspend fun createUser(request: RegisterRequest, password: String): Boolean{
        return dbQuery{
            val insertStatement = UsersTable.insert {
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[middleName] = request.middleName ?: ""
                it[phone] = request.phone
                it[email] = request.email
                it[passwordHash] = password
            }
            insertStatement.insertedCount > 0
        }
    }

    suspend fun getUserById(userId: UUID): ResultRow? {
        return dbQuery {
            UsersTable.selectAll().where { UsersTable.id eq userId.toKotlinUuid() }.singleOrNull()
        }
    }

    suspend fun updateProfile(userId: UUID, request: UpdateProfileRequest): Boolean {
        return dbQuery {
            UsersTable.update({ UsersTable.id eq userId.toKotlinUuid() }) {
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[middleName] = request.middleName ?: ""
                it[email] = request.email
            } > 0
        }
    }

    suspend fun updatePassword(userId: UUID, newPasswordHash: String): Boolean {
        return dbQuery {
            UsersTable.update({ UsersTable.id eq userId.toKotlinUuid() }) {
                it[passwordHash] = newPasswordHash
            } > 0
        }
    }

    suspend fun getUserByLogin(login: String): ResultRow? {
        return dbQuery {
            UsersTable.selectAll().where { (UsersTable.phone eq login) or (UsersTable.email eq login)}.singleOrNull()
        }
    }

}