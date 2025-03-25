package com.idsolution.icondoapp.feature.ssh.data.models.sites


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteDto(
    @SerialName("site_address")
    val siteAddress: String,
    @SerialName("site_id")
    val siteId: Int,
    @SerialName("site_img")
    val siteImg: String,
    @SerialName("site_lobbyDoors")
    val siteLobbyDoors: SiteLobbyDoors? = null,
    @SerialName("site_name")
    val siteName: String,
    @SerialName("site_publicIP")
    val sitePublicIP: String?,
    @SerialName("site_publicPort")
    val sitePublicPort: Int?
)