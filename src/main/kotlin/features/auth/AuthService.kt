package ua.mobibank.features.auth

import com.mobibank.features.auth.AuthRepository
import com.mobibank.features.auth.models.RegisterRequest

class AuthService(private val repository: AuthRepository) {

    fun register(request: RegisterRequest, hash: String){

    }

    fun login(){

    }

}