package com.idsolution.icondoapp.core.data.networking

import com.idsolution.icondoapp.core.data.networking.models.AccessTokenResponse
import com.idsolution.icondoapp.core.domain.AuthInfo
import com.idsolution.icondoapp.core.domain.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProviders
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val sessionStorage: SessionStorage
) {

    fun build(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(HttpRequestRetry) {
                maxRetries = 1 // On réessaie une fois après le refresh
                retryOnServerErrors(maxRetries)
                retryIf { request, response ->
                    response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden
                }
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "icondo_web", password = "123456")
                    }
                    realm = "oauth2/client"
                }
                bearer {
                    loadTokens {
                        val authInfo = sessionStorage.get()
                        println("loadTokens: $authInfo")
                        if (authInfo != null && authInfo.accessToken.isNotEmpty()) {
                            BearerTokens(accessToken = authInfo.accessToken, refreshToken = "refresh")
                        } else {
                            // Si pas de token valide, forcer le rafraîchissement
                            null
                        }
                    }
                    refreshTokens {
                        println("refreshTokens")
                        try {
                            // Récupérer les informations d'authentification actuelles pour le nom d'utilisateur
                            val authInfo = sessionStorage.get()
                            val username = authInfo?.username ?: "david@ldctechnologie.com"

                            println("Refreshing token for user: $username")

                            // Récupérer un nouveau token avec les identifiants de l'utilisateur
                            val response = client.post("$CONDO_URL/oauth/token") {
                                markAsRefreshTokenRequest()
                                header("Authorization", "Basic aWNvbmRvX3dlYjoxMjM0NTY=")
                                contentType(ContentType.Application.FormUrlEncoded)
                                url {
                                    parameters.append("grant_type", "password")
                                    parameters.append("scope", "read_profile")
                                    parameters.append("username", username)
                                    parameters.append("password", "icondo") // Idéalement, stockez le mot de passe de manière sécurisée
                                }
                            }
                            if (response.status.isSuccess()) {
                                val accessTokenResponse = response.body<AccessTokenResponse>()

                                // Mettre à jour les informations d'authentification
                                val newAuthInfo = AuthInfo(
                                    accessToken = accessTokenResponse.accessToken,
                                    username = username
                                )

                                println("Token refreshed successfully for $username")
                                sessionStorage.set(newAuthInfo)
                                BearerTokens(
                                    accessToken = accessTokenResponse.accessToken,
                                    refreshToken = "refresh"
                                )
                            } else {
                                println("Failed to refresh token, status: ${response.status}")
                                null
                            }
                        } catch (e: Exception) {
                            println("Exception during token refresh: ${e.message}")
                            e.printStackTrace()
                            null
                        }
                    }
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
        // No-op; plugin not installed
    }
}

val CONDO_URL = "https://api.i-dsolution.com"