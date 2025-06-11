package com.idsolution.icondoapp.core.domain.usecases

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.domain.SessionStorage
import com.idsolution.icondoapp.feature.auth.domain.AuthSessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ICondoLoginUseCase(
    private val authRepository: AuthRepository,
    private val authSessionManager: AuthSessionManager,
    private val sessionStorage: SessionStorage
) {
    fun invoke(
        email: String,
        password: String,
        voipUsername: String,
        voipPassword: String,
        voipDomain: String
    ): Flow<Result<Unit, DataError.Network>> = flow {
        val result = authRepository.login(email, password)
        if (result is Result.Error) {
            println("ICondoLoginUseCase invoke: ${result.error}")
            emit(result)
        }
        if (result is Result.Success) {
            println("ICondoLoginUseCase Success login : ${sessionStorage.get()}")
            val userInfos = authRepository.getUser(email)
            println("ICondoLoginUseCase Success login 2")
            if (userInfos is Result.Success) {
                println("ICondoLoginUseCase Success login 3")
                val authInfo = sessionStorage.get()
                val username = authRepository.loggedUser?.username ?: ""
                println("ICondoLoginUseCase Success login $username")
                val newAuthInfo = authInfo?.copy(username = username)
                sessionStorage.set(newAuthInfo)
                println("ICondoLoginUseCase Success login")
                authSessionManager.onLoginSuccess(authRepository.loggedUser)
                iCondoVoipLoginUseCase(
                    username = voipUsername,
                    password = voipPassword,
                    domain = voipDomain
                )
                emit(Result.Success(Unit))
            } else {
                println("ICondoLoginUseCase Error login")
                emit(userInfos)
            }
        }
    }
}