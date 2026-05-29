package com.mobibank.features.auth

import com.mobibank.features.auth.models.RegisterRequest
import com.mobibank.features.auth.models.UsersTable
import database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll


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

    suspend fun getUserByLogin(login: String): ResultRow? {
        return dbQuery {
            UsersTable.selectAll().where { (UsersTable.phone eq login) or (UsersTable.email eq login)}.singleOrNull()
        }
    }

}