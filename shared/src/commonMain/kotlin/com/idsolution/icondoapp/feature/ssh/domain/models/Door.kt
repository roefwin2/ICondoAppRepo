package com.example.testkmpapp.feature.ssh.domain.models

import com.idsolution.icondoapp.core.presentation.helper.Resource

data class Door(
    val name: String,
    val isOpen: Resource<Boolean>,
    val number: Int
)

