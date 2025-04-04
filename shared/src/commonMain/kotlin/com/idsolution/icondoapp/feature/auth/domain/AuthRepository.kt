package com.example.testkmpapp.feature.auth.domain

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.auth.domain.models.ICondoUser

interface AuthRepository {
    val loggedUser : ICondoUser?
    suspend fun login(email: String, password: String): Result<Unit,DataError.Network>
    suspend fun  signup(firstName: String, lastName: String,email: String, password: String): Result<Unit,DataError.Network>
    suspend fun getUser(userName : String): Result<Unit,DataError.Network>
}