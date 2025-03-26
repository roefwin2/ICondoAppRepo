package com.idsolution.icondoapp.feature.auth.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUser(
    @SerialName("id") val id: Int? = null,
    @SerialName("firstname") val firstname: String,
    @SerialName("lastname") val lastname: String,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("enabled") val enabled: Boolean
)
