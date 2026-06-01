package ua.mobibank.di

import com.mobibank.features.auth.AuthRepository
import features.accounts.AccountRepository
import features.accounts.AccountService
import features.loans.LoanRepository
import features.loans.LoanService
import features.transactions.TransactionRepository
import org.koin.dsl.module
import ua.mobibank.features.auth.AuthService
import ua.mobibank.features.deposits.DepositRepository
import ua.mobibank.features.deposits.DepositService
import ua.mobibank.features.transactions.TransactionService

val appModule = module {
    single { AuthRepository() }
    single { AuthService(get()) }

    single { AccountRepository() }
    single { AccountService(get()) }

    single { LoanRepository() }
    single { LoanService(get()) }

    single { DepositRepository() }
    single { DepositService(get()) }

    single { TransactionRepository() }
    single { TransactionService(get()) }
}