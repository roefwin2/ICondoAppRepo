package com.idsolution.icondoapp.feature.ssh.data.models.cameras

import com.idsolution.icondoapp.feature.ssh.domain.models.Camera

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CamerasResponseDto(
    @SerialName("cameras")
    val cameras: List<CameraDto>
)

@Serializable
data class CameraDto(
    @SerialName("id")
    val id: Int,
    @SerialName("site_id")
    val siteId: Int,
    @SerialName("name")
    val name: String,
    @SerialName("url")
    val url: String
)

fun CameraDto.toDomain() = Camera(
    id = id,
    siteId = siteId,
    name = name,
    url = url
)

fun CamerasResponseDto.toDomain() = cameras.map { it.toDomain() }