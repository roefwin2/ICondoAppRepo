package com.idsolution.icondoapp.core.data.networking

import com.idsolution.icondoapp.core.data.networking.models.AccessTokenResponse
import com.idsolution.icondoapp.core.domain.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProviders
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    engine: HttpClientEngine,
    sessionStorage: SessionStorage
): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }


        // ✅ INSTALLER LE PLUGIN DE REFRESH EN PREMIER
        install(TokenRefreshPlugin) {
            this.sessionStorage = sessionStorage
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val authInfo = sessionStorage.get()
                    println("🔑 loadTokens called - authInfo exists: ${authInfo != null}")
                    authInfo?.let {
                        println("   - Access token: ${it.accessToken.take(20)}...")
                        println("   - Refresh token exists: ${it.refreshToken != null}")
                        BearerTokens(
                            accessToken = it.accessToken,
                            refreshToken = it.refreshToken ?: ""
                        )
                    }
                }

                refreshTokens {
                    println("🔄 refreshTokens triggered - Token expired or unauthorized")

                    val authInfo = sessionStorage.get()
                    if (authInfo == null) {
                        println("❌ No auth info in storage")
                        return@refreshTokens null
                    }

                    val refreshToken = authInfo.refreshToken
                    if (refreshToken == null) {
                        println("❌ No refresh token in auth info")
                        return@refreshTokens null
                    }

                    println("📤 Attempting refresh with token: ${refreshToken.take(20)}...")

                    try {
                        val response = client.post("https://api.i-dsolution.com/oauth/token") {
                            markAsRefreshTokenRequest()
                            contentType(ContentType.Application.Json)
                            setBody(
                                mapOf(
                                    "grant_type" to "refresh_token",
                                    "refresh_token" to refreshToken,
                                    "client_id" to "icondo_android",
                                    "client_secret" to "123456"
                                )
                            )
                        }

                        println("📥 Refresh response status: ${response.status}")

                        if (response.status.isSuccess()) {
                            val responseBody: String = response.body()
                            println("📦 Refresh response body: $responseBody")

                            val json = Json { ignoreUnknownKeys = true }
                            val tokenResponse =
                                json.decodeFromString<AccessTokenResponse>(responseBody)

                            println("✅ Token refreshed successfully")
                            println("   - New access token: ${tokenResponse.accessToken.take(20)}...")
                            println("   - New refresh token: ${tokenResponse.refreshToken?.take(20)}...")

                            val updatedAuthInfo = authInfo.copy(
                                accessToken = tokenResponse.accessToken,
                                refreshToken = tokenResponse.refreshToken ?: authInfo.refreshToken
                            )

                            sessionStorage.set(updatedAuthInfo)
                            println("💾 New tokens saved to storage")

                            val verification = sessionStorage.get()
                            println("🔍 Verification - tokens in storage:")
                            println("   - Access: ${verification?.accessToken?.take(20)}...")
                            println("   - Refresh: ${verification?.refreshToken?.take(20)}...")

                            BearerTokens(
                                accessToken = tokenResponse.accessToken,
                                refreshToken = tokenResponse.refreshToken ?: refreshToken
                            )
                        } else {
                            val errorBody: String = response.body()
                            println("❌ Refresh failed with status: ${response.status}")
                            println("❌ Error body: $errorBody")

                            sessionStorage.clear()
                            null
                        }
                    } catch (e: Exception) {
                        println("❌ Refresh exception: ${e.message}")
                        e.printStackTrace()

                        sessionStorage.clear()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    val isOAuthEndpoint = request.url.host == "api.i-dsolution.com" &&
                            request.url.encodedPath.contains("/oauth/token")

                    println("🔒 sendWithoutRequest - URL: ${request.url}, isOAuth: $isOAuthEndpoint")
                    !isOAuthEndpoint
                }
            }
        }

        // ✅ AJOUTER CETTE PARTIE pour gérer aussi le 403
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                val clientException = exception as? ClientRequestException
                    ?: return@handleResponseExceptionWithRequest

                // Si on reçoit un 403 Forbidden, on peut aussi tenter un refresh
                if (clientException.response.status.value == 403) {
                    println("⚠️ Received 403 Forbidden - May indicate expired token")
                    println("   URL: ${request.url}")

                    // Ktor va gérer le refresh via le plugin Auth
                    // On ne fait rien ici, juste du logging
                }
            }
        }
    }
}

// Force the Auth plugin to invoke the `loadTokens` block again on the next client request.
fun HttpClient.invalidateBearerTokens() {
    try {
        val token = authProviders
            .filterIsInstance<BearerAuthProvider>()
            .first()
        println("token: $token")
        token.clearToken()
        println("Bearer tokens cleared")
    } catch (e: IllegalStateException) {
        println("Error invalidate token $e")
    }
}

val CONDO_URL = "https://api.i-dsolution.com"