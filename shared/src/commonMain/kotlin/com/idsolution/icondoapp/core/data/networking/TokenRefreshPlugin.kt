package com.idsolution.icondoapp.core.data.networking

import com.idsolution.icondoapp.core.data.networking.models.AccessTokenResponse
import com.idsolution.icondoapp.core.domain.AuthInfo
import com.idsolution.icondoapp.core.domain.SessionStorage
import io.ktor.client.call.body
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

val TokenRefreshPlugin = createClientPlugin(
    "TokenRefreshPlugin",
    createConfiguration = ::TokenRefreshPluginConfig
) {
    val sessionStorage = pluginConfig.sessionStorage

    onResponse { response ->
        if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
            println("⚠️ Received ${response.status.value} - Token may be expired")
            println("   URL: ${response.call.request.url}")

            if (response.call.request.url.encodedPath.contains("/oauth/token")) {
                println("   Skipping refresh for OAuth endpoint")
                return@onResponse
            }

            println("🔄 Attempting automatic token refresh via re-login")

            val authInfo = sessionStorage.get()
            if (authInfo == null) {
                println("❌ No auth info available")
                return@onResponse
            }

            if (authInfo.username.isEmpty() || authInfo.password.isEmpty()) {
                println("❌ Missing credentials for refresh")
                return@onResponse
            }

            println("📤 Re-logging in as: ${authInfo.username}")

            try {
                // ✅ Re-login avec username/password au lieu d'utiliser refresh_token
                val refreshResponse = response.call.client.post("https://api.i-dsolution.com/oauth/token") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf(
                        "grant_type" to "password",
                        "username" to authInfo.username,
                        "password" to authInfo.password,
                        "client_id" to "icondo_android",
                        "client_secret" to "123456"
                    ))
                }

                println("📥 Refresh response: ${refreshResponse.status}")

                if (refreshResponse.status.isSuccess()) {
                    val responseBody: String = refreshResponse.body()
                    println("📦 Response body: $responseBody")

                    val json = Json { ignoreUnknownKeys = true }
                    val tokenResponse = json.decodeFromString<AccessTokenResponse>(responseBody)

                    println("✅ Token refreshed successfully via re-login!")
                    println("   - New access token: ${tokenResponse.accessToken.take(20)}...")
                    println("   - New refresh token: ${tokenResponse.refreshToken?.take(20)}...")

                    sessionStorage.set(
                        AuthInfo(
                            accessToken = tokenResponse.accessToken,
                            refreshToken = tokenResponse.refreshToken,
                            username = authInfo.username,
                            password = authInfo.password
                        )
                    )

                    println("💾 New tokens saved to storage")

                    response.call.client.invalidateBearerTokens()

                } else {
                    val errorBody: String = refreshResponse.body()
                    println("❌ Refresh failed: ${refreshResponse.status}")
                    println("❌ Error: $errorBody")
                    sessionStorage.clear()
                }
            } catch (e: Exception) {
                println("❌ Refresh exception: ${e.message}")
                e.printStackTrace()
                sessionStorage.clear()
            }
        }
    }
}

class TokenRefreshPluginConfig {
    lateinit var sessionStorage: SessionStorage
}