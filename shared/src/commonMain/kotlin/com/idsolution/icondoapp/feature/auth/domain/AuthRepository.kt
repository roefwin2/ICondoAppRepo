package com.example.testkmpapp.feature.auth.domain

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult

interface AuthRepository {
    suspend fun login(email: String, password: String): EmptyDataResult<DataError.Network>
}