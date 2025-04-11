package com.idsolution.icondoapp.feature.ssh.data.models.sites


import com.idsolution.icondoapp.core.presentation.helper.Success
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import com.example.testkmpapp.feature.ssh.domain.models.Door
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SitesDto(
    @SerialName("data")
    val sites: List<SiteDto>
)

fun SitesDto.toDomain(): List<CondoSite> {
    return sites.map {
        CondoSite(
            it.siteName,
            host = it.sitePublicIP ?: "",
            port = it.sitePublicPort ?: 0,
            doors = it.siteLobbyDoors?.run {
                setOf(
                    Door(
                        name = "Door : $door1",
                        isOpen = Success(false),
                        number = door1 ?: 0
                    ),
                    Door(
                        name = "Door : $door2",
                        isOpen = Success(false),
                        number = door2 ?: 0
                    ),
                    Door(
                        name = "Door : $door3",
                        isOpen = Success(false),
                        number = door3 ?: 0
                    )
                )
            } ?: setOf(),
            siteUser = it.siteUser, sitePwd = it.sitePwd, siteSshPort = it.siteSshPort)
    }
}