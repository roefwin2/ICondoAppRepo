package com.idsolution.icondoapp.feature.ssh.data.models.sites


import com.idsolution.icondoapp.feature.ssh.domain.models.DoorName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DoorNameDto(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String
)

fun DoorNameDto.toDomain(): DoorName {
    return DoorName(
        id = id,
        doorName = name
    )
}