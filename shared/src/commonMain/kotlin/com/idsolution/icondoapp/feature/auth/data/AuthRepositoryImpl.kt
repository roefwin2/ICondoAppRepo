package com.idsolution.icondoapp.feature.auth.data

import com.idsolution.icondoapp.core.data.networking.invalidateBearerTokens
import com.idsolution.icondoapp.feature.auth.data.models.LoginRequest
import com.idsolution.icondoapp.feature.auth.domain.AuthRepository
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.core.data.networking.models.AccessTokenResponse
import com.idsolution.icondoapp.core.domain.AuthInfo
import com.idsolution.icondoapp.core.domain.SessionStorage
import com.idsolution.icondoapp.feature.auth.data.models.CreateUser
import com.idsolution.icondoapp.feature.auth.data.models.UserDto
import com.idsolution.icondoapp.feature.auth.data.models.toDomain
import com.idsolution.icondoapp.feature.auth.domain.models.ICondoUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.request.delete
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

    companion object {
        private const val CLIENT_ID = "icondo_android"
        private const val CLIENT_SECRET = "123456"
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            println("🔐 AuthRepositoryImpl login: $email")

            // Invalider les tokens AVANT la requête
            httpClient.invalidateBearerTokens()

            val result = httpClient.post(
                urlString = "https://api.i-dsolution.com/oauth/token"
            ) {
                contentType(ContentType.Application.Json)
                // Marquer comme requête qui ne doit pas utiliser l'auth
                attributes.put(AuthCircuitBreaker, Unit)
                setBody(
                    LoginRequest(
                        grant_type = "password",
                        username = email,
                        password = password,
                        client_id = CLIENT_ID,
                        client_secret = CLIENT_SECRET
                    )
                )
            }

            if (result.status.isSuccess()) {
                val json = Json { ignoreUnknownKeys = true }
                val accessTokenResponse = json.decodeFromString<AccessTokenResponse>(result.body())

                println("📦 Tokens received:")
                println("   - Access token: ${accessTokenResponse.accessToken.take(20)}...")
                println("   - Refresh token: ${accessTokenResponse.refreshToken?.take(20)}...")
                println("   - Expires in: ${accessTokenResponse.expiresIn}s")

                sessionStorage.set(
                    AuthInfo(
                        accessToken = accessTokenResponse.accessToken,
                        refreshToken = accessTokenResponse.refreshToken,
                        username = email,
                        password = password
                    )
                )

                // Invalider à nouveau pour forcer le rechargement
                httpClient.invalidateBearerTokens()

                // Vérifier que c'est bien stocké
                val stored = sessionStorage.get()
                println("✅ Stored auth info has refresh token: ${stored?.refreshToken != null}")

                Result.Success(Unit)
            } else {
                println("❌ AuthRepositoryImpl login failed: ${result.status}")
                Result.Error(
                    DataError.Network.SERVER_ERROR,
                    "${result.call.request.url} : ${result.status.description}"
                )
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
            ) {
                httpClient.invalidateBearerTokens()
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
                Result.Error(
                    DataError.Network.SERVER_ERROR,
                    "${result.call.request.url} : ${result.status.description}"
                )
            }
        }

    override suspend fun getUser(userName: String): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                println("👤 Getting user: $userName")

                var result = httpClient.get(
                    urlString = "https://api.i-dsolution.com/users/getUser?username=$userName"
                )

                println("📥 getUser response status: ${result.status}")

                // Si 403 ou 401, tenter un refresh et réessayer
                if (result.status.value == 403 || result.status.value == 401) {
                    println("⚠️ ${result.status.value} received - Attempting token refresh")

                    when (val refreshResult = refreshToken()) {
                        is Result.Success -> {
                            println("✅ Token refreshed - Retrying getUser")
                            // Réessayer la requête avec le nouveau token
                            result = httpClient.get(
                                urlString = "https://api.i-dsolution.com/users/getUser?username=$userName"
                            )
                            println("📥 Retry getUser response status: ${result.status}")
                        }
                        is Result.Error -> {
                            println("❌ Token refresh failed")
                            return@withContext Result.Error(
                                DataError.Network.UNAUTHORIZED,
                                "Token refresh failed"
                            )
                        }
                    }
                }

                if (result.status.isSuccess()) {
                    val json = Json { ignoreUnknownKeys = true }
                    val userDto = json.decodeFromString<UserDto>(result.body())
                    val iCondoUser = userDto.toDomain()
                    _loggedUser = iCondoUser
                    println("✅ User retrieved: $iCondoUser")
                    Result.Success(Unit)
                } else {
                    println("❌ getUser failed: ${result.status}")
                    Result.Error(
                        DataError.Network.SERVER_ERROR,
                        "${result.call.request.url} : ${result.status.description}"
                    )
                }
            } catch (e: Exception) {
                println("❌ getUser exception: ${e.message}")
                e.printStackTrace()
                Result.Error(DataError.Network.UNKNOWN, e.message)
            }
        }

    override suspend fun refreshToken(): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            println("🔄 Attempting to refresh token via re-login...")

            val authInfo = sessionStorage.get()
            if (authInfo == null) {
                println("❌ No auth info available for refresh")
                return@withContext Result.Error(
                    DataError.Network.UNAUTHORIZED,
                    "No auth info available"
                )
            }

            if (authInfo.username.isEmpty() || authInfo.password.isEmpty()) {
                println("❌ Missing credentials for refresh")
                return@withContext Result.Error(
                    DataError.Network.UNAUTHORIZED,
                    "Missing credentials"
                )
            }

            println("🔄 Re-logging in as: ${authInfo.username}")

            // ✅ Invalider les tokens avant le re-login
            httpClient.invalidateBearerTokens()

            val result = httpClient.post(
                urlString = "https://api.i-dsolution.com/oauth/token"
            ) {
                contentType(ContentType.Application.Json)
                attributes.put(AuthCircuitBreaker, Unit)
                setBody(
                    LoginRequest(
                        grant_type = "password",
                        username = authInfo.username,
                        password = authInfo.password,
                        client_id = CLIENT_ID,
                        client_secret = CLIENT_SECRET
                    )
                )
            }

            if (result.status.isSuccess()) {
                val json = Json { ignoreUnknownKeys = true }
                val accessTokenResponse = json.decodeFromString<AccessTokenResponse>(result.body())

                println("✅ Token refreshed successfully via re-login")
                println("   - New access token: ${accessTokenResponse.accessToken.take(20)}...")
                println("   - New refresh token: ${accessTokenResponse.refreshToken?.take(20)}...")

                // ✅ Mettre à jour le storage avec les nouveaux tokens
                sessionStorage.set(
                    AuthInfo(
                        accessToken = accessTokenResponse.accessToken,
                        refreshToken = accessTokenResponse.refreshToken,
                        username = authInfo.username,
                        password = authInfo.password  // Garder le password pour les futurs refresh
                    )
                )

                // ✅ Invalider à nouveau pour forcer le rechargement
                httpClient.invalidateBearerTokens()

                Result.Success(Unit)
            } else {
                println("❌ Token refresh (re-login) failed: ${result.status}")
                val errorBody: String = result.body()
                println("❌ Error body: $errorBody")

                // ✅ Si le re-login échoue, clear le storage pour forcer un vrai login
                sessionStorage.clear()

                Result.Error(
                    DataError.Network.UNAUTHORIZED,
                    "Failed to refresh token: ${result.status.description}"
                )
            }
        }

    override suspend fun revokeToken(): Result<Unit, DataError.Network> =
        withContext(Dispatchers.IO) {
            try {
                println("🚪 AuthRepositoryImpl: Revoking token...")

                val authInfo = sessionStorage.get()
                if (authInfo == null || authInfo.accessToken.isEmpty()) {
                    println("⚠️ No token to revoke")
                    return@withContext Result.Success(Unit)
                }

                val result = httpClient.delete(
                    urlString = "https://api.i-dsolution.com/oauth/token"
                ) {
                    contentType(ContentType.Application.Json)
                    // Le token Bearer est ajouté automatiquement par le plugin Auth
                }

                if (result.status.isSuccess()) {
                    println("✅ Token revoked successfully")
                    Result.Success(Unit)
                } else {
                    println("⚠️ Token revocation failed: ${result.status}")
                    // On ne considère pas ça comme une erreur bloquante
                    // Le token expirera de toute façon
                    Result.Success(Unit)
                }
            } catch (e: Exception) {
                println("❌ Token revocation error: ${e.message}")
                // On retourne Success quand même car le logout local doit continuer
                Result.Success(Unit)
            }
        }

    override fun invalidateToken() {
        httpClient.invalidateBearerTokens()
    }
}