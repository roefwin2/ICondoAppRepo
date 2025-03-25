package com.example.testkmpapp.feature.auth.data

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.data.networking.models.AccessTokenResponse
import com.idsolution.icondoapp.core.domain.AuthInfo
import com.idsolution.icondoapp.core.domain.SessionStorage
import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.feature.auth.data.models.UserDto
import com.idsolution.icondoapp.feature.auth.data.models.toDomain
import com.idsolution.icondoapp.feature.auth.domain.models.ICondoUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage
) : AuthRepository {
    private var _loggedUser: ICondoUser? = null
    override val loggedUser get() = _loggedUser

    override suspend fun login(
        email: String,
        password: String
    ): EmptyDataResult<DataError.Network> =
        withContext(Dispatchers.IO) {

            val result = httpClient.post(
                urlString = "https://api.i-dsolution.com/oauth/token?grant_type=password&scope=read_profile&username=johndoe&password=SecurePass123!"
            )
            if (result.status.isSuccess()) {
                val json =
                    Json {
                        ignoreUnknownKeys = true
                    } // Pour ignorer les clés inconnues si nécessaire
                val accessTokenResponse =
                    json.decodeFromString<AccessTokenResponse>(result.body())
                sessionStorage.set(
                    AuthInfo(
                        accessToken = accessTokenResponse.accessToken,
                        userId = accessTokenResponse.jti
                    )
                )
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        }

    override suspend fun getUser(userName: String): EmptyDataResult<DataError.Network> =
        withContext(Dispatchers.IO) {
            val result = httpClient.get(
                urlString = "https://api.i-dsolution.com/users/getUser?username=johndoe"
            )
            if (result.status.isSuccess()) {
                val json =
                    Json {
                        ignoreUnknownKeys = true
                    } // Pour ignorer les clés inconnues si nécessaire
                val userDto =
                    json.decodeFromString<UserDto>(result.body())
                val iCondoUser = userDto.toDomain()
                _loggedUser = iCondoUser
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        }
}