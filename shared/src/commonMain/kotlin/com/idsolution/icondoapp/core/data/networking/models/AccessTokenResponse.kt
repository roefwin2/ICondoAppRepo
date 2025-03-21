package com.idsolution.icondoapp.core.data.networking.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("jti")
    val jti: String,
    @SerialName("scope")
    val scope: String,
    @SerialName("token_type")
    val tokenType: String
)