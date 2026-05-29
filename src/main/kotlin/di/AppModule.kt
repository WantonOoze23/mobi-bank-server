package ua.mobibank.di

import com.mobibank.features.auth.AuthRepository
import features.accounts.AccountRepository
import features.accounts.AccountService
import org.koin.dsl.module
import ua.mobibank.features.auth.AuthService

val appModule = module {
    single { AuthRepository() }
    single { AuthService(get()) }

    single { AccountRepository() }
    single { AccountService(get()) }


}