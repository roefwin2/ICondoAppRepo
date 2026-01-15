package com.idsolution.icondoapp.feature.auth.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val grant_type: String,
    val username: String,
    val password: String,
    val client_id: String,
    val client_secret: String
)
