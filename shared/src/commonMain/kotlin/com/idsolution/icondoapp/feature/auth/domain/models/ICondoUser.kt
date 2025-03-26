package com.idsolution.icondoapp.feature.auth.domain.models

data class ICondoUser(
    val username : String,
    val lastname : String,
    val siteName : String?,
    val accessDoors : Set<Int>,
    val keyTags :Set<Int>
)
