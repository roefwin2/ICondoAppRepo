package com.idsolution.icondoapp.core.domain

import com.idsolution.icondoapp.core.domain.AuthInfo

interface SessionStorage {

    suspend fun get() : AuthInfo?
    suspend fun set(info: AuthInfo?)
}