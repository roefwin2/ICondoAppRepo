package com.idsolution.icondoapp.core.domain

data class AuthInfo(
    val accessToken: String,
    val refreshToken: String? = null,
    val username: String,
    val password: String
)