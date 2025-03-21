package com.example.testkmpapp.feature.ssh.data.models


import kotlinx.serialization.Serializable

@Serializable
data class StartTunnelRequest(
    val hostname: String,
    val password: String,
    val port: Int,
    val siteName: String,
    val username: String
)