package com.idsolution.icondoapp.feature.auth.data.models


import com.example.testkmpapp.feature.ssh.domain.models.Door
import com.idsolution.icondoapp.core.presentation.helper.Idle
import com.idsolution.icondoapp.feature.auth.domain.models.ICondoUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("acces")
    val acces: Acces?,
    @SerialName("enabled")
    val enabled: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("lastname")
    val lastname: String?,
    @SerialName("site_name")
    val siteName: String?,
    @SerialName("username")
    val username: String
)

fun UserDto.toDomain() = ICondoUser(
    username = username,
    lastname = lastname.toString(),
    siteName = siteName,
    accessDoors = acces?.doors?.map { Door("Door :$it",Idle(),it) }?.toSet() ?: emptySet(),
    keyTags = acces?.keytags?.map { Door("Keytag :$it",Idle(),it) }?.toSet() ?: emptySet()
)