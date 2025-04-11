package com.example.testkmpapp.feature.ssh.data.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartTunnelRequest(
    @SerialName("hostname") val hostname: String,
    @SerialName("password") val password: String,
    @SerialName("port") val port: Int,
    @SerialName("sshPort") val sshPort: Int,
    @SerialName("siteName") val siteName: String,
    @SerialName("username") val username: String
)