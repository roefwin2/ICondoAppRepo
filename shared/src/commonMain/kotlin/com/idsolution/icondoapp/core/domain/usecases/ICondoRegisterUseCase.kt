package com.idsolution.icondoapp.core.domain.usecases

import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

class ICondoRegisterUseCase(
    private val authRepository: AuthRepository,
    private val iCondoLoginUseCase: ICondoLoginUseCase
) {
    fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        voipUsername: String,
        voipPassword: String,
        voipDomain: String
    ): Flow<Result<Unit, DataError.Network>> = flow {
        val result = authRepository.signup(firstName, lastName, email, password)
        if (result is Result.Error) {
            println("ICondoRegisterUseCase invoke: ${result.error}")
            emit(result)
        }
        if (result is Result.Success) {
            iCondoLoginUseCase.invoke(email, password, voipUsername, voipPassword, voipDomain)
                .collect {
                    println("ICondoRegisterUseCase invoke: $it")
                    emit(it)
                }
        }
    }
}