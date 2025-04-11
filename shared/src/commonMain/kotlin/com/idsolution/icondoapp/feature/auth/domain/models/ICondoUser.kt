package com.idsolution.icondoapp.feature.auth.domain.models

import com.example.testkmpapp.feature.ssh.domain.models.Door

data class ICondoUser(
    val username : String,
    val lastname : String,
    val siteName : String?,
    val accessDoors : Set<Door>,
    val keyTags :Set<Door>
)
