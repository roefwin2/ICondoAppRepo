package com.example.testkmpapp.feature.auth.data

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.data.networking.models.AccessTokenResponse
import com.idsolution.icondoapp.core.domain.AuthInfo
import com.idsolution.icondoapp.core.domain.SessionStorage
import com.example.testkmpapp.feature.auth.domain.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
    override suspend fun login(
        email: String,
        password: String
    ): EmptyDataResult<DataError.Network> =
        withContext(Dispatchers.IO) {

            val result = httpClient.post(
                urlString = "https://api.i-dsolution.com/oauth/token?grant_type=password&scope=read_profile&username=david@ldctechnologie.com&password=icondo"
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
}