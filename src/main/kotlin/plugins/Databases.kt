package ua.mobibank.plugins

import database.DatabaseFactory
import io.ktor.server.application.Application

fun Application.configureDatabases(){
    DatabaseFactory.init()
}

