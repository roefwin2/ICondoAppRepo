package com.example.testkmpapp.feature.ssh.domain.models

data class CondoSite(
    val siteName: String,
    val host: String,
    val port: Int,
    val doors: Set<Door> = setOf(),
    val siteUser: String,
    val sitePwd: String,
    val siteSshPort: Int
)
