package com.idsolution.icondoapp.feature.auth.data.models


import com.idsolution.icondoapp.feature.auth.domain.models.ICondoUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("acces")
    val acces: Acces,
    @SerialName("enabled")
    val enabled: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("lastname")
    val lastname: String,
    @SerialName("site_name")
    val siteName: String,
    @SerialName("username")
    val username: String
)

fun UserDto.toDomain() = ICondoUser(
    username = username,
    lastname = lastname,
    siteName = siteName,
    accessDoors = acces.doors.toSet(),
    keyTags = acces.keytags.toSet()
)