package com.idsolution.icondoapp.feature.auth.data.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Acces(
    @SerialName("doors")
    val doors: List<Int>,
    @SerialName("keytags")
    val keytags: List<Int>
)