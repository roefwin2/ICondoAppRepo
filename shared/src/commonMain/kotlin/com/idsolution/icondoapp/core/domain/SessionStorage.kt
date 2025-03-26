package com.idsolution.icondoapp.core.domain

interface SessionStorage {

    suspend fun get() : AuthInfo?
    suspend fun set(info: AuthInfo?)
    suspend fun clear()
}