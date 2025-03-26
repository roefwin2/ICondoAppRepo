package com.example.testkmpapp.feature.auth.data

import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.data.networking.models.AccessTokenResponse
import com.idsolution.icondoapp.core.domain.AuthInfo
import com.idsolution.icondoapp.core.domain.SessionStorage
import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.example.testkmpapp.feature.ssh.data.models.SubmitLoginRequest
import com.idsolution.icondoapp.feature.auth.data.models.CreateUser
import com.idsolution.icondoapp.feature.auth.data.models.UserDto
import com.idsolution.icondoapp.feature.auth.data.models.toDomain
import com.idsolution.icondoapp.feature.auth.domain.models.ICondoUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
) : AuthRepository {
    private var _loggedUser: ICondoUser? = null
    override val loggedUser get() = _loggedUser

    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit,DataError.Network> =
        withContext(Dispatchers.IO) {

            val result = httpClient.post(
                urlString = "https://api.i-dsolution.com/oauth/token?grant_type=password&scope=read_profile&username=$email&password=$password"
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
                        username = ""
                    )
                )
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        }

    override suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): EmptyDataResult<DataError.Network> =
        withContext(Dispatchers.IO) {

            val result = httpClient.post(
                urlString = "https://api.i-dsolution.com/users/sign-up"
            ){
                contentType(ContentType.Application.Json)
                setBody(
                    CreateUser(
                        firstname = firstName,
                        lastname = lastName,
                        username = email,
                        password = password,
                        enabled = true
                    )
                )
            }
            if (result.status.isSuccess()) {
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        }

    override suspend fun getUser(userName: String): Result<Unit,DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                val result = httpClient.get(
                    urlString = "https://api.i-dsolution.com/users/getUser?username=$userName"
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
                    println("AuthRepositoryImpl getUser: $iCondoUser")
                    Result.Success(Unit)
                } else {
                    println("AuthRepositoryImpl getUser: ${result.status}")
                    Result.Error(DataError.Network.SERVER_ERROR)
                }
            }catch (e:Exception){
                println("AuthRepositoryImpl getUser: $e")
                Result.Error(DataError.Network.UNKNOWN)
            }
        }
}