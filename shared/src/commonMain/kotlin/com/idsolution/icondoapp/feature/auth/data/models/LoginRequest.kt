package com.example.testkmpapp.feature.auth.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username : String,
    val password :String
)
