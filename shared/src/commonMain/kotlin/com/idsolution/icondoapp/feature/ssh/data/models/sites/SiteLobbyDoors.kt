package com.idsolution.icondoapp.feature.ssh.data.models.sites


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteLobbyDoors(
    @SerialName("door1")
    val door1: Int? = null,
    @SerialName("door2")
    val door2: Int? = null,
    @SerialName("door3")
    val door3: Int? = null
)