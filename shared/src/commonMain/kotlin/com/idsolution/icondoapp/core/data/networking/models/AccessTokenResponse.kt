package com.idsolution.icondoapp.core.data.networking.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,  // ✅ AJOUTER CETTE LIGNE
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("scope")
    val scope: String,
    @SerialName("jti")
    val jti: String? = null
)