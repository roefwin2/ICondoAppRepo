package com.idsolution.icondoapp.feature.ssh.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class DoorStatus(
    val id: Int,
    val doorName: String,
    val isOpen: Boolean,
    val lastUpdated: String? = null
)