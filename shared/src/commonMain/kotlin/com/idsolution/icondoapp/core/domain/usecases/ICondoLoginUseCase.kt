package com.idsolution.icondoapp.core.domain.usecases

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.core.data.networking.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ICondoLoginUseCase(
    private val authRepository: AuthRepository,
) {
    fun invoke(
        email: String,
        password: String,
        voipUsername: String,
        voiPassword: String,
        domain: String,
    ): Flow<EmptyDataResult<DataError.Network>> = flow {
        val result = authRepository.login(email, password)
        if (result is Result.Error) {
            emit(result)
        }
        if (result is Result.Success) {
            emit(result)
        }
    }
}