package com.example.testkmpapp.feature.ssh.data.models


import kotlinx.serialization.Serializable

@Serializable
data class SubmitLoginRequest(
    val password: String,
    val siteName: String,
    val username: String
)